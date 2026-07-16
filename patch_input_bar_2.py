import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

target = """                            onDocumentClick = {
                                documentLauncher.launch(arrayOf("*/*"))
                            },
                            onModelSelected = { name, _ -> viewModel.selectModel(name, "") }
                        )"""

replacement = """                            onDocumentClick = {
                                documentLauncher.launch(arrayOf("*/*"))
                            },
                            onGenerateImageClick = { showInlineGenerateDialog = true },
                            onModelSelected = { name, _ -> viewModel.selectModel(name, "") }
                        )"""
content = content.replace(target, replacement)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
