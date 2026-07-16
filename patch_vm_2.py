import re

with open("app/src/main/java/com/example/ChatViewModel.kt", "r") as f:
    content = f.read()

old_generate = """    fun generateImage(prompt: String, sourceImageUri: String? = null, onResult: (String?, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = org.json.JSONObject()
                json.put("model", "agnes-image-2.1-flash")
                json.put("prompt", prompt)
                json.put("size", "1024x768")
                
                val extraBody = org.json.JSONObject()
                extraBody.put("response_format", "url")
                if (sourceImageUri != null) {
                    val base64 = getBase64FromUri(sourceImageUri)
                    if (base64 != null) {
                        extraBody.put("image", org.json.JSONArray().put("data:image/jpeg;base64,$base64"))
                    } else if (sourceImageUri.startsWith("http")) {
                        extraBody.put("image", org.json.JSONArray().put(sourceImageUri))
                    }
                }
                json.put("extra_body", extraBody)"""

new_generate = """    fun generateImage(prompt: String, style: String = "Default", size: String = "1024x1024", sourceImageUri: String? = null, onResult: (String?, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val finalPrompt = prompt + (ImageStyles.styleSuffixMap[style] ?: "")
                val json = org.json.JSONObject()
                json.put("model", "agnes-image-2.1-flash")
                json.put("prompt", finalPrompt)
                json.put("size", size)
                
                val extraBody = org.json.JSONObject()
                extraBody.put("response_format", "url")
                if (sourceImageUri != null) {
                    val base64 = getBase64FromUri(sourceImageUri)
                    if (base64 != null) {
                        extraBody.put("image", org.json.JSONArray().put("data:image/jpeg;base64,$base64"))
                    } else if (sourceImageUri.startsWith("http")) {
                        extraBody.put("image", org.json.JSONArray().put(sourceImageUri))
                    }
                }
                json.put("extra_body", extraBody)"""
content = content.replace(old_generate, new_generate)

old_insert = """                            val entity = GeneratedImageEntity(
                                id = UUID.randomUUID().toString(),
                                prompt = prompt,
                                imageUrl = finalUrl,
                                timestamp = System.currentTimeMillis()
                            )"""
new_insert = """                            val entity = GeneratedImageEntity(
                                id = UUID.randomUUID().toString(),
                                prompt = prompt,
                                imageUrl = finalUrl,
                                timestamp = System.currentTimeMillis(),
                                style = style,
                                size = size
                            )"""
content = content.replace(old_insert, new_insert)

with open("app/src/main/java/com/example/ChatViewModel.kt", "w") as f:
    f.write(content)
