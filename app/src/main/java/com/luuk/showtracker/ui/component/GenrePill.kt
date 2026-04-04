package com.luuk.showtracker.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GenrePill(
    genreName: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = genreName,
        color = MaterialTheme.colorScheme.onSecondary,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(GenrePillDefaults.CornerRadius)
            )
            .padding(
                horizontal = GenrePillDefaults.HorizontalPadding,
                vertical = GenrePillDefaults.VerticalPadding
            )
    )
}

private object GenrePillDefaults {
    val CornerRadius = 999.dp
    val HorizontalPadding = 12.dp
    val VerticalPadding = 6.dp
}
