import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old_theme_options = """val themeOptions = listOf(
                        Triple("Follow System", Color.Gray, "Auto-switches based on device setting"),
                        Triple("Pure black (AMOLED)", Color(0xFF000000), "Infinite contrast for OLED screens"),
                        Triple("Midnight Blue", Color(0xFF4A9EFF), "Deep space aesthetic with modern blue accents"),
                        Triple("Soft Light Gray", Color(0xFFEDEDED), "Clean and bright"),
                        Triple("Pure white", Color(0xFFFFFFFF), "Maximum brightness")
                    )"""

new_theme_options = """val themeOptions = listOf(
                        Triple("Follow System", Color.Gray, "Auto-switches based on device setting"),
                        Triple("Pure Black", Color(0xFF000000), "Deepest blacks for OLED"),
                        Triple("Dark Gray", Color(0xFF1C1C1E), "Smooth and easy on the eyes"),
                        Triple("Charcoal", Color(0xFF161618), "Darker neutral gray"),
                        Triple("True Gray", Color(0xFF3A3A3C), "Balanced mid-tone gray"),
                        Triple("Soft White", Color(0xFFFFFFFF), "Clean, bright light theme")
                    )"""

content = content.replace(old_theme_options, new_theme_options)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
