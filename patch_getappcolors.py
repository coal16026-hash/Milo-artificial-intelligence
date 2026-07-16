import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# We need to replace the entire getAppColors function
old_func_pattern = r"@Composable\nfun getAppColors\(themeSetting: String\): AppColors \{.*?\n\}"

new_func = """@Composable
fun getAppColors(themeSetting: String): AppColors {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeSetting) {
        "Pure Black", "Dark Gray", "Charcoal", "True Gray" -> true
        "Soft White" -> false
        "Follow System" -> isSystemDark
        else -> isSystemDark
    }
    
    val bg = when (themeSetting) {
        "Pure Black" -> Color(0xFF000000)
        "Dark Gray" -> Color(0xFF1C1C1E)
        "Charcoal" -> Color(0xFF161618)
        "True Gray" -> Color(0xFF3A3A3C)
        "Soft White" -> Color(0xFFFFFFFF)
        "Follow System" -> if (isSystemDark) Color(0xFF000000) else Color(0xFFFFFFFF)
        else -> if (isSystemDark) Color(0xFF000000) else Color(0xFFFFFFFF)
    }

    val bubbleGray = when (themeSetting) {
        "Pure Black" -> Color(0xFF171717)
        "Dark Gray" -> Color(0xFF2C2C2E)
        "Charcoal" -> Color(0xFF232326)
        "True Gray" -> Color(0xFF48484A)
        "Soft White" -> Color(0xFFF2F2F2)
        "Follow System" -> if (isSystemDark) Color(0xFF171717) else Color(0xFFF2F2F2)
        else -> if (isSystemDark) Color(0xFF171717) else Color(0xFFF2F2F2)
    }

    val userBubble = when (themeSetting) {
        "Pure Black" -> Color(0xFF3A3A3A)
        "Dark Gray" -> Color(0xFF48484A)
        "Charcoal" -> Color(0xFF3A3A3D)
        "True Gray" -> Color(0xFF5A5A5D)
        "Soft White" -> Color(0xFFE8E8E8)
        "Follow System" -> if (isSystemDark) Color(0xFF3A3A3A) else Color(0xFFE8E8E8)
        else -> if (isSystemDark) Color(0xFF3A3A3A) else Color(0xFFE8E8E8)
    }

    val aiText = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0A0A0A)
    
    val primary = if (isDark) Color(0xFFEDEDED) else Color(0xFF111111)
    val inputBg = if (isDark) Color(0xFF212121) else Color(0xFFE5E5EA)
    val textGray = if (isDark) Color(0xFFB4B4B4) else Color(0xFF6B6B6B)
    val iconGray = if (isDark) Color(0xFF8E8E8E) else Color(0xFF71717A)
    val plusBg = if (isDark) Color(0xFF171717) else Color(0xFFD4D4D8)
    val borderGray = if (isDark) Color(0xFF2A2A2A) else Color(0xFFD1D5DB)

    return AppColors(bg, inputBg, bubbleGray, userBubble, aiText, textGray, iconGray, plusBg, borderGray, primary, isDark)
}"""

content = re.sub(old_func_pattern, new_func, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
