import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Add state
state_target = "    var attachedImageUri by remember { mutableStateOf<Uri?>(null) }"
state_replacement = """    var attachedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showInlineGenerateDialog by remember { mutableStateOf(false) }"""
content = content.replace(state_target, state_replacement)

# Add dropdown item
dropdown_target = """                        DropdownMenuItem(
                            text = { Text("Snap from camera", color = colors.aiText, fontSize = 14.sp) },"""
dropdown_replacement = """                        DropdownMenuItem(
                            text = { Text("Generate image", color = colors.aiText, fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = colors.aiText, modifier = Modifier.size(20.dp))
                            },
                            onClick = {
                                showAttachMenu = false
                                showInlineGenerateDialog = true
                            },
                            modifier = Modifier
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                        DropdownMenuItem(
                            text = { Text("Snap from camera", color = colors.aiText, fontSize = 14.sp) },"""
content = content.replace(dropdown_target, dropdown_replacement)

# Add the dialog at the end of ChatScreen (before AnimatedVisibility blocks)
dialog_target = "        // Settings Screen with slide + fade transition"
dialog_replacement = """
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

        // Settings Screen with slide + fade transition"""
content = content.replace(dialog_target, dialog_replacement)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
