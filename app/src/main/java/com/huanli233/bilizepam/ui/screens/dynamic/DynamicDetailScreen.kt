package com.huanli233.bilizepam.ui.screens.dynamic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ScreenScaffold
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.ui.components.scrollAwareTopBar
import com.huanli233.bilizepam.ui.viewmodel.DynamicDetailUiState
import com.huanli233.bilizepam.ui.viewmodel.DynamicDetailViewModel

@Composable
fun DynamicDetailScreen(
    dynamicId: String,
    onNavigateBack: () -> Unit = {},
    onUserClick: (Long) -> Unit = {},
    onVideoClick: (String) -> Unit = {},
    onImageClick: (List<String>, Int) -> Unit = { _, _ -> },
    onDynamicClick: (com.huanli233.biliwebapi.bean.dynamic.Dynamic) -> Unit = {},
    viewModel: DynamicDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(dynamicId) {
        viewModel.loadDynamic(dynamicId)
    }

    ScreenScaffold(
        topBar = scrollAwareTopBar(
            title = stringResource(R.string.dynamic_detail),
            showBackIcon = true,
            onBackClick = onNavigateBack
        )
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is DynamicDetailUiState.Loading -> {
                    LoadingContent()
                }
                is DynamicDetailUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 8.dp)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        DynamicCard(
                            dynamic = state.dynamic,
                            onClick = {},
                            onUserClick = onUserClick,
                            onVideoClick = onVideoClick,
                            onImageClick = onImageClick,
                            onLikeClick = { dynamicId, isLiked ->
                                viewModel.likeDynamic(dynamicId, isLiked)
                            },
                            onDynamicClick = onDynamicClick,
                            showFullContent = true
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                is DynamicDetailUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.loadDynamic(dynamicId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onRetry) {
            Text(stringResource(R.string.retry))
        }
    }
}
