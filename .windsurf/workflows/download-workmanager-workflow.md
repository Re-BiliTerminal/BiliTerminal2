# Download Workflow (WorkManager OneTime + Download Manager)

## Context
This workflow describes how to implement a background download feature using **WorkManager one-time work** and a dedicated **DownloadManager** layer that persists state in a DB and exposes reactive status to UI.

Goals:
- Background-stable downloads with progress and cancellation.
- Single source of truth in DB for UI rendering.
- De-duplicate same resource downloads via a unique key.

Non-goals (initial version):
- True pause/resume unless HTTP Range + partial file persistence is implemented.

---

## Architecture
### Modules / Layers
- **DownloadManager (app layer service)**
  - Responsibilities:
    - Create and update download records.
    - Enqueue/cancel/retry WorkManager requests.
    - Enforce uniqueness (per resource key) and optional concurrency policy.
    - Expose `Flow`/`StateFlow` for UI.

- **Room DB**
  - `DownloadEntity` + `DownloadDao`
  - DB is the **business truth**; WorkInfo is auxiliary.

- **DownloadWorker (CoroutineWorker)**
  - Responsible only for executing the download, writing to file, updating DB progress.

- **NotificationCoordinator**
  - Creates foreground progress notifications via `setForeground()`.
  - Creates completion/failure notifications (optional).

- **UI**
  - Download list screen subscribes to DB flow.
  - UI triggers manager actions (enqueue/cancel/retry/open/delete).

---

## Data Model
### DownloadStatus
Recommended enum:
- `ENQUEUED`
- `RUNNING`
- `CANCELED`
- `SUCCEEDED`
- `FAILED`

(Optional future)
- `PAUSED`

### Table: `downloads`
Suggested fields:
- `id: Long` (PK)
- `key: String` (unique)

Request:
- `url: String`
- `headersJson: String?`
- `fileName: String`
- `destPath: String`
- `tempPath: String`

State:
- `status: DownloadStatus`
- `progress: Int` (0..100)
- `downloadedBytes: Long`
- `totalBytes: Long`
- `errorMessage: String?`

Work linkage:
- `workId: String?` (UUID string)

Timestamps:
- `createdAt: Long`
- `updatedAt: Long`
- `finishedAt: Long?`

Notes:
- Use `key` to enforce “same resource not duplicated”.
- For progress unknown: keep `totalBytes=0` and treat UI as indeterminate.

---

## WorkManager Design
### Work Type
- `OneTimeWorkRequest` using `CoroutineWorker`.

### Constraints
- `NetworkType.CONNECTED`
- Optional setting: `NetworkType.UNMETERED`.

### Backoff
- `BackoffPolicy.EXPONENTIAL` with a reasonable minimum backoff.

### Tags
- `download`
- `key:<downloadKey>`

### Unique Work
Use `enqueueUniqueWork(downloadKey, ExistingWorkPolicy.KEEP/REPLACE)`:
- Initial version:
  - KEEP: avoid duplicate enqueues.
  - REPLACE: used for explicit retry.

---

## Worker Input/Output
### InputData
- `downloadId: Long`
- `url: String`
- `destPath: String`
- `tempPath: String`
- `headersJson: String?`

(Optional)
- `expectedSha256: String?`
- `mime: String?`

### OutputData
- `downloadId: Long`
- `destPath: String`
- `totalBytes: Long`

---

## Worker Execution Flow
### 1) Start
- Load `downloadId`.
- DB update: `status=RUNNING`.
- Call `setForeground()` to show progress notification.

### 2) Download
- Stream download to `tempPath`.
- If response has `Content-Length`, set `totalBytes`.
- Update DB progress:
  - Throttle updates (e.g., every 300–800ms or per N KB) to reduce DB writes.
- Regularly check `isStopped`:
  - If stopped: abort loop and mark `CANCELED`.

### 3) Finish
- Rename `tempPath` -> `destPath`.
- DB update: `status=SUCCEEDED`, `progress=100`, `finishedAt=now`.

### 4) Failure / Retry
- Network transient errors: `Result.retry()`.
- Non-retriable errors (403/404/permission): `Result.failure()`.
- DB update: `status=FAILED`, `errorMessage=...`.

---

## DownloadManager Responsibilities
### Enqueue
`enqueueDownload(request)`:
- Compute `downloadKey`.
- DB lookup by key:
  - If `RUNNING/ENQUEUED`: return existing.
  - If `FAILED/CANCELED/SUCCEEDED`: allow re-enqueue based on UX decision.
- Create `OneTimeWorkRequest` and enqueue unique work.
- Persist `workId` in DB.

### Cancel
`cancel(downloadId)`:
- `workManager.cancelWorkById(UUID.fromString(workId))`
- DB update `CANCELED`.

### Retry
`retry(downloadId)`:
- Re-enqueue with `ExistingWorkPolicy.REPLACE`.

### Observe
- `observeAll(): Flow<List<DownloadEntity>>`
- `observeById(id): Flow<DownloadEntity>`

---

## UI / UX
### Download List Screen
Each item shows:
- File name
- Status
- Progress bar
- Optional total bytes / downloaded bytes

Actions:
- Cancel (RUNNING/ENQUEUED)
- Retry (FAILED)
- Open (SUCCEEDED)
- Delete record / delete file (SUCCEEDED/FAILED/CANCELED)

### Entry points
- Video detail / player page: “Download” button triggers `DownloadManager.enqueueDownload()`.

---

## Notifications
- Running: foreground notification with progress.
- Completed: completion notification (optional).
- Failed: failure notification with shortcut to retry (optional).

---

## Concurrency
Initial version:
- Rely on WorkManager scheduling + unique work per `key`.

Future:
- Limit concurrent RUNNING downloads by a scheduler inside DownloadManager.

---

## Pitfalls / Notes
- WorkManager is not a perfect fit for true pause/resume. If required:
  - Implement HTTP Range + persist partial bytes.
  - Restart worker from current size.
- Do not update DB progress too frequently.
- Ensure destination path permissions and storage strategy (app-private vs SAF/MediaStore) are decided upfront.
