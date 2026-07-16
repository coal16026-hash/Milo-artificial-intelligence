package com.example

import com.example.ui.ImageGeneratorScreen
import com.example.ui.MiloVideoScreen


import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.*
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ChatScreen()
            }
        }
    }
}

data class AppColors(
    val background: Color,
    val inputBg: Color,
    val bubbleGray: Color,
    val userBubble: Color,
    val aiText: Color,
    val textGray: Color,
    val iconGray: Color,
    val plusBg: Color,
    val borderGray: Color,
    val primary: Color,
    val onPrimary: Color,
    val isDark: Boolean
)

@Composable
fun getAppColors(themeSetting: String): AppColors {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeSetting) {
        "Pure Black", "Dark Gray", "Charcoal", "True Gray" -> true
        "Soft White" -> false
        "Follow System" -> isSystemDark
        else -> isSystemDark
    }
    
    val bg = when (themeSetting) {
        "Pure Black" -> Color(0xFF000000)
        "Dark Gray" -> Color(0xFF1C1C1E)
        "Charcoal" -> Color(0xFF161618)
        "True Gray" -> Color(0xFF3A3A3C)
        "Soft White" -> Color(0xFFFFFFFF)
        "Follow System" -> if (isSystemDark) Color(0xFF000000) else Color(0xFFFFFFFF)
        else -> if (isSystemDark) Color(0xFF000000) else Color(0xFFFFFFFF)
    }

    val bubbleGray = when (themeSetting) {
        "Pure Black" -> Color(0xFF171717)
        "Dark Gray" -> Color(0xFF2C2C2E)
        "Charcoal" -> Color(0xFF232326)
        "True Gray" -> Color(0xFF48484A)
        "Soft White" -> Color(0xFFF2F2F2)
        "Follow System" -> if (isSystemDark) Color(0xFF171717) else Color(0xFFF2F2F2)
        else -> if (isSystemDark) Color(0xFF171717) else Color(0xFFF2F2F2)
    }

    val userBubble = when (themeSetting) {
        "Pure Black" -> Color(0xFF3A3A3A)
        "Dark Gray" -> Color(0xFF48484A)
        "Charcoal" -> Color(0xFF3A3A3D)
        "True Gray" -> Color(0xFF5A5A5D)
        "Soft White" -> Color(0xFFE8E8E8)
        "Follow System" -> if (isSystemDark) Color(0xFF3A3A3A) else Color(0xFFE8E8E8)
        else -> if (isSystemDark) Color(0xFF3A3A3A) else Color(0xFFE8E8E8)
    }

    val aiText = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0A0A0A)
    
    val primary = if (isDark) Color(0xFFEDEDED) else Color(0xFF111111)
    val onPrimary = if (isDark) Color(0xFF0A0A0A) else Color(0xFFFFFFFF)
    val inputBg = if (isDark) Color(0xFF212121) else Color(0xFFE5E5EA)
    val textGray = if (isDark) Color(0xFFB4B4B4) else Color(0xFF6B6B6B)
    val iconGray = if (isDark) Color(0xFF8E8E8E) else Color(0xFF71717A)
    val plusBg = if (isDark) Color(0xFF171717) else Color(0xFFD4D4D8)
    val borderGray = if (isDark) Color(0xFF2A2A2A) else Color(0xFFD1D5DB)

    return AppColors(bg, inputBg, bubbleGray, userBubble, aiText, textGray, iconGray, plusBg, borderGray, primary, onPrimary, isDark)
}

