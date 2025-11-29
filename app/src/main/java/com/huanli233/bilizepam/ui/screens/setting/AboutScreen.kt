package com.huanli233.bilizepam.ui.screens.setting

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.materialcore.copy
import androidx.wear.compose.materialcore.plus
import androidx.wear.compose.materialcore.toVerticalPadding
import com.highcapable.betterandroid.ui.extension.component.base.toDp
import com.huanli233.bilizepam.BuildConfig
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.ui.components.scrollAwareTopBar
import com.huanli233.bilizepam.ui.components.rememberEnterAlwaysScrollBehavior
import com.huanli233.bilizepam.ui.dialog.AdaptDialog
import com.huanli233.bilizepam.ui.dialog.FullScreenAlertDialog

const val URL_QQ_CHANNEL = "https://pd.qq.com/s/fdti2l61d"
const val ID_QQ_GROUP = "719041250"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
    val uriHandler = LocalUriHandler.current
    var showGroupIdDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScalingLazyListState(initialCenterItemIndex = 0)
    
    // Create ScrollBehavior for TopBar
    val scrollBehavior = rememberEnterAlwaysScrollBehavior()

    if (showGroupIdDialog) {
        AdaptDialog(
            onDismissRequest = { showGroupIdDialog = false },
            title = { Text(stringResource(R.string.qq_group)) },
            text = { Text(stringResource(id = R.string.group_id, ID_QQ_GROUP)) },
            confirmButton = { close ->
                TextButton(onClick = { close() }) {
                    Text("OK")
                }
            }
        )
    }

    androidx.wear.compose.material3.ScreenScaffold(
        scrollState = scrollState,
        topBar = scrollAwareTopBar(
            title = stringResource(id = R.string.about),
            showBackIcon = true,
            onBackClick = { navController.popBackStack() },
            scrollBehavior = scrollBehavior
        ),
        topBarScrollBehavior = scrollBehavior
    ) {
        androidx.wear.compose.foundation.lazy.ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = scrollState,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = it
        ) {
            item {
                Image(
                    painter = painterResource(id = R.mipmap.icon),
                    contentDescription = "App Icon",
                    modifier = Modifier.size(48.dp)
                )
            }
            item {
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                Text(
                    text = stringResource(id = R.string.about_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            item {
                AssistChip(
                    onClick = { /* No-op */ },
                    label = { Text(stringResource(R.string.version_name_format, BuildConfig.VERSION_NAME)) },
                    leadingIcon = {
                        if (BuildConfig.DEBUG) {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_bug_report),
                                contentDescription = "Debug Build",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                )
            }
            item {
                Text(
                    text = stringResource(id = R.string.about_opensource),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            item {
                val repoUrl = stringResource(R.string.repo_url)
                TextButton(onClick = { uriHandler.openUri(repoUrl) }) {
                    Text(stringResource(id = R.string.repo_url))
                }
            }
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.contact_info),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
            item {
                FilledTonalButton(
                    onClick = { uriHandler.openUri(URL_QQ_CHANNEL) },
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_public),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(text = stringResource(id = R.string.qq_channel))
                }
            }
            item {
                FilledTonalButton(
                    onClick = { showGroupIdDialog = true },
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_group),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(text = stringResource(id = R.string.qq_group))
                }
            }
            item {
                Card(
                    modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = stringResource(id = R.string.disclaimer),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(id = R.string.about_to_uncle),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}