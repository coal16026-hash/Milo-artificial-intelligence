import re

with open("app/src/main/java/com/example/ChatViewModel.kt", "r") as f:
    code = f.read()

target = """                val request = OpenRouterRequest(
                    model = "glm-5.2",
                    messages = listOf(OpenRouterMessage(role = "user", content = titlePrompt))
                )"""

replacement = """                val request = OpenRouterRequest(
                    model = "glm-5.2",
                    messages = listOf(OpenRouterRequestMessage(role = "user", content = listOf(ContentPart(type="text", text=titlePrompt))))
                )"""

code = code.replace(target, replacement)

with open("app/src/main/java/com/example/ChatViewModel.kt", "w") as f:
    f.write(code)

