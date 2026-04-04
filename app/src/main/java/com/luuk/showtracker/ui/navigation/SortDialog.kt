package com.luuk.showtracker.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.luuk.showtracker.R
import com.luuk.showtracker.data.model.WatchlistSortOption

@Composable
internal fun WatchlistSortDialog(
    selectedSortOption: WatchlistSortOption,
    onOptionSelected: (WatchlistSortOption) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppNavigationDefaults.ProfileDialogOuterPadding),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(AppNavigationDefaults.ProfileDialogInnerPadding)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.sort_watchlist_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.content_close),
                        tint = Color.White,
                        modifier = Modifier
                            .clickable(onClick = onDismiss)
                            .size(AppNavigationDefaults.ProfileCloseIconSize)
                    )
                }

                Spacer(modifier = Modifier.padding(top = AppNavigationDefaults.ProfileDialogHeaderSpacing))

                WatchlistSortOption.entries.forEach { sortOption ->
                    TextButton(
                        onClick = { onOptionSelected(sortOption) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(sortOption.labelResId()),
                                color = if (sortOption == selectedSortOption) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    Color.White
                                },
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

internal fun WatchlistSortOption.labelResId(): Int {
    return when (this) {
        WatchlistSortOption.NEWEST -> R.string.sort_newest
        WatchlistSortOption.OLDEST -> R.string.sort_oldest
        WatchlistSortOption.TITLE_ASC -> R.string.sort_title_asc
        WatchlistSortOption.TITLE_DESC -> R.string.sort_title_desc
    }
}
