import sys

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# First replace AppColors data class
old_app_colors = """data class AppColors(
    val background: Color,
    val inputBg: Color,
    val bubbleGray: Color,
    val aiText: Color,
    val textGray: Color,
    val iconGray: Color,
    val plusBg: Color,
    val borderGray: Color,
    val primary: Color,
    val isDark: Boolean
)"""

new_app_colors = """data class AppColors(
    val background: Color,
    val inputBg: Color,
    val bubbleGray: Color,
    val userBubble: Color,
    val aiText: Color,
    val textGray: Color,
    val iconGray: Color,
    val plusBg: Color,
    val borderGray: Color,
    val primary: Color,
    val isDark: Boolean
)"""

content = content.replace(old_app_colors, new_app_colors)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
