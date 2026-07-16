import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Fix ChatViewModel.kt
with open("app/src/main/java/com/example/ChatViewModel.kt", "r") as f:
    cvm = f.read()

cvm = cvm.replace("BuildConfig.crowllmapikey", "BuildConfig.CROWLLM_API_KEY")
with open("app/src/main/java/com/example/ChatViewModel.kt", "w") as f:
    f.write(cvm)

# Fix InputBar definition
old_inputbar_def = """fun InputBar(
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
    onModelSelected: (String, String) -> Unit
)"""

new_inputbar_def = """fun InputBar(
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
)"""
content = content.replace(old_inputbar_def, new_inputbar_def)

# Fix InputBar usage in InputBar body
old_inline = """                            onClick = {
                                showAttachMenu = false
                                showInlineGenerateDialog = true
                            },"""

new_inline = """                            onClick = {
                                showAttachMenu = false
                                onGenerateImageClick()
                            },"""
content = content.replace(old_inline, new_inline)

# Fix InputBar usage in ChatScreen
old_call = """                        InputBar(
                            inputText = state.inputText,
                            isGenerating = state.isGenerating,
                            colors = colors,
                            attachedImageUri = attachedImageUri,
                            selectedModel = state.selectedModel,
                            onTextChange = { viewModel.updateInput(it) },
                            onSend = { 
                                if (state.inputText.isNotBlank() || attachedImageUri != null) {
                                    if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.sendMessage(attachedImageUri?.toString())
                                    attachedImageUri = null
                                    scope.launch {
                                        kotlinx.coroutines.delay(100)
                                        listState.animateScrollToItem(0)
                                    }
                                }
                            },
                            onStop = { viewModel.stopGeneration() },
                            onMicClick = { 
                                if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                speechLauncher.launch(Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                })
                            },
                            onGalleryClick = {
                                if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            onCameraClick = {
                                if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                cameraPhotoUri = context.createImageFileUri()
                                cameraLauncher.launch(cameraPhotoUri!!)
                            },
                            onDocumentClick = {
                                if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                documentLauncher.launch(arrayOf("*/*"))
                            },
                            onModelSelected = { id, name -> viewModel.selectModel(id, name) }
                        )"""

new_call = """                        InputBar(
                            inputText = state.inputText,
                            isGenerating = state.isGenerating,
                            colors = colors,
                            attachedImageUri = attachedImageUri,
                            selectedModel = state.selectedModel,
                            onTextChange = { viewModel.updateInput(it) },
                            onSend = { 
                                if (state.inputText.isNotBlank() || attachedImageUri != null) {
                                    if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.sendMessage(attachedImageUri?.toString())
                                    attachedImageUri = null
                                    scope.launch {
                                        kotlinx.coroutines.delay(100)
                                        listState.animateScrollToItem(0)
                                    }
                                }
                            },
                            onStop = { viewModel.stopGeneration() },
                            onMicClick = { 
                                if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                speechLauncher.launch(Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                })
                            },
                            onGalleryClick = {
                                if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            onCameraClick = {
                                if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                cameraPhotoUri = context.createImageFileUri()
                                cameraLauncher.launch(cameraPhotoUri!!)
                            },
                            onDocumentClick = {
                                if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                documentLauncher.launch(arrayOf("*/*"))
                            },
                            onGenerateImageClick = {
                                if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showInlineGenerateDialog = true
                            },
                            onModelSelected = { id, name -> viewModel.selectModel(id, name) }
                        )"""
content = content.replace(old_call, new_call)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
