package com.example.ui

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import android.widget.VideoView
import android.widget.MediaController
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.text.BasicTextField
import com.example.VideoState
import com.example.AppColors
import com.example.ChatViewModel

private fun saveVideoToDownloads(context: Context, url: String, prompt: String) {
    try {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(url)
        val request = DownloadManager.Request(uri)
            .setTitle("Milo Generated Video")
            .setDescription(if (prompt.length > 50) prompt.take(50) + "..." else prompt)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "milo_video_${System.currentTimeMillis()}.mp4")
            
        downloadManager.enqueue(request)
        Toast.makeText(context, "Direct download started. Check your notification drawer and Downloads folder.", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Download failed: ${e.message}. Copying link to clipboard as fallback.", Toast.LENGTH_LONG).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiloVideoScreen(
    viewModel: ChatViewModel,
    colors: AppColors,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val videoState by viewModel.videoState.collectAsState()

    Scaffold(
        containerColor = colors.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Milo-Video-1",
                            color = colors.aiText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            "Agnes Video V2.0 Engine",
                            color = colors.textGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.aiText
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.resetVideoState()
                            Toast.makeText(context, "Configurations reset", Toast.LENGTH_SHORT).show()
                        },
                        enabled = !videoState.isGenerating
                    ) {
                        Icon(
                            Icons.Outlined.Refresh,
                            contentDescription = "Reset Settings",
                            tint = colors.aiText
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.aiText,
                    navigationIconContentColor = colors.aiText,
                    actionIconContentColor = colors.aiText
                )
            )
        }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val isWideScreen = maxWidth >= 600.dp

            if (isWideScreen) {
                // Wide Screen: Left Pane (Controls) & Right Pane (Canvas Display)
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .width(360.dp)
                            .fillMaxHeight()
                            .background(colors.background)
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp)
                    ) {
                        VideoGenerationControls(
                            prompt = videoState.prompt,
                            onPromptChange = { viewModel.updateVideoPrompt(it) },
                            aspectRatio = videoState.aspectRatio,
                            onAspectRatioChange = { viewModel.updateVideoAspectRatio(it) },
                            duration = videoState.duration,
                            onDurationChange = { viewModel.updateVideoDuration(it) },
                            isGenerating = videoState.isGenerating,
                            onGenerate = { viewModel.generateVideo() },
                            colors = colors
                        )
                    }

                    // Divider between panels
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(colors.borderGray)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(colors.inputBg)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        VideoCanvasDisplay(
                            videoState = videoState,
                            colors = colors,
                            context = context,
                            onDownload = { url, p -> saveVideoToDownloads(context, url, p) },
                            onCopyLink = { url ->
                                clipboardManager.setText(AnnotatedString(url))
                                Toast.makeText(context, "Video URL copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            } else {
                // Mobile/Narrow Screen: Vertical scrollable layout with stacked controls and canvas
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Canvas preview is kept highly visible at the top on Mobile
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.bubbleGray),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, colors.borderGray)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            VideoCanvasDisplay(
                                videoState = videoState,
                                colors = colors,
                                context = context,
                                onDownload = { url, p -> saveVideoToDownloads(context, url, p) },
                                onCopyLink = { url ->
                                    clipboardManager.setText(AnnotatedString(url))
                                    Toast.makeText(context, "Video URL copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }

                    // Settings & Actions below the canvas
                    VideoGenerationControls(
                        prompt = videoState.prompt,
                        onPromptChange = { viewModel.updateVideoPrompt(it) },
                        aspectRatio = videoState.aspectRatio,
                        onAspectRatioChange = { viewModel.updateVideoAspectRatio(it) },
                        duration = videoState.duration,
                        onDurationChange = { viewModel.updateVideoDuration(it) },
                        isGenerating = videoState.isGenerating,
                        onGenerate = { viewModel.generateVideo() },
                        colors = colors
                    )
                }
            }
        }
    }
}

