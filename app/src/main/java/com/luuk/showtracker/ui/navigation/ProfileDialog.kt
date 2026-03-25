package com.luuk.showtracker.ui.navigation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.luuk.showtracker.R
import com.luuk.showtracker.ui.component.CompactPrimaryButton
import com.luuk.showtracker.ui.component.ProfileAvatar
import java.io.File
import java.io.FileOutputStream

@Composable
internal fun ProfileDialogHost(
    profileName: String,
    profilePhotoUri: String?,
    showProfileDialog: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String?) -> Unit
) {
    if (!showProfileDialog) return

    val context = LocalContext.current
    val editedProfileNameState = remember(profileName) { mutableStateOf(profileName) }
    val editedProfilePhotoUriState = remember(profilePhotoUri) { mutableStateOf(profilePhotoUri) }

    val cameraPreviewLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            editedProfilePhotoUriState.value = saveProfilePhoto(context, bitmap)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraPreviewLauncher.launch(null)
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            editedProfilePhotoUriState.value = uri.toString()
        }
    }

    ProfileDialog(
        profileName = editedProfileNameState.value,
        profilePhotoUri = editedProfilePhotoUriState.value,
        onProfileNameChange = { editedProfileNameState.value = it },
        onChoosePhotoClick = { imagePickerLauncher.launch(arrayOf("image/*")) },
        onTakePhotoClick = {
            val hasCameraPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            if (hasCameraPermission) {
                cameraPreviewLauncher.launch(null)
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        },
        onDismiss = onDismiss,
        onSave = { onSave(editedProfileNameState.value.trim(), editedProfilePhotoUriState.value) }
    )
}

@Composable
private fun ProfileDialog(
    profileName: String,
    profilePhotoUri: String?,
    onProfileNameChange: (String) -> Unit,
    onChoosePhotoClick: () -> Unit,
    onTakePhotoClick: () -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
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
                        text = stringResource(R.string.profile_edit_title),
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

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProfileAvatar(
                        name = profileName,
                        photoUri = profilePhotoUri,
                        modifier = Modifier.size(AppNavigationDefaults.ProfileDialogAvatarSize)
                    )

                    Spacer(modifier = Modifier.padding(top = AppNavigationDefaults.ProfileDialogSpacing))

                    Text(
                        text = profileName.ifBlank { stringResource(R.string.profile_guest) },
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.padding(top = AppNavigationDefaults.ProfileDialogSpacing))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppNavigationDefaults.ProfileDialogSpacing)
                    ) {
                        CompactPrimaryButton(
                            text = stringResource(R.string.profile_gallery),
                            onClick = onChoosePhotoClick
                        )

                        CompactPrimaryButton(
                            text = stringResource(R.string.profile_camera),
                            onClick = onTakePhotoClick
                        )
                    }
                }

                Spacer(modifier = Modifier.padding(top = AppNavigationDefaults.ProfileFieldSpacing))

                OutlinedTextField(
                    value = profileName,
                    onValueChange = onProfileNameChange,
                    label = { Text(stringResource(R.string.profile_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.padding(top = AppNavigationDefaults.ProfileDialogActionsSpacing))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CompactPrimaryButton(
                        text = stringResource(R.string.profile_save),
                        onClick = onSave
                    )
                }
            }
        }
    }
}

private fun saveProfilePhoto(
    context: Context,
    bitmap: Bitmap
): String? {
    return runCatching {
        val photoFile = File(context.filesDir, AppNavigationDefaults.PROFILE_PHOTO_FILE_NAME)
        FileOutputStream(photoFile).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, AppNavigationDefaults.PROFILE_PHOTO_QUALITY, outputStream)
        }
        Uri.fromFile(photoFile).toString()
    }.getOrNull()
}
