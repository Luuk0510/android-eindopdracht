package com.luuk.showtracker.ui.navigation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

internal object AppNavigationDefaults {
    const val SELECTED_ITEM_INDICATOR_ALPHA = 0.16f
    const val PROFILE_PHOTO_FILE_NAME = "profile_photo.jpg"
    const val PROFILE_PHOTO_QUALITY = 92

    val NavigationIconSize = 26.dp
    val TopBarIconSize = 28.dp
    val TopBarHorizontalPadding = 16.dp
    val TopBarVerticalPadding = 4.dp
    val TopBarTitleSpacing = 5.dp
    val SearchIconPadding = 6.dp
    val SearchFieldTopPadding = 12.dp
    val SortIconSize = 22.dp
    val SortButtonEndPadding = 12.dp
    val SortLabelSpacing = 6.dp
    val NavigationProfileAvatarSize = 24.dp
    val ProfileDialogOuterPadding = 20.dp
    val ProfileDialogInnerPadding = 24.dp
    val ProfileDialogAvatarSize = 76.dp
    val ProfileDialogSpacing = 12.dp
    val ProfileDialogHeaderSpacing = 16.dp
    val ProfileFieldSpacing = 18.dp
    val ProfileDialogActionsSpacing = 20.dp
    val ProfileCloseIconSize = 24.dp
}

@Composable
internal fun navigationBarItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.primary,
    selectedTextColor = MaterialTheme.colorScheme.primary,
    indicatorColor = MaterialTheme.colorScheme.primary.copy(
        alpha = AppNavigationDefaults.SELECTED_ITEM_INDICATOR_ALPHA
    )
)
