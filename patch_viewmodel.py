import sys

with open("app/src/main/java/com/example/ChatViewModel.kt", "r") as f:
    content = f.read()

method = """
    fun generateImage(prompt: String, onResult: (String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = org.json.JSONObject()
                json.put("model", "agnes-image-2.1-flash")
                json.put("prompt", prompt)
                
                val requestBody = okhttp3.RequestBody.create(
                    okhttp3.MediaType.parse("application/json"),
                    json.toString()
                )
                
                val request = okhttp3.Request.Builder()
                    .url("https://agnes-ai.com/v1/images/generations")
                    .addHeader("Authorization", "Bearer skyATs9uzPnSZAPgSGHLkRNjQy1sCHxi96rSGi7NvizZ52Iuf1")
                    .post(requestBody)
                    .build()
                    
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                val response = client.newCall(request).execute()
                val responseBody = response.body()?.string()
                
                if (response.isSuccessful && responseBody != null) {
                    val responseObj = org.json.JSONObject(responseBody)
                    val dataArray = responseObj.optJSONArray("data")
                    if (dataArray != null && dataArray.length() > 0) {
                        val imageUrl = dataArray.getJSONObject(0).optString("url")
                        withContext(Dispatchers.Main) {
                            onResult(imageUrl)
                        }
                    } else {
                        withContext(Dispatchers.Main) { onResult(null) }
                    }
                } else {
                    withContext(Dispatchers.Main) { onResult(null) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { onResult(null) }
            }
        }
    }
"""

if "fun generateImage(" not in content:
    content = content.replace("class ChatViewModel(private val context: Context) : ViewModel() {", "class ChatViewModel(private val context: Context) : ViewModel() {\n" + method)

with open("app/src/main/java/com/example/ChatViewModel.kt", "w") as f:
    f.write(content)
