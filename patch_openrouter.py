import re

with open("app/src/main/java/com/example/ChatViewModel.kt", "r") as f:
    code = f.read()

target = "    val message: OpenRouterMessage?,"
replacement = "    val message: OpenRouterResponseMessage?,"
code = code.replace(target, replacement)

target2 = """data class OpenRouterMessage(
    val role: String,
    val content: String?,
    @com.squareup.moshi.Json(name = "reasoning")
    val reasoning: String? = null,
    @com.squareup.moshi.Json(name = "reasoning_content")
    val reasoningContent: String? = null
)"""
replacement2 = """data class OpenRouterResponseMessage(
    val role: String,
    val content: String?,
    @com.squareup.moshi.Json(name = "reasoning")
    val reasoning: String? = null,
    @com.squareup.moshi.Json(name = "reasoning_content")
    val reasoningContent: String? = null
)"""
code = code.replace(target2, replacement2)

with open("app/src/main/java/com/example/ChatViewModel.kt", "w") as f:
    f.write(code)

