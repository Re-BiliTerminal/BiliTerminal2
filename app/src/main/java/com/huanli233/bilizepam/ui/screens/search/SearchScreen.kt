package com.huanli233.bilizepam.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material3.*
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.ui.components.rememberEnterAlwaysScrollBehavior
import com.huanli233.bilizepam.ui.components.scrollAwareTopBar
import com.huanli233.bilizepam.ui.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    onMenuClick: () -> Unit,
    onSearch: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val listState = rememberLazyListState()
    val scrollBehavior = rememberEnterAlwaysScrollBehavior()

    ScreenScaffold(
        scrollState = listState,
        topBar = scrollAwareTopBar(
            title = stringResource(R.string.search),
            showBackIcon = false,
            showMenuIcon = true,
            onMenuClick = onMenuClick,
            scrollBehavior = scrollBehavior
        ),
        topBarScrollBehavior = scrollBehavior
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = paddingValues,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                SearchInputCard(
                    searchQuery = searchQuery,
                    onQueryChange = { viewModel.updateSearchQuery(it) },
                    onSearch = {
                        if (searchQuery.isNotBlank()) {
                            viewModel.addToHistory(searchQuery)
                            onSearch(searchQuery)
                        }
                    }
                )
            }

            if (suggestions.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.search_suggestions),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(16.dp, 8.dp)
                    )
                }
                items(suggestions) { suggestion ->
                    SuggestionItem(
                        text = suggestion,
                        onClick = {
                            viewModel.updateSearchQuery(suggestion)
                            viewModel.addToHistory(suggestion)
                            onSearch(suggestion)
                        }
                    )
                }
            }

            if (searchHistory.isNotEmpty() && suggestions.isEmpty() && searchQuery.isEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp, 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.search_history),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = stringResource(R.string.clear_history),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { viewModel.clearHistory() }
                        )
                    }
                }
                items(searchHistory) { history ->
                    HistoryItem(
                        text = history,
                        onClick = {
                            viewModel.updateSearchQuery(history)
                            onSearch(history)
                        },
                        onDelete = { viewModel.removeHistoryItem(history) }
                    )
                }
            }

            if (searchQuery.isEmpty() && searchHistory.isEmpty()) {
                item {
                    EmptySearchState()
                }
            }
        }
    }
}

@Composable
private fun SearchInputCard(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = stringResource(R.string.search_hint),
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                singleLine = true
            )

            if (searchQuery.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onSearch,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.search))
                }
            }
        }
    }
}

@Composable
private fun SuggestionItem(
    text: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun HistoryItem(
    text: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "×",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable(onClick = onDelete)
            )
        }
    }
}

@Composable
private fun EmptySearchState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.search_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
