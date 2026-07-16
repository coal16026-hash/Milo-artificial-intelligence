import re

with open("app/src/main/java/com/example/ChatViewModel.kt", "r") as f:
    content = f.read()

# Add dao properties
dao_old = "private val dao = db.chatDao()"
dao_new = "private val dao = db.chatDao()\n    private val imageDao = db.generatedImageDao()\n    val recentImages: kotlinx.coroutines.flow.StateFlow<List<GeneratedImageEntity>> = imageDao.getAllGeneratedImages().kotlinx_coroutines_flow_stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Eagerly, emptyList())"
dao_new = dao_new.replace("kotlinx_coroutines_flow_stateIn", "stateIn")

content = content.replace(dao_old, dao_new)

# Add imports
imports_old = "import kotlinx.coroutines.flow.asStateFlow"
imports_new = "import kotlinx.coroutines.flow.asStateFlow\nimport kotlinx.coroutines.flow.SharingStarted\nimport kotlinx.coroutines.flow.stateIn\nimport java.io.File\nimport java.io.FileOutputStream\nimport java.net.URL"
content = content.replace(imports_old, imports_new)

# Modify generateImage
generate_old = """                        val imageUrl = dataArray.getJSONObject(0).optString("url")
                        if (imageUrl.isNotEmpty()) {
                            withContext(Dispatchers.Main) {
                                onResult(imageUrl, null)
                            }
                        } else {"""
generate_new = """                        val imageUrl = dataArray.getJSONObject(0).optString("url")
                        if (imageUrl.isNotEmpty()) {
                            val localUri = downloadAndSaveImage(imageUrl)
                            val finalUrl = localUri ?: imageUrl
                            
                            val entity = GeneratedImageEntity(
                                id = UUID.randomUUID().toString(),
                                prompt = prompt,
                                imageUrl = finalUrl,
                                timestamp = System.currentTimeMillis()
                            )
                            imageDao.insertGeneratedImage(entity)
                            
                            withContext(Dispatchers.Main) {
                                onResult(finalUrl, null)
                            }
                        } else {"""
content = content.replace(generate_old, generate_new)

# Add downloadAndSaveImage method
download_func = """
    private fun downloadAndSaveImage(imageUrl: String): String? {
        try {
            val url = URL(imageUrl)
            val connection = url.openConnection()
            connection.connect()
            val inputStream = connection.getInputStream()
            
            val file = File(context.cacheDir, "img_${System.currentTimeMillis()}.png")
            val outputStream = FileOutputStream(file)
            
            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.close()
            inputStream.close()
            
            return file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
"""

content = content.replace("fun generateImage", download_func + "\n    fun generateImage")

with open("app/src/main/java/com/example/ChatViewModel.kt", "w") as f:
    f.write(content)
