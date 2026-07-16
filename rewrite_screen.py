with open("app/src/main/java/com/example/ui/ImageGeneratorScreen.kt", "w") as f:
    f.write("""package com.example.ui

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
            title = { Text("All Generations", color = colors.aiText) },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.heightIn(max = 400.dp),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentImages) { img ->
                        AsyncImage(
                            model = img.imageUrl,
                            contentDescription = "History image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.inputBg)
                                .border(1.dp, if (imageUrl == img.imageUrl) colors.primary else colors.borderGray, RoundedCornerShape(8.dp))
                                .clickable {
                                    imageUrl = img.imageUrl
                                    prompt = img.prompt
                                    selectedStyle = img.style
                                    selectedSize = img.size
                                    errorMessage = null
                                    showHistoryDialog = false
                                }
                        )
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
            
            if (generationMode == "Edit") {
                Text("SOURCE IMAGE", color = colors.textGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, start = 4.dp))
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
                            Text("Select source image", color = colors.textGray, fontSize = 14.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            
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
                        val reqUri = if (generationMode == "Edit") sourceImageUri else null
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
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.SaveAlt, contentDescription = "Save", tint = colors.iconGray)
                    }
                    IconButton(onClick = {}) {
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentImages.take(5)) { img ->
                        AsyncImage(
                            model = img.imageUrl,
                            contentDescription = "Recent image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.inputBg)
                                .border(1.dp, if (imageUrl == img.imageUrl) colors.primary else colors.borderGray, RoundedCornerShape(8.dp))
                                .clickable {
                                    imageUrl = img.imageUrl
                                    prompt = img.prompt
                                    selectedStyle = img.style
                                    selectedSize = img.size
                                    errorMessage = null
                                }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
""")
