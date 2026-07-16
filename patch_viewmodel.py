import re

with open("app/src/main/java/com/example/ChatViewModel.kt", "r") as f:
    content = f.read()

old_func_pattern = r"fun generateImage\(prompt: String, onResult: \(String\?\) -> Unit\) \{.*?\n    \}"

new_func = """fun generateImage(prompt: String, onResult: (String?, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = org.json.JSONObject()
                json.put("model", "agnes-image-2.1-flash")
                json.put("prompt", prompt)
                json.put("size", "1024x768")
                
                val extraBody = org.json.JSONObject()
                extraBody.put("response_format", "url")
                json.put("extra_body", extraBody)
                
                val requestBody = okhttp3.RequestBody.create(
                    "application/json".toMediaTypeOrNull(),
                    json.toString()
                )
                
                val request = okhttp3.Request.Builder()
                    .url("https://apihub.agnes-ai.com/v1/images/generations")
                    .addHeader("Authorization", "Bearer ${com.example.BuildConfig.AGNES_API_KEY}")
                    .post(requestBody)
                    .build()
                    
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                
                if (response.isSuccessful && responseBody != null) {
                    val responseObj = org.json.JSONObject(responseBody)
                    val dataArray = responseObj.optJSONArray("data")
                    if (dataArray != null && dataArray.length() > 0) {
                        val imageUrl = dataArray.getJSONObject(0).optString("url")
                        if (imageUrl.isNotEmpty()) {
                            withContext(Dispatchers.Main) {
                                onResult(imageUrl, null)
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                onResult(null, "No image returned")
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) { 
                            onResult(null, "No image returned") 
                        }
                    }
                } else {
                    val errorMsg = responseBody ?: "Unknown error (HTTP ${response.code})"
                    withContext(Dispatchers.Main) {
                        onResult(null, "Error: $errorMsg")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(null, e.message ?: "Network error")
                }
            }
        }
    }"""

content = re.sub(old_func_pattern, new_func, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ChatViewModel.kt", "w") as f:
    f.write(content)