@Composable
fun VideoGenerationControls(
    prompt: String,
    onPromptChange: (String) -> Unit,
    aspectRatio: String,
    onAspectRatioChange: (String) -> Unit,
    duration: String,
    onDurationChange: (String) -> Unit,
    isGenerating: Boolean,
    onGenerate: () -> Unit,
    colors: AppColors
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Prompt Textbox
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "PROMPT",
                color = colors.textGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.inputBg)
                    .border(1.dp, colors.borderGray, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                BasicTextField(
                    value = prompt,
                    onValueChange = onPromptChange,
                    textStyle = TextStyle(color = colors.aiText, fontSize = 14.sp),
                    cursorBrush = SolidColor(colors.aiText),
                    enabled = !isGenerating,
                    decorationBox = { innerTextField ->
                        if (prompt.isEmpty()) {
                            Text(
                                "Describe what happens in the video (e.g., 'A majestic eagle flying over snow-covered peaks, 4k cinematic style')...",
                                color = colors.textGray,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Aspect Ratio Selector
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "ASPECT RATIO",
                color = colors.textGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val ratios = listOf(
                    Triple("Landscape (16:9)", "16:9", Icons.Outlined.CropLandscape),
                    Triple("Portrait (9:16)", "9:16", Icons.Outlined.CropPortrait),
                    Triple("Square (1:1)", "1:1", Icons.Outlined.CropSquare)
                )
                ratios.forEach { (name, label, icon) ->
                    val isSelected = aspectRatio == name
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) colors.primary else colors.inputBg)
                            .border(
                                1.dp,
                                if (isSelected) colors.primary else colors.borderGray,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable(enabled = !isGenerating) { onAspectRatioChange(name) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                icon,
                                contentDescription = name,
                                tint = if (isSelected) colors.onPrimary else colors.aiText,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                label,
                                color = if (isSelected) colors.onPrimary else colors.aiText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Duration Selector
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "DURATION",
                color = colors.textGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val durations = listOf("5 Seconds", "10 Seconds")
                durations.forEach { d ->
                    val isSelected = duration == d
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) colors.primary else colors.inputBg)
                            .border(
                                1.dp,
                                if (isSelected) colors.primary else colors.borderGray,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable(enabled = !isGenerating) { onDurationChange(d) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            d,
                            color = if (isSelected) colors.onPrimary else colors.aiText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Action Button
        Button(
            onClick = onGenerate,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            ),
            enabled = !isGenerating && prompt.isNotBlank()
        ) {
            if (isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = colors.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    Icons.Outlined.VideoSettings,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Generate Video",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun VideoCanvasDisplay(
    videoState: VideoState,
    colors: AppColors,
    context: Context,
    onDownload: (String, String) -> Unit,
    onCopyLink: (String) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsingGlow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    when {
        videoState.isGenerating -> {
            // Loading and step progress state
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(72.dp),
                        color = colors.primary,
                        strokeWidth = 4.dp
                    )
                    Icon(
                        Icons.Outlined.Videocam,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier
                            .size(32.dp)
                            .alpha(pulseAlpha)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = videoState.statusText,
                    color = colors.aiText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Creating frames on Milo-gen-1 via Milo AI Hub. Do not close this panel.",
                    color = colors.textGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
        videoState.errorMessage != null -> {
            // Error State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Outlined.ErrorOutline,
                    contentDescription = "Error",
                    tint = Color(0xFFFF6B6B),
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Video Generation Failed",
                    color = colors.aiText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    videoState.errorMessage,
                    color = Color(0xFFFF8B8B),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        videoState.videoUrl != null -> {
            // Success & Playback State
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Video Player Container
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                        .border(1.dp, colors.borderGray, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                val mediaController = MediaController(ctx)
                                mediaController.setAnchorView(this)
                                setMediaController(mediaController)
                                setVideoURI(Uri.parse(videoState.videoUrl))
                                setOnPreparedListener { mp ->
                                    mp.isLooping = true
                                    start()
                                }
                            }
                        },
                        update = { videoView ->
                            videoView.setVideoURI(Uri.parse(videoState.videoUrl))
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Action buttons under player
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onDownload(videoState.videoUrl, videoState.prompt) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary
                        )
                    ) {
                        Icon(
                            Icons.Outlined.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download Video", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { onCopyLink(videoState.videoUrl) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, colors.borderGray),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colors.aiText
                        )
                    ) {
                        Icon(
                            Icons.Outlined.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Link", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        else -> {
            // Default Empty / Placeholder State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Outlined.MovieFilter,
                    contentDescription = null,
                    tint = colors.textGray,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Milo-Video-1 Canvas",
                    color = colors.aiText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Enter a prompt and configure settings on the panel, then click Generate to start rendering your high-fidelity cinematic video clip.",
                    color = colors.textGray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }
        }
    }
}
