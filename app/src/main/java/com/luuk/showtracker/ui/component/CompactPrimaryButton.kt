package com.luuk.showtracker.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun CompactPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    iconContentDescription: String? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(CompactPrimaryButtonDefaults.Height),
        contentPadding = PaddingValues(
            horizontal = CompactPrimaryButtonDefaults.HorizontalPadding,
            vertical = CompactPrimaryButtonDefaults.VerticalPadding
        )
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = iconContentDescription,
                modifier = Modifier.size(CompactPrimaryButtonDefaults.IconSize)
            )
            Spacer(modifier = Modifier.width(CompactPrimaryButtonDefaults.IconSpacing))
        }

        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

object CompactPrimaryButtonDefaults {
    val Height = 38.dp
    val HorizontalPadding = 14.dp
    val VerticalPadding = 4.dp
    val IconSize = 18.dp
    val IconSpacing = 6.dp
}
