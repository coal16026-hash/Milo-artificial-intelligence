import re

with open("app/src/main/java/com/example/ChatViewModel.kt", "r") as f:
    content = f.read()

old_generate = """    fun generateImage(prompt: String, onResult: (String?, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = org.json.JSONObject()
                json.put("model", "agnes-image-2.1-flash")
                json.put("prompt", prompt)
                json.put("size", "1024x768")
                
                val extraBody = org.json.JSONObject()
                extraBody.put("response_format", "url")
                json.put("extra_body", extraBody)"""

new_generate = """    fun generateImage(prompt: String, sourceImageUri: String? = null, onResult: (String?, String?) -> Unit) {
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
                
content = content.replace(old_generate, new_generate)

func_base64 = """
    private fun getBase64FromUri(uriString: String): String? {
        if (uriString.startsWith("http")) return null
        return try {
            val uri = android.net.Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) {
                android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
"""
content = content.replace("    private fun downloadAndSaveImage", func_base64 + "\n    private fun downloadAndSaveImage")

with open("app/src/main/java/com/example/ChatViewModel.kt", "w") as f:
    f.write(content)
