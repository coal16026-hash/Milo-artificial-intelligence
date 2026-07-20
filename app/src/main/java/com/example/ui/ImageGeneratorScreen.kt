package com.example.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.AppColors
import com.example.ChatViewModel
import com.example.ImageStyles
import com.example.GeneratedImageEntity
import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import android.content.Intent

private fun saveImageToDownloads(context: Context, url: String, prompt: String) {
    try {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Milo Generated Image")
            .setDescription(if (prompt.length > 50) prompt.take(50) + "..." else prompt)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "milo_${System.currentTimeMillis()}.jpg")
        downloadManager.enqueue(request)
        Toast.makeText(context, "Downloading image to Downloads folder...", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to download image: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageGeneratorScreen(
    viewModel: ChatViewModel,
    colors: AppColors,
    onBack: () -> Unit
) {
    var prompt by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var selectedHistoryImage by remember { mutableStateOf<GeneratedImageEntity?>(null) }
    
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    var generationMode by remember { mutableStateOf("Generate") }
    var sourceImageUri by remember { mutableStateOf<String?>(null) }
    
    var selectedStyle by remember { mutableStateOf("Default") }
    var selectedSize by remember { mutableStateOf("1024x1024") }
    
    val recentImages by viewModel.recentImages.collectAsState()
    
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                sourceImageUri = uri.toString()
            }
        }
    )

    if (showHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            title = { Text("All Generations", color = colors.aiText, fontWeight = FontWeight.Bold) },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.heightIn(max = 450.dp),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recentImages) { img ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.inputBg)
                                .border(1.dp, if (imageUrl == img.imageUrl) colors.primary else colors.borderGray, RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedHistoryImage = img
                                    showHistoryDialog = false
                                }
                        ) {
                            AsyncImage(
                                model = img.imageUrl,
                                contentDescription = "History image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .background(Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                    ))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = img.prompt,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHistoryDialog = false }) {
                    Text("Close", color = colors.primary)
                }
            },
            containerColor = colors.plusBg
        )
    }

    // Detail dialog for viewing, saving, sharing, and copying a history image
    selectedHistoryImage?.let { img ->
        val formattedTime = remember(img.timestamp) {
            try {
                val sdf = java.text.SimpleDateFormat("MMM dd, yyyy - hh:mm a", java.util.Locale.getDefault())
                sdf.format(java.util.Date(img.timestamp))
            } catch (e: Exception) {
                "Unknown Date"
            }
        }
        AlertDialog(
            onDismissRequest = { selectedHistoryImage = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Generation Details", color = colors.aiText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { selectedHistoryImage = null }) {
                        Icon(Icons.Outlined.Close, contentDescription = "Close", tint = colors.aiText)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(if (img.size == "1024x768") 4f/3f else if (img.size == "768x1024") 3f/4f else 1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.inputBg)
                            .border(1.dp, colors.borderGray, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = img.imageUrl,
                            contentDescription = "History Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Style: ${img.style}", color = colors.aiText, fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = colors.inputBg)
                        )
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Size: ${img.size}", color = colors.aiText, fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = colors.inputBg)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.AccessTime,
                            contentDescription = "Time",
                            tint = colors.textGray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Generated on $formattedTime",
                            color = colors.textGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("PROMPT", color = colors.textGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.inputBg)
                            .border(1.dp, colors.borderGray, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(img.prompt, color = colors.aiText, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(img.prompt))
                                        Toast.makeText(context, "Prompt copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Outlined.ContentCopy, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy", color = colors.primary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Button(
                        onClick = {
                            prompt = img.prompt
                            selectedStyle = img.style
                            selectedSize = img.size
                            imageUrl = img.imageUrl
                            selectedHistoryImage = null
                            Toast.makeText(context, "Loaded into generator", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reuse Settings", color = colors.onPrimary, fontSize = 14.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = {
                            sourceImageUri = img.imageUrl
                            generationMode = "Edit"
                            selectedHistoryImage = null
                            Toast.makeText(context, "Set as Base Image for Editing", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.inputBg)
                    ) {
                        Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null, tint = colors.aiText, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Use as Base for Edit", color = colors.aiText, fontSize = 14.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                saveImageToDownloads(context, img.imageUrl, img.prompt)
                            },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderGray),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.aiText)
                        ) {
                            Icon(Icons.Outlined.SaveAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save", fontSize = 14.sp)
                        }
                        
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Milo Generated Image")
                                    putExtra(Intent.EXTRA_TEXT, "Check out this image: ${img.imageUrl}")
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Image URL"))
                            },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderGray),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.aiText)
                        ) {
                            Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share", fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteGeneratedImage(img)
                        selectedHistoryImage = null
                        Toast.makeText(context, "Deleted from history", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color(0xFFE53935), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", color = Color(0xFFE53935))
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedHistoryImage = null }) {
                    Text("Close", color = colors.textGray)
                }
            },
            containerColor = colors.plusBg
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = colors.aiText)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("Generate Image", color = colors.aiText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        
        HorizontalDivider(color = colors.borderGray.copy(alpha = 0.5f))
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.inputBg)
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Generate", "Edit").forEach { mode ->
                    val isSelected = generationMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) colors.primary else Color.Transparent)
                            .clickable { generationMode = mode }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode,
                            color = if (isSelected) colors.onPrimary else colors.aiText,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(if (generationMode == "Edit") "SOURCE IMAGE" else "REFERENCE IMAGE (OPTIONAL)", color = colors.textGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, start = 4.dp))
                if (sourceImageUri != null) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        AsyncImage(
                            model = sourceImageUri,
                            contentDescription = "Source Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, colors.borderGray, RoundedCornerShape(12.dp))
                                .clickable {
                                    pickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                        )
                        IconButton(
                            onClick = { sourceImageUri = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(32.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Outlined.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.inputBg)
                            .border(1.dp, colors.borderGray, RoundedCornerShape(12.dp))
                            .clickable {
                                pickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = "Select Image", tint = colors.iconGray, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Select image", color = colors.textGray, fontSize = 14.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            
            Text("STYLE", color = colors.textGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, start = 4.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ImageStyles.styleSuffixMap.keys.toList()) { style ->
                    val isSelected = selectedStyle == style
                    Surface(
                        color = if (isSelected) colors.primary else colors.inputBg,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.clickable { selectedStyle = style },
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, colors.borderGray)
                    ) {
                        Text(
                            text = style,
                            color = if (isSelected) colors.onPrimary else colors.aiText,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("SIZE", color = colors.textGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, start = 4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ImageStyles.supportedSizes.forEach { size ->
                    val isSelected = selectedSize == size
                    Surface(
                        color = if (isSelected) colors.primary else colors.inputBg,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f).clickable { selectedSize = size },
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, colors.borderGray)
                    ) {
                        Text(
                            text = size,
                            color = if (isSelected) colors.onPrimary else colors.aiText,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "PROMPT",
                color = colors.textGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, start = 4.dp)
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.inputBg)
                    .border(1.dp, colors.borderGray, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                BasicTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    textStyle = TextStyle(color = colors.aiText, fontSize = 16.sp),
                    cursorBrush = SolidColor(colors.aiText),
                    modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 80.dp),
                    decorationBox = { innerTextField ->
                        if (prompt.isEmpty()) {
                            Text(if (generationMode == "Edit") "Describe the edit..." else "Describe the image you want to generate...", color = colors.textGray, fontSize = 16.sp)
                        }
                        innerTextField()
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            val canGenerate = if (generationMode == "Edit") {
                prompt.isNotBlank() && sourceImageUri != null
            } else {
                prompt.isNotBlank()
            }

            Button(
                onClick = {
                    if (canGenerate && !isGenerating) {
                        isGenerating = true
                        errorMessage = null
                        imageUrl = null
                        val reqUri = sourceImageUri
                        viewModel.generateImage(prompt, selectedStyle, selectedSize, reqUri) { resultUrl, error ->
                            isGenerating = false
                            if (error != null) {
                                errorMessage = error
                            } else if (resultUrl != null) {
                                imageUrl = resultUrl
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(4.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, disabledContainerColor = colors.inputBg),
                enabled = !isGenerating && canGenerate
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(color = colors.onPrimary, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = if (canGenerate) colors.onPrimary else colors.iconGray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (generationMode == "Edit") "Apply Edit" else "Generate Image", color = if (canGenerate) colors.onPrimary else colors.iconGray, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("RESULT", color = colors.textGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, start = 4.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(if (selectedSize == "1024x768") 4f/3f else if (selectedSize == "768x1024") 3f/4f else 1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.inputBg)
                    .border(1.dp, colors.borderGray, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isGenerating -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = colors.primary, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            val infiniteTransition = rememberInfiniteTransition()
                            val alpha by infiniteTransition.animateFloat(
                                initialValue = 0.4f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                )
                            )
                            Text("Generating...", color = colors.aiText, modifier = Modifier.alpha(alpha), fontSize = 16.sp)
                        }
                    }
                    errorMessage != null -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(Icons.Outlined.ErrorOutline, contentDescription = "Error", tint = Color(0xFFE53935), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(errorMessage ?: "Unknown error", color = colors.aiText, textAlign = TextAlign.Center, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            TextButton(onClick = { 
                                isGenerating = true
                                errorMessage = null
                                val reqUri = sourceImageUri
                                viewModel.generateImage(prompt, selectedStyle, selectedSize, reqUri) { resultUrl, error ->
                                    isGenerating = false
                                    if (error != null) {
                                        errorMessage = error
                                    } else if (resultUrl != null) {
                                        imageUrl = resultUrl
                                    }
                                }
                            }) {
                                Text("Try again", color = colors.primary)
                            }
                        }
                    }
                    imageUrl != null -> {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Generated image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.Image, contentDescription = "Placeholder", tint = colors.iconGray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Your image will appear here", color = colors.textGray, fontSize = 14.sp)
                        }
                    }
                }
            }
            
            if (imageUrl != null && !isGenerating) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                ) {
                    IconButton(onClick = {
                        imageUrl?.let { url ->
                            saveImageToDownloads(context, url, prompt)
                        }
                    }) {
                        Icon(Icons.Outlined.SaveAlt, contentDescription = "Save", tint = colors.iconGray)
                    }
                    IconButton(onClick = {
                        imageUrl?.let { url ->
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Milo Generated Image")
                                putExtra(Intent.EXTRA_TEXT, "Check out this image generated with Milo AI: $url")
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Image Link"))
                        }
                    }) {
                        Icon(Icons.Outlined.Share, contentDescription = "Share", tint = colors.iconGray)
                    }
                    IconButton(onClick = {
                        isGenerating = true
                        errorMessage = null
                        imageUrl = null
                        val reqUri = if (generationMode == "Edit") sourceImageUri else null
                        viewModel.generateImage(prompt, selectedStyle, selectedSize, reqUri) { resultUrl, error ->
                            isGenerating = false
                            if (error != null) {
                                errorMessage = error
                            } else if (resultUrl != null) {
                                imageUrl = resultUrl
                            }
                        }
                    }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Regenerate", tint = colors.iconGray)
                    }
                }
            }
            
            if (recentImages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, start = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("RECENT GENERATIONS", color = colors.textGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Icon(
                        Icons.Outlined.GridView,
                        contentDescription = "View all",
                        tint = colors.textGray,
                        modifier = Modifier.size(20.dp).clickable { showHistoryDialog = true }.padding(end = 4.dp)
                    )
                }
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recentImages.take(8)) { img ->
                        Box(
                            modifier = Modifier
                                .width(160.dp)
                                .height(160.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.inputBg)
                                .border(1.5.dp, if (imageUrl == img.imageUrl) colors.primary else colors.borderGray, RoundedCornerShape(14.dp))
                                .clickable {
                                    selectedHistoryImage = img
                                }
                        ) {
                            AsyncImage(
                                model = img.imageUrl,
                                contentDescription = "Recent image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            
                            // Top action buttons overlay
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.TopCenter)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                // Save/Download button
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                                        .clickable {
                                            saveImageToDownloads(context, img.imageUrl, img.prompt)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Outlined.SaveAlt,
                                        contentDescription = "Save",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            // Bottom prompt overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .background(Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                    ))
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = img.prompt,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
