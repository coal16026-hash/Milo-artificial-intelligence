import sys

with open("app/src/main/java/com/example/ChatViewModel.kt", "r") as f:
    content = f.read()

content = content.replace(
    'okhttp3.MediaType.parse("application/json")',
    'okhttp3.MediaType.Companion.parse("application/json")'
)

content = content.replace(
    'response.body()?.string()',
    'response.body?.string()'
)

with open("app/src/main/java/com/example/ChatViewModel.kt", "w") as f:
    f.write(content)