@Composable
fun AvatarIcon(avatar: String, colors: AppColors, modifier: Modifier = Modifier) {
    if (avatar.startsWith("content://") || avatar.startsWith("file://") || avatar.contains("/")) {
        coil.compose.AsyncImage(
            model = avatar,
            contentDescription = "User Avatar",
            modifier = modifier.clip(CircleShape),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    } else {
        when (avatar) {
            "robot" -> Icon(Icons.Outlined.SmartToy, null, tint = colors.aiText, modifier = modifier)
            "star" -> Icon(Icons.Outlined.Star, null, tint = colors.aiText, modifier = modifier)
            "heart" -> Icon(Icons.Outlined.Favorite, null, tint = colors.aiText, modifier = modifier)
            "bolt" -> Icon(Icons.Outlined.Bolt, null, tint = colors.aiText, modifier = modifier)
            "face" -> Icon(Icons.Outlined.Face, null, tint = colors.aiText, modifier = modifier)
            else -> Icon(Icons.Outlined.AccountCircle, null, tint = colors.aiText, modifier = modifier)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val viewModel: ChatViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return ChatViewModel(context.applicationContext) as T
            }
        }
    )
    val state by viewModel.state.collectAsState()
    val theme by viewModel.theme.collectAsState()
    val textSize by viewModel.textSize.collectAsState()
    val hapticEnabled by viewModel.hapticFeedback.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val userAvatar by viewModel.userAvatar.collectAsState()
    val colors = getAppColors(theme)

    // Sync with Firebase Auth state at startup
    LaunchedEffect(Unit) {
        try {
            val firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance()
            viewModel.updateWithFirebaseUser(firebaseAuth.currentUser)
        } catch (e: Exception) {
            // Firebase not initialized, keep local state
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var showSettings by remember { mutableStateOf(false) }
    var showSignIn by remember { mutableStateOf(false) }
    var showImageGenerator by remember { mutableStateOf(false) }
    var showVideoGenerator by remember { mutableStateOf(false) }
    var attachedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showInlineGenerateDialog by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current

    // Speech-to-text launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                viewModel.onInputTextChanged(matches[0])
            }
        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            attachedImageUri = uri
            Toast.makeText(context, "Gallery image attached", Toast.LENGTH_SHORT).show()
        }
    }

    // Document picker launcher
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            attachedImageUri = uri
            Toast.makeText(context, "Document attached", Toast.LENGTH_SHORT).show()
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            try {
                val file = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                attachedImageUri = Uri.fromFile(file)
                Toast.makeText(context, "Camera photo attached", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to capture photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val isAtBottom = !listState.canScrollForward
    var userScrolledUp by remember { mutableStateOf(false) }
    LaunchedEffect(isAtBottom) {
        if (!isAtBottom) {
            userScrolledUp = true
        } else {
            userScrolledUp = false
        }
    }

    // Auto-scroll to bottom on new messages if not manually scrolled up
    LaunchedEffect(state.messages.size, state.isGenerating) {
        if (state.messages.isNotEmpty() && !userScrolledUp) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ModalNavigationDrawer(
            modifier = Modifier.imePadding(),
            drawerState = drawerState,
            drawerContent = {
                SidebarContent(
                    conversations = viewModel.conversations.collectAsState().value,
                    colors = colors,
                    viewModel = viewModel,
                    onNewChat = {
                        if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.clearChat()
                        scope.launch { drawerState.close() }
                    },
                    onSettingsClick = {
                        if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showSettings = true
                        scope.launch { drawerState.close() }
                    },
                    onImageGeneratorClick = {
                        if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showImageGenerator = true
                        scope.launch { drawerState.close() }
                    },
                    onVideoGeneratorClick = {
                        if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showVideoGenerator = true
                        scope.launch { drawerState.close() }
                    },
                    onLoadChat = { id ->
                        if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.loadChat(id)
                        scope.launch { drawerState.close() }
                    },
                    onDeleteChat = { id ->
                        if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.deleteConversation(id)
                    },
                    onRenameChat = { id, newTitle ->
                        if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.renameConversation(id, newTitle)
                    },
                    onPinChat = { id, pinned ->
                        if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.togglePin(id, pinned)
                    }
                )
            },
            scrimColor = Color.Black.copy(alpha = 0.5f)
        ) {
            Scaffold(
                containerColor = colors.background,
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                "Milo AI",
                                color = colors.aiText,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp,
                                letterSpacing = (-0.5).sp
                            )
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    scope.launch { drawerState.open() }
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Outlined.Menu, contentDescription = "Open navigation drawer", tint = colors.aiText)
                            }
                        },
                        actions = {
                            var menuExpanded by remember { mutableStateOf(false) }
                            var showSearchDialog by remember { mutableStateOf(false) }
                            var showArtifactsDialog by remember { mutableStateOf(false) }
                            var selectedArtifact by remember { mutableStateOf<Triple<String, Message, MessagePart>?>(null) }
                            var searchQuery by remember { mutableStateOf("") }
                            val context = LocalContext.current

                            // New Chat button
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(colors.bubbleGray)
                                    .clickable {
                                        if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.clearChat()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ChatBubbleOutline,
                                    contentDescription = "New chat",
                                    tint = colors.aiText,
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp)
                                        .size(20.dp)
                                )
                            }

                            // Avatar button
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colors.bubbleGray)
                                    .clickable { 
                                        if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showSettings = true 
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                AvatarIcon(userAvatar, colors, Modifier.size(20.dp))
                            }

                            // Quick access dropdown menu button
                            Box(modifier = Modifier.padding(end = 12.dp)) {
                                Box(
                                    modifier = Modifier
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(colors.bubbleGray)
                                        .clickable {
                                            if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            menuExpanded = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.MoreVert,
                                        contentDescription = "Chat options",
                                        tint = colors.aiText,
                                        modifier = Modifier
                                            .padding(horizontal = 12.dp)
                                            .size(20.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false },
                                    modifier = Modifier
                                        .background(colors.plusBg)
                                        .border(1.dp, colors.borderGray, RoundedCornerShape(16.dp))
                                        .widthIn(min = 220.dp)
                                        .padding(4.dp)
                                ) {
                                    
                                    DropdownMenuItem(
                                        text = { Text("Pin Chat", color = colors.aiText, fontSize = 14.sp) },
                                        leadingIcon = {
                                            Icon(Icons.Outlined.PushPin, contentDescription = null, tint = colors.textGray, modifier = Modifier.size(18.dp))
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            android.widget.Toast.makeText(context, "Chat pinned", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = { Text("Artifacts", color = colors.aiText, fontSize = 14.sp) },
                                        leadingIcon = {
                                            Icon(Icons.Outlined.Widgets, contentDescription = null, tint = Color(0xFF81C995), modifier = Modifier.size(18.dp))
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            showArtifactsDialog = true
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = { Text("Find in Chat", color = colors.aiText, fontSize = 14.sp) },
                                        leadingIcon = {
                                            Icon(Icons.Outlined.Search, contentDescription = null, tint = colors.textGray, modifier = Modifier.size(18.dp))
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            showSearchDialog = true
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = { Text("Delete Chat", color = Color(0xFFFF6B6B), fontSize = 14.sp) },
                                        leadingIcon = {
                                            Icon(Icons.Outlined.Delete, contentDescription = null, tint = Color(0xFFFF6B6B), modifier = Modifier.size(18.dp))
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            viewModel.clearChat()
                                            android.widget.Toast.makeText(context, "Chat deleted", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }

                            if (showSearchDialog) {
                                androidx.compose.material3.AlertDialog(
                                    onDismissRequest = { showSearchDialog = false },
                                    containerColor = colors.plusBg,
                                    modifier = Modifier.shadow(8.dp, shape = AlertDialogDefaults.shape),
                                    title = { Text("Find in Chat", color = colors.aiText) },
                                    text = {
                                        Column {
                                            OutlinedTextField(
                                                value = searchQuery,
                                                onValueChange = { searchQuery = it },
                                                placeholder = { Text("Search messages...", color = colors.textGray) },
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = colors.aiText,
                                                    unfocusedTextColor = colors.aiText,
                                                    focusedBorderColor = colors.aiText,
                                                    unfocusedBorderColor = colors.borderGray
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            val matchingMessages = state.messages.filter { it.text.contains(searchQuery, ignoreCase = true) }
                                            LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                                                items(matchingMessages) { msg ->
                                                    Text(
                                                        text = "${if (msg.isUser) "You" else "Milo"}: ${msg.text}",
                                                        color = colors.textGray,
                                                        fontSize = 13.sp,
                                                        modifier = Modifier.padding(vertical = 4.dp),
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        TextButton(onClick = { showSearchDialog = false }) {
                                            Text("Close", color = colors.aiText)
                                        }
                                    }
                                )
                            }

                            if (showArtifactsDialog) {
                                val artifacts = state.messages.flatMap { msg ->
                                    parseMessageParts(msg.text)
                                        .filter { it is MessagePart.Code || it is MessagePart.Formula }
                                        .map { part -> Triple(msg.id, msg, part) }
                                }

                                ModalBottomSheet(
                                    onDismissRequest = { showArtifactsDialog = false },
                                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                                    containerColor = colors.plusBg,
                                    contentColor = colors.aiText,
                                    tonalElevation = 8.dp,
                                    dragHandle = { BottomSheetDefaults.DragHandle(color = colors.textGray) }
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(0.85f)
                                            .padding(bottom = 16.dp)
                                    ) {
                                        // Header row
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 18.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Chat Artifacts (${artifacts.size})",
                                                style = androidx.compose.ui.text.TextStyle(
                                                    color = colors.aiText,
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                            IconButton(onClick = { showArtifactsDialog = false }) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Close,
                                                    contentDescription = "Close gallery",
                                                    tint = colors.aiText
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        if (artifacts.isEmpty()) {
                                            // Empty state centered layout
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .weight(1f),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Widgets,
                                                    contentDescription = "No artifacts",
                                                    tint = colors.textGray.copy(alpha = 0.3f),
                                                    modifier = Modifier.size(72.dp)
                                                )
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Text(
                                                    text = "No code or LaTeX formulas found in this chat.",
                                                    color = colors.textGray,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        } else {
                                            // 2-Column Grid
                                            LazyVerticalGrid(
                                                columns = GridCells.Fixed(2),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .weight(1f)
                                                    .padding(horizontal = 16.dp),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                                contentPadding = PaddingValues(bottom = 16.dp)
                                            ) {
                                                items(artifacts) { (msgId, message, part) ->
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(170.dp)
                                                            .clip(RoundedCornerShape(16.dp))
                                                            .background(colors.bubbleGray)
                                                            .border(1.dp, colors.borderGray, RoundedCornerShape(16.dp))
                                                            .clickable {
                                                                if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                selectedArtifact = Triple(msgId, message, part)
                                                            }
                                                            .padding(12.dp)
                                                    ) {
                                                        Column(modifier = Modifier.fillMaxSize()) {
                                                            // Header Row: Type Icon & Badge
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                                    val typeIcon = if (part is MessagePart.Code) Icons.Outlined.Code else Icons.Outlined.Functions
                                                                    val iconColor = if (part is MessagePart.Code) Color(0xFF8AB4F8) else Color(0xFF81C995)
                                                                    Icon(
                                                                        imageVector = typeIcon,
                                                                        contentDescription = null,
                                                                        tint = iconColor,
                                                                        modifier = Modifier.size(16.dp)
                                                                    )
                                                                }
                                                                
                                                                val badgeText = if (part is MessagePart.Code) part.language.ifEmpty { "code" } else "latex"
                                                                Box(
                                                                    modifier = Modifier
                                                                        .clip(RoundedCornerShape(4.dp))
                                                                        .background(colors.inputBg)
                                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                                ) {
                                                                    Text(
                                                                        text = badgeText.lowercase(),
                                                                        color = colors.textGray,
                                                                        fontSize = 9.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                                                    )
                                                                }
                                                            }

                                                            Spacer(modifier = Modifier.height(8.dp))

                                                            // Preview Container with fade-out
                                                            Box(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .weight(1f)
                                                                    .clip(RoundedCornerShape(8.dp))
                                                                    .background(colors.inputBg)
                                                                    .padding(6.dp)
                                                            ) {
                                                                when (part) {
                                                                    is MessagePart.Code -> {
                                                                        val previewLines = part.code.lines().take(5).joinToString("\n")
                                                                        val highlighted = highlightCode(previewLines)
                                                                        Text(
                                                                            text = highlighted,
                                                                            fontSize = 10.sp,
                                                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                                            lineHeight = 13.sp,
                                                                            color = colors.aiText,
                                                                            maxLines = 5,
                                                                            overflow = TextOverflow.Ellipsis,
                                                                            modifier = Modifier.fillMaxSize()
                                                                        )
                                                                    }
                                                                    is MessagePart.Formula -> {
                                                                        val cleanFormula = part.formula
                                                                            .removePrefix("\\[").removeSuffix("\\]")
                                                                            .removePrefix("\\(").removeSuffix("\\)")
                                                                            .trim()
                                                                        val isDark = colors.isDark
                                                                        val textColorHex = if (isDark) "#E8EAED" else "#1F1F1F"
                                                                        val miniHtml = remember(cleanFormula, isDark) {
                                                                            """
                                                                            <!DOCTYPE html>
                                                                            <html>
                                                                            <head>
                                                                                <meta charset="utf-8">
                                                                                <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.css">
                                                                                <script defer src="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.js"></script>
                                                                                <style>
                                                                                    body {
                                                                                        background-color: transparent;
                                                                                        color: $textColorHex;
                                                                                        font-family: sans-serif;
                                                                                        display: flex;
                                                                                        justify-content: center;
                                                                                        align-items: center;
                                                                                        height: 100vh;
                                                                                        margin: 0;
                                                                                        padding: 0;
                                                                                        overflow: hidden;
                                                                                    }
                                                                                    .formula-container {
                                                                                        font-size: 0.9em;
                                                                                        text-align: center;
                                                                                    }
                                                                                </style>
                                                                            </head>
                                                                            <body>
                                                                                <div class="formula-container" id="math"></div>
                                                                                <script>
                                                                                    window.onload = function() {
                                                                                        try {
                                                                                            katex.render(String.raw`${cleanFormula.replace("\\", "\\\\")}`, document.getElementById("math"), {
                                                                                                displayMode: true,
                                                                                                throwOnError: false
                                                                                            });
                                                                                        } catch (e) {
                                                                                            document.getElementById("math").innerText = `${cleanFormula}`;
                                                                                        }
                                                                                    };
                                                                                </script>
                                                                            </body>
                                                                            </html>
                                                                            """.trimIndent()
                                                                        }
                                                                        AndroidView(
                                                                            factory = { ctx ->
                                                                                android.webkit.WebView(ctx).apply {
                                                                                    settings.javaScriptEnabled = true
                                                                                    setBackgroundColor(0) // Transparent background
                                                                                    isVerticalScrollBarEnabled = false
                                                                                    isHorizontalScrollBarEnabled = false
                                                                                    setOnTouchListener { _, _ -> true }
                                                                                    loadDataWithBaseURL(null, miniHtml, "text/html", "UTF-8", null)
                                                                                }
                                                                            },
                                                                            update = { webView ->
                                                                                webView.loadDataWithBaseURL(null, miniHtml, "text/html", "UTF-8", null)
                                                                            },
                                                                            modifier = Modifier.fillMaxSize()
                                                                        )
                                                                    }
                                                                    else -> {}
                                                                }

                                                                // Linear Gradient Overlay for beautiful fadeout
                                                                Box(
                                                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .height(20.dp)
                                                                        .align(Alignment.BottomCenter)
                                                                        .background(
                                                                            Brush.verticalGradient(
                                                                                colors = listOf(Color.Transparent, colors.inputBg)
                                                                            )
                                                                        )
                                                                )
                                                            }

                                                            Spacer(modifier = Modifier.height(6.dp))

                                                            // Message Context Caption
                                                            val messageWords = message.text.split("\\s+".toRegex()).filter { it.isNotBlank() }
                                                            val firstFewWords = messageWords.take(3).joinToString(" ")
                                                            val contextCaption = "from: $firstFewWords..."
                                                            Text(
                                                                text = contextCaption,
                                                                color = colors.textGray,
                                                                fontSize = 11.sp,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis,
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Detail View Dialog for Selected Artifact
                            selectedArtifact?.let { (msgId, message, part) ->
                                androidx.compose.material3.AlertDialog(
                                    onDismissRequest = { selectedArtifact = null },
                                    containerColor = colors.plusBg,
                                    modifier = Modifier.shadow(8.dp, shape = AlertDialogDefaults.shape).fillMaxWidth(0.92f),
                                    title = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (part is MessagePart.Code) Icons.Outlined.Code else Icons.Outlined.Functions,
                                                contentDescription = null,
                                                tint = if (part is MessagePart.Code) Color(0xFF8AB4F8) else Color(0xFF81C995),
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Text(
                                                text = if (part is MessagePart.Code) "Code Artifact" else "LaTeX Formula",
                                                color = colors.aiText,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                        }
                                    },
                                    text = {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 450.dp)
                                                .verticalScroll(rememberScrollState()),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            when (part) {
                                                is MessagePart.Code -> {
                                                    CodeBlockCard(
                                                        language = part.language,
                                                        code = part.code,
                                                        colors = colors
                                                    )
                                                }
                                                is MessagePart.Formula -> {
                                                    FormulaCard(
                                                        formula = part.formula,
                                                        colors = colors
                                                    )
                                                }
                                                else -> {}
                                            }

                                            // Context Information
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(colors.bubbleGray)
                                                    .padding(12.dp)
                                            ) {
                                                Text(
                                                    text = "Source Message Context",
                                                    color = colors.textGray,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = message.text.take(200) + (if (message.text.length > 200) "..." else ""),
                                                    color = colors.aiText,
                                                    fontSize = 13.sp,
                                                    lineHeight = 18.sp
                                                )
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                selectedArtifact = null
                                                showArtifactsDialog = false
                                                scope.launch {
                                                    val reversedMessages = state.messages.reversed()
                                                    val reversedIndex = reversedMessages.indexOfFirst { it.id == msgId }
                                                    if (reversedIndex != -1) {
                                                        val targetIndex = if (state.isGenerating) reversedIndex + 1 else reversedIndex
                                                        listState.animateScrollToItem(targetIndex)
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.ArrowDownward,
                                                contentDescription = "Jump to Message",
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Jump to message", fontSize = 13.sp)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { selectedArtifact = null }) {
                                            Text("Close", color = colors.aiText)
                                        }
                                    }
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
                },
                bottomBar = {
                    Box(modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(bottom = 12.dp, top = 4.dp)
                    ) {
                        InputBar(
                            inputText = state.inputText,
                            isGenerating = state.isGenerating,
                            colors = colors,
                            attachedImageUri = attachedImageUri,
                            selectedModel = selectedModel,
                            onTextChange = { viewModel.onInputTextChanged(it) },
                            onSend = {
                                if (state.inputText.isNotBlank() || attachedImageUri != null) {
                                    if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.sendMessage(attachedImageUri?.toString())
                                    attachedImageUri = null
                                }
                            },
                            onStop = {
                                if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.stopGeneration()
                            },
                            onMicClick = {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                }
                                try {
                                    speechLauncher.launch(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Speech-to-text not available", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onGalleryClick = {
                                imagePickerLauncher.launch("image/*")
                            },
                            onCameraClick = {
                                cameraLauncher.launch(null)
                            },
                            onDocumentClick = {
                                documentPickerLauncher.launch("*/*")
                            },
                            onGenerateImageClick = { showInlineGenerateDialog = true },
                            onModelSelected = { model, code -> viewModel.setSelectedModel(model, code) }
                        )
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .clickable(
                            indication = null,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        ) {
                            keyboardController?.hide()
                        }
                ) {
                    if (state.messages.isEmpty()) {
                        // Onboarding / Empty state with prompt chips
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("How can Milo help you today?", color = colors.aiText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Ask questions, draft text, or brainstorm ideas.", color = colors.textGray, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            // Suggested prompt chips
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth(0.9f)) {
                                state.suggestions.forEach { suggestion ->
                                    OutlinedButton(
                                        onClick = {
                                            if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.onInputTextChanged(suggestion)
                                            viewModel.sendMessage()
                                        },
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                        shape = CircleShape,
                                        colors = ButtonDefaults.outlinedButtonColors(containerColor = colors.inputBg),
                                        border = BorderStroke(1.dp, colors.borderGray)
                                    ) {
                                        Text(suggestion, color = colors.aiText, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                            reverseLayout = true
                        ) {
                            if (state.isGenerating) {
                                item {
                                    GeneratingIndicator(
                                        colors = colors,
                                        agentStatus = state.agentStatus,
                                        agentLogs = state.agentLogs
                                    )
                                }
                            }

                            val reversedMessages = state.messages.reversed()
                            itemsIndexed(reversedMessages, key = { _, msg -> msg.id }) { reversedIndex, msg ->
                                val originalIndex = state.messages.lastIndex - reversedIndex
                                val isLatest = originalIndex == state.messages.lastIndex
                                val showSuggestions = isLatest && !msg.isUser && !state.isGenerating && state.inputText.isEmpty()
                                
                                MessageBubble(
                                    message = msg,
                                    colors = colors,
                                    textSize = textSize,
                                    showSuggestions = showSuggestions,
                                    suggestions = state.suggestions,
                                    onSuggestionClick = { 
                                        if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.onInputTextChanged(it)
                                        viewModel.sendMessage()
                                    },
                                    onCopy = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Copied Text", msg.text)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    onRegenerate = { 
                                        if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.regenerateResponse(originalIndex) 
                                    },
                                    isError = isLatest && state.errorMessage != null,
                                    errorMessage = state.errorMessage,
                                    onRetry = { 
                                        if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.retryLastMessage() 
                                    },
                                    onEditUserMessage = { text ->
                                        viewModel.editUserMessage(originalIndex, text)
                                    },
                                    allConversationMessages = state.allConversationMessages,
                                    onSetBranch = { parentId, idx ->
                                        viewModel.setBranch(parentId, idx)
                                    }
                                )
                            }
                        }

                        // Scroll to bottom pill when user scrolled up
                        if (userScrolledUp) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 16.dp)
                            ) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            listState.animateScrollToItem(0)
                                            userScrolledUp = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.bubbleGray),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Icon(Icons.Outlined.ArrowDownward, contentDescription = "Scroll to bottom", tint = colors.aiText, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("New messages", color = colors.aiText, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }


        if (showInlineGenerateDialog) {
            var generatePrompt by remember { mutableStateOf("") }
            var isGenerating by remember { mutableStateOf(false) }
            var genError by remember { mutableStateOf<String?>(null) }
            
            AlertDialog(
                onDismissRequest = { if (!isGenerating) showInlineGenerateDialog = false },
                title = { Text("Generate Image", color = colors.aiText) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = generatePrompt,
                            onValueChange = { generatePrompt = it },
                            placeholder = { Text("Describe the image...") },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            textStyle = TextStyle(color = colors.aiText),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.borderGray,
                                cursorColor = colors.primary
                            ),
                            enabled = !isGenerating
                        )
                        if (genError != null) {
                            Text(genError ?: "", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (generatePrompt.isNotBlank() && !isGenerating) {
                                isGenerating = true
                                genError = null
                                viewModel.generateImage(generatePrompt, "Default", "1024x1024", null) { resultUrl, error ->
                                    isGenerating = false
                                    if (error != null) {
                                        genError = error
                                    } else if (resultUrl != null) {
                                        attachedImageUri = Uri.parse(resultUrl)
                                        showInlineGenerateDialog = false
                                    }
                                }
                            }
                        },
                        enabled = !isGenerating && generatePrompt.isNotBlank()
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = colors.primary, strokeWidth = 2.dp)
                        } else {
                            Text("Generate", color = colors.primary)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showInlineGenerateDialog = false }, enabled = !isGenerating) {
                        Text("Cancel", color = colors.textGray)
                    }
                },
                containerColor = colors.plusBg
            )
        }

        // Settings Screen with slide + fade transition
        AnimatedVisibility(
            visible = showSettings && !showSignIn && !showImageGenerator && !showVideoGenerator,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            SettingsScreen(
                viewModel = viewModel,
                colors = colors,
                onBack = { showSettings = false },
                onOpenSignIn = { showSignIn = true },
                onClearAll = { viewModel.clearAllConversations() }
            )
        }
        
        // Image Generator Screen with slide + fade transition
        AnimatedVisibility(
            visible = showImageGenerator && !showSignIn,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            ImageGeneratorScreen(
                viewModel = viewModel,
                colors = colors,
                onBack = { showImageGenerator = false }
            )
        }

        // Milo Video Generator Screen with slide + fade transition
        AnimatedVisibility(
            visible = showVideoGenerator && !showSignIn,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            MiloVideoScreen(
                viewModel = viewModel,
                colors = colors,
                onBack = { showVideoGenerator = false }
            )
        }

        // Sign In Screen with slide + fade transition
        AnimatedVisibility(
            visible = !isLoggedIn || showSignIn,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            SignInScreen(
                viewModel = viewModel,
                colors = colors,
                onBack = { 
                    if (isLoggedIn) {
                        showSignIn = false 
                    }
                },
                canGoBack = isLoggedIn
            )
        }
    }
}

@Composable
fun GeneratingIndicator(colors: AppColors, agentStatus: String? = null, agentLogs: List<String> = emptyList()) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha1"
    )
    
    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = androidx.compose.animation.core.StartOffset(200)
        ),
        label = "alpha2"
    )
    
    val alpha3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = androidx.compose.animation.core.StartOffset(400)
        ),
        label = "alpha3"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (agentStatus != null) {
            Row(
                modifier = Modifier
                    .border(1.dp, Color(0xFF2E2E2E), RoundedCornerShape(20.dp))
                    .background(Color(0xFF161616), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF81C995), CircleShape)
                        .alpha(alpha1)
                )
                Text(
                    text = agentStatus,
                    color = Color(0xFF81C995),
                    fontSize = 12.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }

        if (agentLogs.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .border(1.dp, Color(0xFF2E2E2E), RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F0F0F), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF81C995), CircleShape))
                    Text(
                        text = "AGENT EXECUTION LOGS",
                        color = Color(0xFF81C995),
                        fontSize = 10.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
                
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    agentLogs.forEach { log ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(">", color = Color(0xFF6E6E6E), fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                            Text(log, color = Color(0xFFD4D4D4), fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(colors.inputBg)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(colors.aiText, CircleShape).alpha(alpha1))
                Box(modifier = Modifier.size(8.dp).background(colors.aiText, CircleShape).alpha(alpha2))
                Box(modifier = Modifier.size(8.dp).background(colors.aiText, CircleShape).alpha(alpha3))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    colors: AppColors,
    textSize: String = "Default",
    showSuggestions: Boolean = false,
    suggestions: List<String> = emptyList(),
    onSuggestionClick: (String) -> Unit = {},
    onCopy: () -> Unit = {},
    onRegenerate: () -> Unit = {},
    isError: Boolean = false,
    errorMessage: String? = null,
    onRetry: () -> Unit = {},
    onEditUserMessage: (String) -> Unit = {},
    allConversationMessages: List<MessageEntity> = emptyList(),
    onSetBranch: (parentMessageId: String?, index: Int) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var showMoreMenu by remember { mutableStateOf(false) }
    
    val fontSizeSp = when (textSize) {
        "Small" -> 14.sp
        "Large" -> 18.sp
        else -> 16.sp
    }

    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn()
    ) {
        if (message.isUser) {
            val siblings = allConversationMessages.filter { it.parentMessageId == message.parentMessageId }
            val siblingCount = siblings.size
            val currentSiblingIndex = siblings.indexOfFirst { it.id.toString() == message.id }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.userBubble)
                            .combinedClickable(
                                onClick = {},
                                onLongClick = { showMoreMenu = true }
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (message.imageUri != null) {
                            coil.compose.AsyncImage(
                                model = message.imageUri,
                                contentDescription = "Attached image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                        if (message.text.isNotEmpty()) {
                            Text(
                                text = message.text,
                                color = colors.aiText,
                                fontSize = fontSizeSp,
                                lineHeight = fontSizeSp * 1.5f
                            )
                        }
                    }
                    
                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false },
                        modifier = Modifier.background(colors.inputBg)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Copy text", color = colors.aiText) },
                            onClick = {
                                onCopy()
                                showMoreMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit", color = colors.aiText) },
                            onClick = {
                                onEditUserMessage(message.text)
                                showMoreMenu = false
                            }
                        )
                    }
                }
            }
                if (siblingCount > 1 && currentSiblingIndex >= 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp, end = 8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                val prevIndex = (currentSiblingIndex - 1 + siblingCount) % siblingCount
                                onSetBranch(message.parentMessageId, prevIndex)
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                                contentDescription = "Previous branch",
                                tint = colors.textGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "${currentSiblingIndex + 1}/$siblingCount",
                            color = colors.textGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        IconButton(
                            onClick = {
                                val nextIndex = (currentSiblingIndex + 1) % siblingCount
                                onSetBranch(message.parentMessageId, nextIndex)
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                contentDescription = "Next branch",
                                tint = colors.textGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        } else {
            val siblings = allConversationMessages.filter { it.parentMessageId == message.parentMessageId }
            val siblingCount = siblings.size
            val currentSiblingIndex = siblings.indexOfFirst { it.id.toString() == message.id }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                if (isError && errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFF6B6B).copy(alpha = 0.15f))
                            .clickable { onRetry() }
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = Color(0xFFFF6B6B))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = errorMessage,
                                color = Color(0xFFFF6B6B),
                                fontSize = fontSizeSp
                            )
                        }
                    }
                } else {
                    val thinkingText = remember(message.text, message.thinking) {
                        message.thinking ?: run {
                            val match = Regex("<think>(.*?)</think>", RegexOption.DOT_MATCHES_ALL).find(message.text)
                                ?: Regex("<thinking>(.*?)</thinking>", RegexOption.DOT_MATCHES_ALL).find(message.text)
                                ?: Regex("<thought>(.*?)</thought>", RegexOption.DOT_MATCHES_ALL).find(message.text)
                            match?.groups?.get(1)?.value?.trim()
                        }
                    }
                    val (finalThinking, agentLogsList) = remember(thinkingText) {
                        if (thinkingText == null) {
                            Pair(null, emptyList<String>())
                        } else {
                            val match = Regex("<agent_logs>(.*?)</agent_logs>", RegexOption.DOT_MATCHES_ALL).find(thinkingText)
                            if (match != null) {
                                val logsStr = match.groups[1]?.value?.trim() ?: ""
                                val list = logsStr.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                                val cleanThink = thinkingText.replace(match.value, "").trim()
                                Pair(if (cleanThink.isEmpty()) null else cleanThink, list)
                            } else {
                                Pair(thinkingText, emptyList())
                            }
                        }
                    }
                    val displayMarkdownText = remember(message.text, thinkingText) {
                        var t = message.text
                        if (message.thinking == null) {
                            t = t.replace(Regex("<think>.*?</think>", RegexOption.DOT_MATCHES_ALL), "")
                                 .replace(Regex("<thinking>.*?</thinking>", RegexOption.DOT_MATCHES_ALL), "")
                                 .replace(Regex("<thought>.*?</thought>", RegexOption.DOT_MATCHES_ALL), "")
                                 .trim()
                        }
                        t
                    }

                    Column(
                        modifier = Modifier
                            .combinedClickable(
                                onClick = {},
                                onLongClick = { showMoreMenu = true }
                            )
                    ) {
                        if (agentLogsList.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .border(1.dp, Color(0xFF2E2E2E), RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0F0F0F), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF81C995), CircleShape))
                                    Text(
                                        text = "AGENT TRACE",
                                        color = Color(0xFF81C995),
                                        fontSize = 10.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    agentLogsList.forEach { log ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(">", color = Color(0xFF6E6E6E), fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                            Text(log, color = Color(0xFFD4D4D4), fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                        }
                                    }
                                }
                            }
                        }

                        val cleanThinkingText = remember(finalThinking) {
                            if (finalThinking != null) {
                                val match = Regex("<think>(.*?)</think>", RegexOption.DOT_MATCHES_ALL).find(finalThinking)
                                    ?: Regex("<thinking>(.*?)</thinking>", RegexOption.DOT_MATCHES_ALL).find(finalThinking)
                                    ?: Regex("<thought>(.*?)</thought>", RegexOption.DOT_MATCHES_ALL).find(finalThinking)
                                match?.groups?.get(1)?.value?.trim() ?: finalThinking
                            } else {
                                null
                            }
                        }

                        if (!cleanThinkingText.isNullOrEmpty()) {
                            ThinkingBar(thinkingText = cleanThinkingText, colors = colors)
                        }
                        val messageParts = remember(displayMarkdownText) {
                            parseMessageParts(displayMarkdownText)
                        }
                        for (part in messageParts) {
                            when (part) {
                                is MessagePart.Text -> {
                                    com.mikepenz.markdown.m3.Markdown(
                                        content = part.content,
                                        colors = com.mikepenz.markdown.m3.markdownColor(
                                            text = colors.aiText,
                                            codeText = colors.aiText,
                                            codeBackground = colors.inputBg
                                        ),
                                        typography = com.mikepenz.markdown.m3.markdownTypography(
                                            text = TextStyle(fontSize = fontSizeSp, lineHeight = fontSizeSp * 1.5f),
                                            code = TextStyle(fontSize = fontSizeSp * 0.9f, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, background = colors.inputBg)
                                        )
                                    )
                                }
                                is MessagePart.Code -> {
                                    CodeBlockCard(
                                        language = part.language,
                                        code = part.code,
                                        colors = colors
                                    )
                                }
                                is MessagePart.Formula -> {
                                    FormulaCard(
                                        formula = part.formula,
                                        colors = colors
                                    )
                                }
                            }
                        }
                    }
                    
                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false },
                        modifier = Modifier.background(colors.inputBg)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Copy text", color = colors.aiText) },
                            onClick = {
                                onCopy()
                                showMoreMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Regenerate", color = colors.aiText) },
                            onClick = {
                                onRegenerate()
                                showMoreMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share", color = colors.aiText) },
                            onClick = {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, message.text)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, null)
                                context.startActivity(shareIntent)
                                showMoreMenu = false
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    var liked by remember { mutableStateOf<Boolean?>(null) }
                    
                    AnimatedVisibility(visible = !isError && message.text.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { onCopy() }, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy text", tint = colors.iconGray, modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { liked = if (liked == true) null else true }, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.Outlined.ThumbUp, contentDescription = "Thumbs up", tint = if (liked == true) colors.aiText else colors.iconGray, modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { liked = if (liked == false) null else false }, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.Outlined.ThumbDown, contentDescription = "Thumbs down", tint = if (liked == false) colors.aiText else colors.iconGray, modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { onRegenerate() }, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.Outlined.Refresh, contentDescription = "Regenerate response", tint = colors.iconGray, modifier = Modifier.size(18.dp))
                                }
                            }

                            if (siblingCount > 1 && currentSiblingIndex >= 0) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            val prevIndex = (currentSiblingIndex - 1 + siblingCount) % siblingCount
                                            onSetBranch(message.parentMessageId, prevIndex)
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                                            contentDescription = "Previous branch",
                                            tint = colors.textGray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = "${currentSiblingIndex + 1}/$siblingCount",
                                        color = colors.textGray,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    IconButton(
                                        onClick = {
                                            val nextIndex = (currentSiblingIndex + 1) % siblingCount
                                            onSetBranch(message.parentMessageId, nextIndex)
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                            contentDescription = "Next branch",
                                            tint = colors.textGray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (showSuggestions && suggestions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(0.7f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(suggestions) { suggestion ->
                                OutlinedButton(
                                    onClick = { onSuggestionClick(suggestion) },
                                    shape = CircleShape,
                                    border = BorderStroke(1.dp, colors.borderGray),
                                    colors = ButtonDefaults.outlinedButtonColors(containerColor = colors.inputBg),
                                    modifier = Modifier.heightIn(min = 40.dp)
                                ) {
                                    Text(
                                        text = suggestion,
                                        color = colors.aiText,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

sealed class MessagePart {
    data class Text(val content: String) : MessagePart()
    data class Code(val language: String, val code: String) : MessagePart()
    data class Formula(val formula: String) : MessagePart()
}

fun parseMessageParts(text: String): List<MessagePart> {
    val parts = mutableListOf<MessagePart>()
    // Match code blocks and $$ math blocks
    val regex = Regex("(```([a-zA-Z0-9_-]*)\\n(.*?)\\n```)|(\\$\\$(.*?)\\$\\$)", RegexOption.DOT_MATCHES_ALL)
    var lastIndex = 0
    for (match in regex.findAll(text)) {
        if (match.range.first > lastIndex) {
            val normalText = text.substring(lastIndex, match.range.first).trim()
            if (normalText.isNotEmpty()) {
                parts.add(MessagePart.Text(normalText))
            }
        }
        val group1 = match.groups[1]?.value
        if (group1 != null) {
            val lang = match.groups[2]?.value ?: ""
            val code = match.groups[3]?.value ?: ""
            if (lang.lowercase() == "latex" || lang.lowercase() == "math") {
                parts.add(MessagePart.Formula(code))
            } else {
                parts.add(MessagePart.Code(lang, code))
            }
        } else {
            val formula = match.groups[5]?.value?.trim() ?: ""
            parts.add(MessagePart.Formula(formula))
        }
        lastIndex = match.range.last + 1
    }
    if (lastIndex < text.length) {
        val remaining = text.substring(lastIndex).trim()
        if (remaining.isNotEmpty()) {
            parts.add(MessagePart.Text(remaining))
        }
    }
    if (parts.isEmpty() && text.isNotEmpty()) {
        parts.add(MessagePart.Text(text))
    }
    return parts
}

fun highlightCode(code: String): AnnotatedString {
    val keywords = setOf("import", "def", "class", "return", "for", "in", "if", "else", "elif", "while", "val", "var", "fun", "print", "true", "false", "none", "null", "async", "await", "try", "except", "catch", "finally", "public", "private", "package", "from", "as")
    
    return buildAnnotatedString {
        val lines = code.lines()
        for ((idx, line) in lines.withIndex()) {
            if (idx > 0) append("\n")
            val wordRegex = Regex("([a-zA-Z_][a-zA-Z0-9_]*)|(\"[^\"]*\")|('[^']*')|([0-9]+)|([^a-zA-Z0-9_\"'\\s]+)|(\\s+)")
            val matches = wordRegex.findAll(line)
            for (match in matches) {
                val token = match.value
                val trimmed = token.trim()
                when {
                    token.startsWith("\"") || token.startsWith("'") -> {
                        withStyle(SpanStyle(color = Color(0xFFA5D6A7))) { append(token) }
                    }
                    keywords.contains(trimmed) -> {
                        withStyle(SpanStyle(color = Color(0xFF82B1FF), fontWeight = FontWeight.Bold)) { append(token) }
                    }
                    trimmed.toIntOrNull() != null || trimmed.toDoubleOrNull() != null -> {
                        withStyle(SpanStyle(color = Color(0xFFFFAB40))) { append(token) }
                    }
                    trimmed.startsWith("#") || trimmed.startsWith("//") -> {
                        withStyle(SpanStyle(color = Color(0xFF78909C))) { append(token) }
                    }
                    else -> {
                        withStyle(SpanStyle(color = Color(0xFFE0E0E0))) { append(token) }
                    }
                }
            }
        }
    }
}

@Composable
fun CodeBlockCard(
    language: String,
    code: String,
    colors: AppColors
) {
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(colors.bubbleGray)
            .border(1.dp, colors.borderGray, RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.inputBg)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = language.ifEmpty { "code" },
                    color = colors.textGray,
                    fontSize = 12.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(code))
                        copied = true
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (copied) Icons.Outlined.Done else Icons.Outlined.ContentCopy,
                        contentDescription = "Copy code",
                        tint = if (copied) colors.primary else colors.textGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            val highlightedCode = remember(code) {
                highlightCode(code)
            }

            androidx.compose.foundation.text.selection.SelectionContainer {
                Text(
                    text = highlightedCode,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = colors.aiText
                )
            }
        }
    }
}

@Composable
fun FormulaCard(
    formula: String,
    colors: AppColors
) {
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    val cleanFormula = formula
        .removePrefix("\\[").removeSuffix("\\]")
        .removePrefix("\\(").removeSuffix("\\)")
        .trim()

    val isDark = colors.isDark
    val bgColorHex = if (isDark) "#131519" else "#F5F5F5"
    val textColorHex = if (isDark) "#E8EAED" else "#1F1F1F"

    val htmlContent = remember(cleanFormula, isDark) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.css">
            <script defer src="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.js"></script>
            <style>
                body {
                    background-color: $bgColorHex;
                    color: $textColorHex;
                    font-family: sans-serif;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    height: 100vh;
                    margin: 0;
                    padding: 8px;
                    overflow: hidden;
                }
                .formula-container {
                    font-size: 1.5em;
                    text-align: center;
                }
            </style>
        </head>
        <body>
            <div class="formula-container" id="math"></div>
            <script>
                window.onload = function() {
                    try {
                        katex.render(String.raw`${cleanFormula.replace("\\", "\\\\")}`, document.getElementById("math"), {
                            displayMode: true,
                            throwOnError: false
                        });
                    } catch (e) {
                        document.getElementById("math").innerText = `${cleanFormula}`;
                    }
                };
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(colors.bubbleGray)
            .border(1.dp, colors.borderGray, RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.inputBg)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LaTeX Formula",
                    color = colors.textGray,
                    fontSize = 11.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(cleanFormula))
                        copied = true
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (copied) Icons.Outlined.Done else Icons.Outlined.ContentCopy,
                        contentDescription = "Copy formula",
                        tint = if (copied) colors.primary else colors.textGray,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { context ->
                        android.webkit.WebView(context).apply {
                            settings.javaScriptEnabled = true
                            setBackgroundColor(android.graphics.Color.TRANSPARENT)
                            loadDataWithBaseURL("https://cdn.jsdelivr.net", htmlContent, "text/html", "UTF-8", null)
                        }
                    },
                    update = { webView ->
                        webView.loadDataWithBaseURL("https://cdn.jsdelivr.net", htmlContent, "text/html", "UTF-8", null)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun ThinkingBar(thinkingText: String, colors: AppColors) {
    var isExpanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.inputBg.copy(alpha = 0.6f))
            .border(1.dp, colors.borderGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .clickable { isExpanded = !isExpanded }
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    Icons.Outlined.Psychology,
                    contentDescription = "Thinking",
                    tint = colors.textGray,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (isExpanded) "Thought process" else "Thought process (tap to expand)",
                    color = colors.textGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Icon(
                if (isExpanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = colors.textGray,
                modifier = Modifier.size(16.dp)
            )
        }
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(200)) + shrinkVertically(animationSpec = tween(200))
        ) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = thinkingText,
                    color = colors.textGray.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun ModelSelectorDropdown(
    selectedModel: String,
    colors: AppColors,
    onModelSelected: (String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    var secretCodeInput by remember { mutableStateOf("") }
    var codeError by remember { mutableStateOf(false) }

    val models = listOf(
        Triple("Milo 2.5 flash-non reasoning", "Milo efficient everyday model", "Fast"),
        Triple("Milo 2.5 flash-reasoning", "Milo reasoning model", "Reasoning"),
        Triple("Milo 2.5 pro", "Milo max reasoning model", "Pro"),
        Triple("Milo-max", "Milo maximum intelligence", "Max")
    )

    if (showSecurityDialog) {
        AlertDialog(
            onDismissRequest = {
                showSecurityDialog = false
                secretCodeInput = ""
                codeError = false
            },
            modifier = Modifier.shadow(8.dp, shape = AlertDialogDefaults.shape),
            title = { Text("Model Restricted", color = colors.aiText) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "This model is limited and still being tested, for safety reasons input secret code",
                        color = colors.textGray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = secretCodeInput,
                        onValueChange = {
                            secretCodeInput = it
                            codeError = false
                        },
                        label = { Text("Secret code", color = colors.textGray) },
                        singleLine = true,
                        isError = codeError,
                        textStyle = TextStyle(color = colors.aiText),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.aiText,
                            unfocusedBorderColor = colors.borderGray,
                            errorBorderColor = Color(0xFFFF6B6B)
                        )
                    )
                    if (codeError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Incorrect code. Please try again.",
                            color = Color(0xFFFF6B6B),
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (secretCodeInput.trim() == ChatViewModel.MILO_MAX_UNLOCK_CODE) {
                            onModelSelected("Milo-max", secretCodeInput.trim())
                            showSecurityDialog = false
                            secretCodeInput = ""
                            codeError = false
                        } else {
                            codeError = true
                        }
                    }
                ) {
                    Text("Unlock", color = colors.aiText, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSecurityDialog = false
                        secretCodeInput = ""
                        codeError = false
                    }
                ) {
                    Text("Cancel", color = colors.textGray)
                }
            },
            containerColor = colors.bubbleGray
        )
    }

    Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(18.dp),
            color = colors.bubbleGray,
            modifier = Modifier.height(36.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(colors.aiText, CircleShape)
                )
                Text(
                    text = selectedModel,
                    color = colors.aiText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    Icons.Outlined.KeyboardArrowDown,
                    contentDescription = "Select model",
                    tint = colors.textGray,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(colors.plusBg)
                .border(1.dp, colors.borderGray, RoundedCornerShape(16.dp))
                .widthIn(min = 280.dp)
                .padding(4.dp)
        ) {
            models.forEach { (name, desc, badge) ->
                val isSelected = selectedModel == name
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) colors.bubbleGray else Color.Transparent)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = name,
                                    color = colors.aiText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = desc,
                                    color = colors.textGray,
                                    fontSize = 12.sp
                                )
                            }
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(colors.aiText, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Outlined.Check,
                                        contentDescription = "Selected",
                                        tint = colors.background,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    },
                    onClick = {
                        expanded = false
                        if (name == "Milo-max") {
                            showSecurityDialog = true
                        } else {
                            onModelSelected(name, "")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun InputBar(
    inputText: String,
    isGenerating: Boolean,
    colors: AppColors,
    attachedImageUri: Uri?,
    selectedModel: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    onMicClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    onDocumentClick: () -> Unit,
    onGenerateImageClick: () -> Unit,
    onModelSelected: (String, String) -> Unit
) {
    var showAttachMenu by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(26.dp),
                clip = false
            )
            .background(colors.inputBg, shape = RoundedCornerShape(26.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ModelSelectorDropdown(
                selectedModel = selectedModel,
                colors = colors,
                onModelSelected = onModelSelected
            )
            Spacer(modifier = Modifier.height(4.dp))

            if (attachedImageUri != null) {
                Row(
                    modifier = Modifier.padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.bubbleGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Image, contentDescription = "Attached image", tint = colors.aiText)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("File attached", color = colors.textGray, fontSize = 12.sp)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box {
                    IconButton(
                        onClick = { showAttachMenu = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(colors.plusBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Add,
                                contentDescription = "Attach image or file",
                                tint = colors.aiText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showAttachMenu,
                        onDismissRequest = { showAttachMenu = false },
                        modifier = Modifier
                            .background(colors.plusBg, RoundedCornerShape(24.dp))
                            .border(1.dp, colors.borderGray, RoundedCornerShape(24.dp))
                            .padding(4.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Upload gallery", color = colors.aiText, fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(Icons.Outlined.PhotoLibrary, contentDescription = null, tint = colors.aiText, modifier = Modifier.size(20.dp))
                            },
                            onClick = {
                                showAttachMenu = false
                                onGalleryClick()
                            },
                            modifier = Modifier
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                        DropdownMenuItem(
                            text = { Text("Generate image", color = colors.aiText, fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = colors.aiText, modifier = Modifier.size(20.dp))
                            },
                            onClick = {
                                showAttachMenu = false
                                onGenerateImageClick()
                            },
                            modifier = Modifier
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                        DropdownMenuItem(
                            text = { Text("Snap from camera", color = colors.aiText, fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(Icons.Outlined.PhotoCamera, contentDescription = null, tint = colors.aiText, modifier = Modifier.size(20.dp))
                            },
                            onClick = {
                                showAttachMenu = false
                                onCameraClick()
                            },
                            modifier = Modifier
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                        DropdownMenuItem(
                            text = { Text("Upload document", color = colors.aiText, fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(Icons.Outlined.Description, contentDescription = null, tint = colors.aiText, modifier = Modifier.size(20.dp))
                            },
                            onClick = {
                                showAttachMenu = false
                                onDocumentClick()
                            },
                            modifier = Modifier
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                BasicTextField(
                    value = inputText,
                    onValueChange = onTextChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 4.dp)
                        .heightIn(max = 120.dp)
                        .verticalScroll(rememberScrollState()),
                    textStyle = TextStyle(color = colors.aiText, fontSize = 14.sp),
                    cursorBrush = SolidColor(colors.aiText),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                    decorationBox = { innerTextField ->
                        if (inputText.isEmpty()) {
                            Text("Ask anything", color = colors.textGray, fontSize = 14.sp)
                        }
                        innerTextField()
                    }
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Crossfade(targetState = Pair(inputText.isEmpty(), isGenerating), label = "send_mic_crossfade") { (isEmpty, generating) ->
                    if (generating) {
                        IconButton(
                            onClick = onStop,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(colors.plusBg, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.Stop,
                                    contentDescription = "Stop generation",
                                    tint = colors.aiText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else if (isEmpty) {
                        IconButton(
                            onClick = onMicClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Mic,
                                contentDescription = "Voice input",
                                tint = colors.textGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = onSend,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(colors.aiText, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.ArrowUpward,
                                    contentDescription = "Send message",
                                    tint = colors.background,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConversationRowItem(
    item: Conversation,
    colors: AppColors,
    onLoadChat: (String) -> Unit,
    onPinChat: (String, Boolean) -> Unit,
    onRenameChat: (String, String) -> Unit,
    onDeleteChat: (String) -> Unit,
    foldersList: List<FolderEntity> = emptyList(),
    onMoveToFolder: (String, String?) -> Unit = { _, _ -> }
) {
    var showRowMenu by remember { mutableStateOf(false) }
    var chatToRename by remember { mutableStateOf<Conversation?>(null) }
    var newTitleInput by remember { mutableStateOf("") }
    var chatToDelete by remember { mutableStateOf<String?>(null) }

    if (chatToDelete != null) {
        AlertDialog(
            onDismissRequest = { chatToDelete = null },
            title = { Text("Delete conversation", color = colors.aiText) },
            text = { Text("Are you sure you want to delete this conversation?", color = colors.textGray) },
            confirmButton = {
                TextButton(onClick = { 
                    onDeleteChat(chatToDelete!!)
                    chatToDelete = null 
                }) {
                    Text("Delete", color = Color(0xFFFF6B6B))
                }
            },
            dismissButton = {
                TextButton(onClick = { chatToDelete = null }) {
                    Text("Cancel", color = colors.aiText)
                }
            },
            containerColor = colors.bubbleGray
        )
    }

    if (chatToRename != null) {
        AlertDialog(
            onDismissRequest = { chatToRename = null },
            title = { Text("Rename conversation", color = colors.aiText) },
            text = {
                OutlinedTextField(
                    value = newTitleInput,
                    onValueChange = { newTitleInput = it },
                    singleLine = true,
                    textStyle = TextStyle(color = colors.aiText)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newTitleInput.isNotBlank()) {
                        onRenameChat(chatToRename!!.id, newTitleInput)
                    }
                    chatToRename = null
                }) {
                    Text("Save", color = colors.aiText)
                }
            },
            dismissButton = {
                TextButton(onClick = { chatToRename = null }) {
                    Text("Cancel", color = colors.aiText)
                }
            },
            containerColor = colors.bubbleGray
        )
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { onLoadChat(item.id) }
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            if (item.isPinned) {
                Icon(Icons.Outlined.PushPin, contentDescription = "Pinned", tint = colors.textGray, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = item.title,
                color = colors.aiText,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.bubbleGray)
                    .clickable { showRowMenu = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = "Chat options",
                    tint = colors.aiText,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        DropdownMenu(
            expanded = showRowMenu,
            onDismissRequest = { showRowMenu = false },
            modifier = Modifier.background(colors.inputBg),
            offset = androidx.compose.ui.unit.DpOffset(x = 180.dp, y = 0.dp)
        ) {
            DropdownMenuItem(
                text = { Text(if (item.isPinned) "Unpin" else "Pin", color = colors.aiText) },
                onClick = {
                    onPinChat(item.id, item.isPinned)
                    showRowMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Rename", color = colors.aiText) },
                onClick = {
                    newTitleInput = item.title
                    chatToRename = item
                    showRowMenu = false
                }
            )
            
            // Move to Folder options
            if (foldersList.isNotEmpty()) {
                foldersList.forEach { folder ->
                    if (item.folderId != folder.id) {
                        DropdownMenuItem(
                            text = { Text("Move to: ${folder.name}", color = colors.aiText) },
                            onClick = {
                                onMoveToFolder(item.id, folder.id)
                                showRowMenu = false
                            },
                            leadingIcon = { Icon(Icons.Outlined.Folder, contentDescription = null, tint = colors.iconGray, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
                if (item.folderId != null) {
                    DropdownMenuItem(
                        text = { Text("Remove from Folder", color = Color(0xFFFF6B6B)) },
                        onClick = {
                            onMoveToFolder(item.id, null)
                            showRowMenu = false
                        },
                        leadingIcon = { Icon(Icons.Outlined.FolderDelete, contentDescription = null, tint = Color(0xFFFF6B6B), modifier = Modifier.size(16.dp)) }
                    )
                }
            }

            DropdownMenuItem(
                text = { Text("Delete", color = Color(0xFFFF6B6B)) },
                onClick = {
                    chatToDelete = item.id
                    showRowMenu = false
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SidebarContent(
    conversations: List<Conversation>,
    colors: AppColors,
    viewModel: ChatViewModel,
    onNewChat: () -> Unit,
    onSettingsClick: () -> Unit,
    onImageGeneratorClick: () -> Unit,
    onLoadChat: (String) -> Unit,
    onDeleteChat: (String) -> Unit,
    onRenameChat: (String, String) -> Unit,
    onPinChat: (String, Boolean) -> Unit,
    onVideoGeneratorClick: () -> Unit
) {
    val userName by viewModel.userName.collectAsState()
    val userAvatar by viewModel.userAvatar.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val foldersList by viewModel.folders.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val expandedFolders = remember { mutableStateMapOf<String, Boolean>() }
    var folderToCreate by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    if (folderToCreate) {
        AlertDialog(
            onDismissRequest = { folderToCreate = false },
            title = { Text("Create Folder", color = colors.aiText) },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    singleLine = true,
                    placeholder = { Text("Folder Name", color = colors.textGray) },
                    textStyle = TextStyle(color = colors.aiText)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newFolderName.isNotBlank()) {
                        viewModel.createFolder(newFolderName)
                    }
                    newFolderName = ""
                    folderToCreate = false
                }) {
                    Text("Create", color = colors.aiText)
                }
            },
            dismissButton = {
                TextButton(onClick = { folderToCreate = false }) {
                    Text("Cancel", color = colors.aiText)
                }
            },
            containerColor = colors.bubbleGray
        )
    }

    ModalDrawerSheet(
        drawerContainerColor = colors.background,
        modifier = Modifier.width(300.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.inputBg, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Search, contentDescription = "Search recents", tint = colors.textGray, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(color = colors.aiText, fontSize = 16.sp),
                        cursorBrush = SolidColor(colors.aiText),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                  Text("Search", color = colors.textGray, fontSize = 16.sp)
                            }
                            innerTextField()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onNewChat() }
                    .padding(vertical = 12.dp, horizontal = 8.dp)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "New chat", tint = colors.aiText, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text("New chat", color = colors.aiText, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onImageGeneratorClick() }
                    .padding(vertical = 12.dp, horizontal = 8.dp)
            ) {
                Icon(Icons.Outlined.Image, contentDescription = "Generate Image", tint = colors.aiText, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text("Generate Image", color = colors.aiText, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onVideoGeneratorClick() }
                    .padding(vertical = 12.dp, horizontal = 8.dp)
            ) {
                Icon(Icons.Outlined.Videocam, contentDescription = "Milo-Video-1", tint = colors.aiText, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text("Milo-Video-1", color = colors.aiText, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recents & Folders", color = colors.textGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                IconButton(
                    onClick = { folderToCreate = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CreateNewFolder,
                        contentDescription = "Create Folder",
                        tint = colors.aiText,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                val filtered = conversations.filter { it.title.contains(searchQuery, ignoreCase = true) }
                val filteredFolders = foldersList.filter { it.name.contains(searchQuery, ignoreCase = true) }

                if (filtered.isEmpty() && filteredFolders.isEmpty()) {
                    item {
                        Text(
                            text = if (conversations.isEmpty()) "No conversations yet" else "No results for '$searchQuery'",
                            color = colors.textGray,
                            fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 8.dp)
                        )
                    }
                } else {
                    // Render Folders
                    items(filteredFolders, key = { "folder_${it.id}" }) { folder ->
                        val isExpanded = expandedFolders[folder.id] ?: false
                        val folderChats = filtered.filter { it.folderId == folder.id }
                        var showFolderMenu by remember { mutableStateOf(false) }
                        var folderToRename by remember { mutableStateOf<FolderEntity?>(null) }
                        var renameInput by remember { mutableStateOf("") }

                        if (folderToRename != null) {
                            AlertDialog(
                                onDismissRequest = { folderToRename = null },
                                title = { Text("Rename Folder", color = colors.aiText) },
                                text = {
                                    OutlinedTextField(
                                        value = renameInput,
                                        onValueChange = { renameInput = it },
                                        singleLine = true,
                                        textStyle = TextStyle(color = colors.aiText)
                                    )
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        if (renameInput.isNotBlank()) {
                                            viewModel.renameFolder(folderToRename!!.id, renameInput)
                                        }
                                        folderToRename = null
                                    }) {
                                        Text("Save", color = colors.aiText)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { folderToRename = null }) {
                                        Text("Cancel", color = colors.aiText)
                                    }
                                },
                                containerColor = colors.bubbleGray
                            )
                        }

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.bubbleGray.copy(alpha = 0.3f))
                                    .clickable {
                                        expandedFolders[folder.id] = !isExpanded
                                    }
                                    .padding(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Outlined.FolderOpen else Icons.Outlined.Folder,
                                    contentDescription = "Folder",
                                    tint = colors.aiText,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = folder.name,
                                    color = colors.aiText,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                Box {
                                    IconButton(
                                        onClick = { showFolderMenu = true },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.MoreVert,
                                            contentDescription = "Folder options",
                                            tint = colors.aiText,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showFolderMenu,
                                        onDismissRequest = { showFolderMenu = false },
                                        modifier = Modifier.background(colors.inputBg)
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Rename", color = colors.aiText) },
                                            onClick = {
                                                renameInput = folder.name
                                                folderToRename = folder
                                                showFolderMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Delete Folder", color = Color(0xFFFF6B6B)) },
                                            onClick = {
                                                viewModel.deleteFolder(folder.id)
                                                showFolderMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            if (isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp)
                                ) {
                                    if (folderChats.isEmpty()) {
                                        Text(
                                            text = "Empty folder (move chats here via chat options)",
                                            color = colors.textGray,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                        )
                                    } else {
                                        folderChats.forEach { item ->
                                            ConversationRowItem(
                                                item = item,
                                                colors = colors,
                                                onLoadChat = onLoadChat,
                                                onPinChat = onPinChat,
                                                onRenameChat = onRenameChat,
                                                onDeleteChat = onDeleteChat,
                                                foldersList = foldersList,
                                                onMoveToFolder = { cId, fId -> viewModel.moveConversationToFolder(cId, fId) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Render Uncategorized Chats
                    val uncategorizedChats = filtered.filter { it.folderId == null }
                    items(uncategorizedChats, key = { it.id }) { item ->
                        ConversationRowItem(
                            item = item,
                            colors = colors,
                            onLoadChat = onLoadChat,
                            onPinChat = onPinChat,
                            onRenameChat = onRenameChat,
                            onDeleteChat = onDeleteChat,
                            foldersList = foldersList,
                            onMoveToFolder = { cId, fId -> viewModel.moveConversationToFolder(cId, fId) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = colors.bubbleGray)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSettingsClick() }
                    .padding(vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(colors.bubbleGray, CircleShape)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AvatarIcon(userAvatar, colors, Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        userName.ifEmpty { "User Profile" },
                        color = colors.aiText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text("Personalized settings", color = colors.textGray, fontSize = 12.sp)
                }
                Icon(Icons.Outlined.Settings, contentDescription = "Open settings", tint = colors.iconGray)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ChatViewModel,
    colors: AppColors,
    onBack: () -> Unit,
    onOpenSignIn: () -> Unit,
    onClearAll: () -> Unit
) {
    BackHandler(enabled = true) {
        onBack()
    }

    var showClearDialog by remember { mutableStateOf(false) }
    
    val theme by viewModel.theme.collectAsState()
    val textSize by viewModel.textSize.collectAsState()
    val hapticFeedback by viewModel.hapticFeedback.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userAvatar by viewModel.userAvatar.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val userPhone by viewModel.userPhone.collectAsState()
    val authMethod by viewModel.authMethod.collectAsState()

    val customInstructionsAboutMe by viewModel.customInstructionsAboutMe.collectAsState()
    val customInstructionsHowRespond by viewModel.customInstructionsHowRespond.collectAsState()
    val isCustomEnabledGlobally by viewModel.customInstructionsEnabledGlobally.collectAsState()
    val selectedTone by viewModel.selectedTone.collectAsState()
    val msgCountThisMonth by viewModel.messageCountThisMonth.collectAsState()
    val tokenCountThisMonth by viewModel.tokenCountThisMonth.collectAsState()
    val scope = rememberCoroutineScope()

    var showNameDialog by remember { mutableStateOf(false) }
    var showAvatarDialog by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf(userName) }

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val file = File(context.filesDir, "avatar_${System.currentTimeMillis()}.jpg")
                    FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    viewModel.setUserAvatar(Uri.fromFile(file).toString())
                    Toast.makeText(context, "Profile picture updated", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to update profile picture", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val json = inputStream?.bufferedReader()?.use { it.readText() }
                    if (!json.isNullOrBlank()) {
                        val moshi = com.squareup.moshi.Moshi.Builder()
                            .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                            .build()
                        val adapter = moshi.adapter(BackupPayload::class.java)
                        val payload = adapter.fromJson(json)
                        if (payload != null) {
                            viewModel.restoreBackup(payload)
                            Toast.makeText(context, "Backup restored successfully!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Invalid backup format", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            modifier = Modifier.shadow(8.dp, shape = AlertDialogDefaults.shape),
            title = { Text("Clear all conversations", color = colors.aiText) },
            text = { Text("Are you sure you want to delete all conversations? This cannot be undone.", color = colors.textGray) },
            confirmButton = {
                TextButton(onClick = { 
                    onClearAll()
                    showClearDialog = false 
                }) {
                    Text("Delete", color = Color(0xFFFF6B6B))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = colors.aiText)
                }
            },
            containerColor = colors.bubbleGray
        )
    }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            modifier = Modifier.shadow(8.dp, shape = AlertDialogDefaults.shape),
            title = { Text("Edit Display Name", color = colors.aiText) },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    label = { Text("Name", color = colors.textGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.aiText,
                        unfocusedTextColor = colors.aiText,
                        focusedBorderColor = colors.aiText,
                        unfocusedBorderColor = colors.borderGray
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.setUserName(tempName)
                    showNameDialog = false 
                }) {
                    Text("Save", color = colors.aiText)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) {
                    Text("Cancel", color = colors.aiText)
                }
            },
            containerColor = colors.bubbleGray
        )
    }

    if (showAvatarDialog) {
        AlertDialog(
            onDismissRequest = { showAvatarDialog = false },
            modifier = Modifier.shadow(8.dp, shape = AlertDialogDefaults.shape),
            title = { Text("Choose Avatar", color = colors.aiText) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Choose from gallery button
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clickable {
                                avatarPickerLauncher.launch("image/*")
                                showAvatarDialog = false
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = colors.inputBg,
                        border = BorderStroke(1.dp, colors.borderGray.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Outlined.PhotoLibrary,
                                contentDescription = null,
                                tint = colors.aiText,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Choose from gallery",
                                color = colors.aiText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Text(
                        "Or select an icon:",
                        color = colors.textGray,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 8.dp)
                    )

                    val avatars = listOf("default", "robot", "star", "heart", "bolt", "face")
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.height(140.dp),
                        contentPadding = PaddingValues(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(avatars) { avatar ->
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(if (userAvatar == avatar) colors.aiText.copy(alpha = 0.2f) else colors.inputBg)
                                    .clickable { 
                                        viewModel.setUserAvatar(avatar)
                                        showAvatarDialog = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                AvatarIcon(avatar, colors, Modifier.size(32.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAvatarDialog = false }) {
                    Text("Cancel", color = colors.aiText)
                }
            },
            containerColor = colors.bubbleGray
        )
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", color = colors.aiText, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Go back", tint = colors.aiText)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- SECTION 1: ACCOUNT ---
            SettingsSectionCard(title = "Account", colors = colors) {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showAvatarDialog = true
                            }
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(colors.bubbleGray, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            AvatarIcon(userAvatar, colors, Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                userName.ifEmpty { "User Profile" },
                                color = colors.aiText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Personalize your account",
                                color = colors.textGray,
                                fontSize = 13.sp
                            )
                        }
                        IconButton(onClick = { 
                            tempName = userName
                            showNameDialog = true 
                        }) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Edit name", tint = colors.iconGray, modifier = Modifier.size(20.dp))
                        }
                    }

                    HorizontalDivider(color = colors.borderGray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (hapticFeedback) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.signOut()
                                onBack()
                            }
                            .padding(vertical = 12.dp, horizontal = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = Color(0xFFFF6B6B),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Sign Out",
                            color = Color(0xFFFF6B6B),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // --- SECTION 2: APPEARANCE ---
            SettingsSectionCard(title = "Appearance", colors = colors) {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                    ) {
                        Icon(Icons.Outlined.Palette, contentDescription = null, tint = colors.iconGray, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Theme", color = colors.aiText, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    val themeOptions = listOf(
                        Triple("Follow System", Color.Gray, "Auto-switches based on device setting"),
                        Triple("Pure Black", Color(0xFF000000), "Deepest blacks for OLED"),
                        Triple("Dark Gray", Color(0xFF1C1C1E), "Smooth and easy on the eyes"),
                        Triple("Charcoal", Color(0xFF161618), "Darker neutral gray"),
                        Triple("True Gray", Color(0xFF3A3A3C), "Balanced mid-tone gray"),
                        Triple("Soft White", Color(0xFFFFFFFF), "Clean, bright light theme")
                    )
                    themeOptions.forEach { (option, swatchColor, subtitle) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (hapticFeedback) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.setTheme(option)
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            RadioButton(
                                selected = theme == option,
                                onClick = {
                                    if (hapticFeedback) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.setTheme(option)
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = colors.aiText, unselectedColor = colors.borderGray)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(swatchColor, CircleShape)
                                    .border(1.dp, colors.borderGray, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(option, color = colors.aiText, fontSize = 15.sp)
                                Text(subtitle, color = colors.textGray, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // --- SECTION 4: DATA & PRIVACY ---
            SettingsSectionCard(title = "Data & Privacy", colors = colors) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (hapticFeedback) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showClearDialog = true
                            }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = Color(0xFFFF6B6B), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Clear all conversations", color = Color(0xFFFF6B6B), fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = colors.iconGray)
                    }
                }
            }

            // --- SECTION 5: APP SETTINGS ---
            SettingsSectionCard(title = "App Settings", colors = colors) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Outlined.Vibration, contentDescription = null, tint = colors.iconGray, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Haptic Feedback", color = colors.aiText, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                    Switch(
                        checked = hapticFeedback, 
                        onCheckedChange = { 
                            viewModel.setHapticFeedback(it)
                            if (it) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                    )
                }
            }

            // --- SECTION: CUSTOM INSTRUCTIONS ---
            SettingsSectionCard(title = "Custom Instructions", colors = colors) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enable Custom Instructions", color = colors.aiText, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Text("Tailor AI answers globally", color = colors.textGray, fontSize = 12.sp)
                        }
                        Switch(
                            checked = isCustomEnabledGlobally,
                            onCheckedChange = {
                                viewModel.setCustomInstructionsEnabledGlobally(it)
                            }
                        )
                    }

                    if (isCustomEnabledGlobally) {
                        OutlinedTextField(
                            value = customInstructionsAboutMe,
                            onValueChange = { viewModel.setCustomInstructions(it, customInstructionsHowRespond) },
                            label = { Text("What you should know about me", color = colors.textGray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = colors.aiText,
                                unfocusedTextColor = colors.aiText,
                                focusedBorderColor = colors.aiText,
                                unfocusedBorderColor = colors.borderGray,
                                focusedLabelColor = colors.aiText,
                                unfocusedLabelColor = colors.textGray
                            ),
                            placeholder = { Text("e.g., Where you live, what you do, your goals...", color = colors.textGray.copy(alpha = 0.5f)) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5
                        )

                        OutlinedTextField(
                            value = customInstructionsHowRespond,
                            onValueChange = { viewModel.setCustomInstructions(customInstructionsAboutMe, it) },
                            label = { Text("How you would like Milo to respond", color = colors.textGray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = colors.aiText,
                                unfocusedTextColor = colors.aiText,
                                focusedBorderColor = colors.aiText,
                                unfocusedBorderColor = colors.borderGray,
                                focusedLabelColor = colors.aiText,
                                unfocusedLabelColor = colors.textGray
                            ),
                            placeholder = { Text("e.g., Formal, casual, detailed, brief, bullet-points...", color = colors.textGray.copy(alpha = 0.5f)) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5
                        )
                    }
                }
            }

            // --- SECTION: RESPONSE TONE ---
            SettingsSectionCard(title = "Response Tone", colors = colors) {
                Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Text(
                        text = "Current Tone: $selectedTone",
                        color = colors.aiText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    val tonesList = listOf("Balanced", "Concise", "Detailed", "Casual", "Formal")
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tonesList) { tone ->
                            val isSelected = selectedTone == tone
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) colors.aiText else colors.inputBg)
                                    .border(1.dp, if (isSelected) Color.Transparent else colors.borderGray, RoundedCornerShape(16.dp))
                                    .clickable {
                                        viewModel.setTone(tone)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = tone,
                                    color = if (isSelected) colors.background else colors.aiText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // --- SECTION: USAGE STATISTICS ---
            SettingsSectionCard(title = "Monthly Usage & Billing", colors = colors) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val estimatedCost = (tokenCountThisMonth.toFloat() / 1_000_000f) * 2.50f
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Messages sent (this month)", color = colors.textGray, fontSize = 14.sp)
                        Text("$msgCountThisMonth", color = colors.aiText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tokens consumed (this month)", color = colors.textGray, fontSize = 14.sp)
                        Text(String.format("%,d", tokenCountThisMonth), color = colors.aiText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    HorizontalDivider(color = colors.borderGray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Estimated Monthly Bill", color = colors.aiText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Blended rate of $2.50 / 1M tokens", color = colors.textGray, fontSize = 11.sp)
                        }
                        Text(
                            text = String.format("$%.4f", estimatedCost),
                            color = Color(0xFF4CAF50),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // --- SECTION: BACKUP & RESTORE ---
            SettingsSectionCard(title = "Backup & Restore", colors = colors) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (hapticFeedback) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                scope.launch {
                                    try {
                                        val json = viewModel.getFullBackupJson()
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, json)
                                            type = "application/json"
                                        }
                                        val shareIntent = Intent.createChooser(sendIntent, "Export Backup")
                                        context.startActivity(shareIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.CloudUpload, contentDescription = null, tint = colors.aiText, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Export Backup (JSON)", color = colors.aiText, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = colors.iconGray)
                    }

                    HorizontalDivider(color = colors.borderGray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (hapticFeedback) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                importLauncher.launch("application/json")
                            }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.CloudDownload, contentDescription = null, tint = colors.aiText, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Import Backup (JSON)", color = colors.aiText, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = colors.iconGray)
                    }
                }
            }

            // --- SECTION 6: ABOUT ---
            SettingsSectionCard(title = "About", colors = colors) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Info, contentDescription = null, tint = colors.iconGray, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Version", color = colors.aiText, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                        Text("BETA 5.0", color = colors.textGray, fontSize = 15.sp)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (hapticFeedback) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@miloai.com")).apply {
                                    putExtra(Intent.EXTRA_SUBJECT, "Milo AI Feedback (BETA 5.0)")
                                }
                                try {
                                    context.startActivity(emailIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "No email client installed", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Feedback, contentDescription = null, tint = colors.iconGray, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Send feedback", color = colors.aiText, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = colors.iconGray)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSectionCard(
    title: String,
    colors: AppColors,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            color = colors.textGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = colors.inputBg,
            shadowElevation = 3.dp,
            border = BorderStroke(1.dp, colors.borderGray.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                content = content
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    viewModel: ChatViewModel,
    colors: AppColors,
    onBack: () -> Unit,
    canGoBack: Boolean = true
) {
    BackHandler(enabled = canGoBack) {
        onBack()
    }

    var selectedTab by remember { mutableStateOf(0) } // 0: Email, 1: Phone
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }

    var phoneInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    var verificationIdState by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val hapticEnabled by viewModel.hapticFeedback.collectAsState()

    val auth = remember { 
        try { 
            com.google.firebase.auth.FirebaseAuth.getInstance() 
        } catch (e: Exception) { 
            null 
        } 
    }
    val authManager = remember { auth?.let { com.example.AuthManager(it) } }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isSignUp) "Create Account" else "Sign In", color = colors.aiText, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    if (canGoBack) {
                        IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Outlined.ArrowBack, contentDescription = "Go back", tint = colors.aiText)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(colors.bubbleGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = colors.aiText, modifier = Modifier.size(28.dp))
            }
            Text("Welcome to Milo AI", color = colors.aiText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Sign in or create an account to sync conversations across your devices.", color = colors.textGray, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)

            Spacer(modifier = Modifier.height(4.dp))

            // Method Selector Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.inputBg, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Email", "Phone").forEachIndexed { index, title ->
                    val selected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) colors.bubbleGray else Color.Transparent)
                            .clickable {
                                if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedTab = index
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (selected) colors.aiText else colors.textGray,
                            fontSize = 14.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colors.aiText)
                }
            } else {
                when (selectedTab) {
                    0 -> { // Email & Password
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (isSignUp) {
                                OutlinedTextField(
                                    value = nameInput,
                                    onValueChange = { nameInput = it },
                                    label = { Text("Full Name", color = colors.textGray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colors.aiText,
                                        unfocusedBorderColor = colors.borderGray,
                                        focusedTextColor = colors.aiText,
                                        unfocusedTextColor = colors.aiText
                                    ),
                                    singleLine = true
                                )
                            }
                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("Email Address", color = colors.textGray) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.aiText,
                                    unfocusedBorderColor = colors.borderGray,
                                    focusedTextColor = colors.aiText,
                                    unfocusedTextColor = colors.aiText
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Email)
                            )
                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text("Password", color = colors.textGray) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.aiText,
                                    unfocusedBorderColor = colors.borderGray,
                                    focusedTextColor = colors.aiText,
                                    unfocusedTextColor = colors.aiText
                                ),
                                singleLine = true,
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Password)
                            )

                            Button(
                                onClick = {
                                    if (emailInput.isBlank() || passwordInput.isBlank()) {
                                        Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    
                                    if (authManager != null && auth != null) {
                                        isLoading = true
                                        if (isSignUp) {
                                            authManager.signUpWithEmail(emailInput, passwordInput, {
                                                isLoading = false
                                                viewModel.updateWithFirebaseUser(auth.currentUser)
                                                // Save custom display name if provided
                                                if (nameInput.isNotBlank()) {
                                                    val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                                                        displayName = nameInput
                                                    }
                                                    auth.currentUser?.updateProfile(profileUpdates)?.addOnCompleteListener {
                                                        viewModel.updateWithFirebaseUser(auth.currentUser)
                                                    }
                                                }
                                                Toast.makeText(context, "Account created successfully!", Toast.LENGTH_SHORT).show()
                                                onBack()
                                            }, { error ->
                                                isLoading = false
                                                Toast.makeText(context, "Sign Up Failed: $error", Toast.LENGTH_SHORT).show()
                                            })
                                        } else {
                                            authManager.signInWithEmail(emailInput, passwordInput, {
                                                isLoading = false
                                                viewModel.updateWithFirebaseUser(auth.currentUser)
                                                Toast.makeText(context, "Signed in successfully!", Toast.LENGTH_SHORT).show()
                                                onBack()
                                            }, { error ->
                                                isLoading = false
                                                Toast.makeText(context, "Sign In Failed: $error", Toast.LENGTH_SHORT).show()
                                            })
                                        }
                                    } else {
                                        viewModel.signInWithEmail(emailInput, nameInput, isSignUp)
                                        Toast.makeText(context, if (isSignUp) "Account created (Simulated)" else "Signed in (Simulated)", Toast.LENGTH_SHORT).show()
                                        onBack()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.aiText)
                            ) {
                                Text(if (isSignUp) "Create Account" else "Sign In", color = colors.background, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }

                            TextButton(
                                onClick = { isSignUp = !isSignUp },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text(
                                    text = if (isSignUp) "Already have an account? Sign In" else "Don't have an account? Create one",
                                    color = colors.aiText,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    1 -> { // Phone Number
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { phoneInput = it },
                                label = { Text("Phone Number (e.g. +1 555-0199)", color = colors.textGray) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.aiText,
                                    unfocusedBorderColor = colors.borderGray,
                                    focusedTextColor = colors.aiText,
                                    unfocusedTextColor = colors.aiText
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone)
                            )

                            if (otpSent) {
                                OutlinedTextField(
                                    value = otpInput,
                                    onValueChange = { otpInput = it },
                                    label = { Text("Enter 6-Digit OTP Code", color = colors.textGray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colors.aiText,
                                        unfocusedBorderColor = colors.borderGray,
                                        focusedTextColor = colors.aiText,
                                        unfocusedTextColor = colors.aiText
                                    ),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                                )
                            }

                            Button(
                                onClick = {
                                    if (phoneInput.isBlank()) {
                                        Toast.makeText(context, "Please enter a phone number", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                                    if (authManager != null && auth != null) {
                                        val activity = context as? Activity
                                        if (activity != null) {
                                            if (!otpSent) {
                                                isLoading = true
                                                val callbacks = object : com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                                    override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {
                                                        authManager.signInWithCredential(credential, {
                                                            isLoading = false
                                                            viewModel.updateWithFirebaseUser(auth.currentUser)
                                                            Toast.makeText(context, "Phone verification successful!", Toast.LENGTH_SHORT).show()
                                                            onBack()
                                                        }, { error ->
                                                            isLoading = false
                                                            Toast.makeText(context, "Verification Failed: $error", Toast.LENGTH_SHORT).show()
                                                        })
                                                    }

                                                    override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                                                        isLoading = false
                                                        Toast.makeText(context, "Phone Verification Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                                    }

                                                    override fun onCodeSent(verificationId: String, token: com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken) {
                                                        isLoading = false
                                                        verificationIdState = verificationId
                                                        otpSent = true
                                                        Toast.makeText(context, "Verification code sent to $phoneInput", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                                authManager.sendVerificationCode(phoneInput, activity, callbacks)
                                            } else {
                                                if (otpInput.isBlank()) {
                                                    Toast.makeText(context, "Please enter the OTP code", Toast.LENGTH_SHORT).show()
                                                    return@Button
                                                }
                                                isLoading = true
                                                val credential = com.google.firebase.auth.PhoneAuthProvider.getCredential(verificationIdState, otpInput)
                                                authManager.signInWithCredential(credential, {
                                                    isLoading = false
                                                    viewModel.updateWithFirebaseUser(auth.currentUser)
                                                    Toast.makeText(context, "Phone Sign-In Successful!", Toast.LENGTH_SHORT).show()
                                                    onBack()
                                                }, { error ->
                                                    isLoading = false
                                                    Toast.makeText(context, "Invalid verification code: $error", Toast.LENGTH_SHORT).show()
                                                })
                                            }
                                        } else {
                                            Toast.makeText(context, "Activity context is required for phone verification", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        if (!otpSent) {
                                            otpSent = true
                                            Toast.makeText(context, "OTP Code sent to $phoneInput (simulated)", Toast.LENGTH_SHORT).show()
                                        } else {
                                            if (otpInput.length < 4) {
                                                Toast.makeText(context, "Please enter a valid OTP code", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            viewModel.signInWithPhone(phoneInput)
                                            Toast.makeText(context, "Phone verification successful (local)!", Toast.LENGTH_SHORT).show()
                                            onBack()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.aiText)
                            ) {
                                Text(if (!otpSent) "Send Verification Code" else "Verify & Sign In", color = colors.background, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = colors.borderGray.copy(alpha = 0.5f)
                )
                Text(
                    text = "OR CONTINUE AS",
                    color = colors.textGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = colors.borderGray.copy(alpha = 0.5f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.signInAsGuest()
                    Toast.makeText(context, "Welcome! Continuing as guest.", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.aiText),
                border = BorderStroke(1.dp, colors.borderGray)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = colors.aiText,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Continue as Guest",
                    color = colors.aiText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
