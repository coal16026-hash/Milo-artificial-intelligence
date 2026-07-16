import sys

with open("app/src/main/java/com/example/ChatViewModel.kt", "r") as f:
    content = f.read()

content = content.replace(
    'okhttp3.MediaType.Companion.parse("application/json")',
    'okhttp3.MediaType.parse("application/json")' # Revert first
)

content = content.replace(
    'okhttp3.MediaType.parse("application/json")',
    '"application/json".toMediaTypeOrNull()'
)

# And make sure toMediaTypeOrNull is imported, or use the full path:
content = content.replace(
    '"application/json".toMediaTypeOrNull()',
    'okhttp3.MediaType.Companion.parse("application/json")' # Wait, Companion.parse is also deprecated?
)
