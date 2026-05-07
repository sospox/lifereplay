package com.LCM.lifereplayapp.ui.screens.authentication

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.LCM.lifereplayapp.viewmodel.MemoryType
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.LCM.lifereplayapp.ui.navigation.ROUTES
import com.LCM.lifereplayapp.utils.AudioPlayer
import com.LCM.lifereplayapp.utils.FileUtils
import com.LCM.lifereplayapp.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController, 
    userViewModel: UserViewModel,
    modifier: Modifier
) {
    val context = LocalContext.current
    val userState by userViewModel.userState.collectAsState()
    val audioPlayer = remember { AudioPlayer(context) }

    DisposableEffect(Unit) {
        onDispose {
            audioPlayer.stop()
        }
    }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var voiceText by remember { mutableStateOf("") }
    
    // Gallery Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val internalUri = FileUtils.saveUriToInternalStorage(context, it)
            userViewModel.addMemory(MemoryType.IMAGE, contentUri = internalUri)
            selectedImageUri = internalUri?.let { uriStr -> Uri.parse(uriStr) }
            capturedBitmap = null
        }
    }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            val internalUri = FileUtils.saveBitmapToInternalStorage(context, it)
            capturedBitmap = it
            selectedImageUri = internalUri?.let { uriStr -> Uri.parse(uriStr) }
            userViewModel.addMemory(MemoryType.IMAGE, contentUri = internalUri, text = "Captured Photo")
        }
    }

    // Voice Input Launcher
    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            voiceText = data?.get(0) ?: ""
            if (voiceText.isNotEmpty()) {
                userViewModel.addMemory(MemoryType.VOICE, text = voiceText)
                Toast.makeText(context, "Voice Saved", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    // Music Launcher
    val musicLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val internalUri = FileUtils.saveUriToInternalStorage(context, it, prefix = "music")
            userViewModel.addMemory(MemoryType.MUSIC, contentUri = internalUri, text = "New Favorite Song")
            Toast.makeText(context, "Music Added", Toast.LENGTH_SHORT).show()
        }
    }

    // Permission Launchers
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch()
        } else {
            Toast.makeText(context, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
            }
            voiceLauncher.launch(intent)
        } else {
            Toast.makeText(context, "Mic Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile") },
                actions = {
                    IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery")
                    }
                    IconButton(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = "Camera")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                if (capturedBitmap != null) {
                    AsyncImage(
                        model = capturedBitmap,
                        contentDescription = "Captured Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = userState.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = userState.email,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Summary Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MemorySummaryItem(
                    count = userState.memories.count { it.type == MemoryType.IMAGE },
                    icon = Icons.Default.PhotoLibrary,
                    label = "Photos"
                )
                MemorySummaryItem(
                    count = userState.memories.count { it.type == MemoryType.VOICE },
                    icon = Icons.Default.Mic,
                    label = "Voices"
                )
                MemorySummaryItem(
                    count = userState.memories.count { it.type == MemoryType.MUSIC },
                    icon = Icons.Default.MusicNote,
                    label = "Songs"
                )
            }

            if (voiceText.isNotEmpty()) {
                Text(
                    text = "Last input: \"$voiceText\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Voice Input Button
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Voice")
                }

                Button(
                    onClick = { musicLauncher.launch("audio/*") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(Icons.Default.MusicNote, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Music")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Your Memories",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(userState.memories) { memory ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        onClick = {
                            if ((memory.type == MemoryType.VOICE || memory.type == MemoryType.MUSIC) && memory.contentUri != null) {
                                try {
                                    audioPlayer.play(Uri.parse(memory.contentUri))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error playing audio", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (memory.type) {
                                    MemoryType.IMAGE -> Icons.Default.PhotoLibrary
                                    MemoryType.VOICE -> Icons.Default.Mic
                                    MemoryType.MUSIC -> Icons.Default.MusicNote
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = memory.type.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = memory.text ?: "Memory ${memory.id.take(5)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            if (memory.type == MemoryType.IMAGE && memory.contentUri != null) {
                                AsyncImage(
                                    model = memory.contentUri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { navController.navigate(ROUTES.ChangePassword.name) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text("Change Password")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { 
                    userViewModel.logout()
                    // Logout: navigate back to Login and clear backstack
                    navController.navigate(ROUTES.Login.name) {
                        popUpTo(0)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text("Logout")
            }
        }
    }
}

@Composable
fun MemorySummaryItem(count: Int, icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(text = count.toString(), fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}
