package com.LCM.lifereplayapp.ui.todo

import android.content.Context
import android.content.Intent
import java.text.SimpleDateFormat
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoForm(
    todoViewModel: TodoViewModel = viewModel(),
    innerPaddingValues: PaddingValues,
    navController: NavHostController,
    modifier: Modifier
) {
    val progress by todoViewModel.uploadProgress.collectAsState()
    val taskCreated by todoViewModel.taskCreated.collectAsState()
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Navigation and reset logic
    if (taskCreated) {
        Toast.makeText(context, "Task created successfully!", Toast.LENGTH_SHORT).show()
        todoViewModel.resetTaskCreated()
        navController.popBackStack()
    }

    // date picker
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val isButtonEnabled by remember {
        derivedStateOf {
            title.isNotEmpty() && datePickerState.selectedDateMillis != null
        }
    }
    val taskDeadLineDate = datePickerState.selectedDateMillis?.let {
        convertMillisToDate(it)
    } ?: ""

    // image picker:
    val launcher = launchFilePicker(context, todoViewModel)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(innerPaddingValues)
            .padding(vertical = 32.dp, horizontal = 16.dp)
            .fillMaxHeight()
    ) {
        OutlinedTextField(
            value = title,
            label = { Text(text = "Title") },
            onValueChange = { title = it },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.padding(vertical = 8.dp))

        OutlinedTextField(
            value = description,
            label = { Text(text = "Description") },
            onValueChange = { description = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.padding(vertical = 8.dp))
        OutlinedTextField(
            value = taskDeadLineDate,
            label = { Text(text = "Deadline") },
            onValueChange = { },
            readOnly = true,
            trailingIcon = {
                IconButton(
                    onClick = { showDatePicker = !showDatePicker }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DateRange,
                        contentDescription = "Icon of Calendar"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.padding(vertical = 8.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = "*/*"
                        addCategory(Intent.CATEGORY_OPENABLE)
                    }
                    launcher.launch(intent)
                }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.padding(vertical = 2.dp))
                    Icon(
                        imageVector = Icons.Filled.PhotoCamera,
                        contentDescription = "Camera button"
                    )
                    Spacer(modifier = Modifier.padding(vertical = 2.dp))
                    Text(text = "Select Image")
                    Spacer(modifier = Modifier.padding(vertical = 2.dp))
                }
            }
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            Text("Progress: ${progress.toInt()}%")
        }
        Spacer(modifier = Modifier.padding(vertical = 8.dp))
        if (showDatePicker) {
            Popup(
                onDismissRequest = { showDatePicker = false },
                alignment = Alignment.TopStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 64.dp)
                        .shadow(elevation = 4.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    DatePicker(
                        state = datePickerState,
                        showModeToggle = false
                    )
                }
            }
        }

        Spacer(modifier = Modifier.padding(vertical = 8.dp))
        Button(
            enabled = isButtonEnabled,
            onClick = {
                todoViewModel.createTask(
                    title = title,
                    description = description,
                    dueDate = datePickerState.selectedDateMillis!!
                )
            }
        ) {
            Text(text = "Create task")
        }
    }
}

fun getFileNameFromUri(context: Context, uri: Uri): String? {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    return cursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (it.moveToFirst() && nameIndex != -1) {
            it.getString(nameIndex)
        } else null
    }
}

fun getFileSizeFromUri(context: Context, uri: Uri): Long {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    return cursor?.use {
        val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
        if (it.moveToFirst() && sizeIndex != -1) {
            it.getLong(sizeIndex)
        } else 0L
    } ?: 0L
}

fun getExtensionFromUri(context: Context, uri: Uri): String? {
    val mimeType = context.contentResolver.getType(uri)
    return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
}

@Composable
fun launchFilePicker(context: Context, todoViewModel: TodoViewModel): ManagedActivityResultLauncher<Intent, ActivityResult> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data
        if (uri != null) {
            val fileSize = getFileSizeFromUri(context, uri)
            val maxFileSize = 10L * 1024 * 1024 // 10MB limit

            if (fileSize > maxFileSize) {
                Toast.makeText(context, "File is too large (max 10MB)", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val fileBytes = inputStream?.use { it.readBytes() }
                    val fileName = getFileNameFromUri(context, uri) ?: "upload.${
                        getExtensionFromUri(context, uri) ?: "dat"
                    }"

                    if (fileBytes != null) {
                        todoViewModel.insertImage(fileName, fileBytes)
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to read file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}
