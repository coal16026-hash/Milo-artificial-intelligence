import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

target = """                            onDocumentClick = {
                                documentPickerLauncher.launch("*/*")
                            },
                            onModelSelected = { model, code -> viewModel.setSelectedModel(model, code) }
                        )"""

replacement = """                            onDocumentClick = {
                                documentPickerLauncher.launch("*/*")
                            },
                            onGenerateImageClick = { showInlineGenerateDialog = true },
                            onModelSelected = { model, code -> viewModel.setSelectedModel(model, code) }
                        )"""

if target in content:
    content = content.replace(target, replacement)
else:
    print("TARGET NOT FOUND")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
