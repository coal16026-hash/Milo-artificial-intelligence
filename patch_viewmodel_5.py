with open("app/src/main/java/com/example/ChatViewModel.kt", "r") as f:
    content = f.read()

content = content.replace(
    'okhttp3.MediaType.Companion.parse("application/json")',
    '"application/json".toMediaTypeOrNull()'
)
content = content.replace(
    'okhttp3.MediaType.parse("application/json")',
    '"application/json".toMediaTypeOrNull()'
)

if "import okhttp3.MediaType.Companion.toMediaTypeOrNull" not in content:
    content = "import okhttp3.MediaType.Companion.toMediaTypeOrNull\n" + content

with open("app/src/main/java/com/example/ChatViewModel.kt", "w") as f:
    f.write(content)
