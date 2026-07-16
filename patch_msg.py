import re

with open("app/src/main/java/com/example/ChatViewModel.kt", "r") as f:
    code = f.read()

replacement = """
        openRouterMessages.add(
            OpenRouterRequestMessage(
                role = "system",
                content = listOf(ContentPart(type = "text", text = systemPrompt))
            )
        )

        for (msg in messages) {
            val contentList = mutableListOf<ContentPart>()
            if (msg.text.isNotBlank()) {
                contentList.add(ContentPart(type = "text", text = msg.text))
            }
            if (msg.imageUri != null) {
                try {
                    val uri = android.net.Uri.parse(msg.imageUri)
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()
                    if (bytes != null) {
                        val base64String = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                        contentList.add(ContentPart(type = "image_url", imageUrl = ImageUrl(url = "data:${mimeType};base64,$base64String")))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            openRouterMessages.add(
                OpenRouterRequestMessage(
                    role = if (msg.isUser) "user" else "assistant",
                    content = if (contentList.isNotEmpty()) contentList else listOf(ContentPart(type="text", text=""))
                )
            )
        }
"""

target = """        openRouterMessages.add(
            OpenRouterMessage(
                role = "system",
                content = systemPrompt
            )
        )

        for (msg in messages) {
            openRouterMessages.add(
                OpenRouterMessage(
                    role = if (msg.isUser) "user" else "assistant",
                    content = msg.text
                )
            )
        }"""

code = code.replace(target, replacement)

with open("app/src/main/java/com/example/ChatViewModel.kt", "w") as f:
    f.write(code)

