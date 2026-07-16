import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old_appcolors = """    val primary: Color,
    val isDark: Boolean
)"""
new_appcolors = """    val primary: Color,
    val onPrimary: Color,
    val isDark: Boolean
)"""
content = content.replace(old_appcolors, new_appcolors)

old_primary = "val primary = if (isDark) Color(0xFFEDEDED) else Color(0xFF111111)"
new_primary = "val primary = if (isDark) Color(0xFFEDEDED) else Color(0xFF111111)\n    val onPrimary = if (isDark) Color(0xFF0A0A0A) else Color(0xFFFFFFFF)"
content = content.replace(old_primary, new_primary)

old_return = "return AppColors(bg, inputBg, bubbleGray, userBubble, aiText, textGray, iconGray, plusBg, borderGray, primary, isDark)"
new_return = "return AppColors(bg, inputBg, bubbleGray, userBubble, aiText, textGray, iconGray, plusBg, borderGray, primary, onPrimary, isDark)"
content = content.replace(old_return, new_return)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
