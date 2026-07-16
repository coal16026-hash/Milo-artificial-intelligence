import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "colors.userBubble" in line and i + 1 not in [906]:
        # only keep userBubble at 906 (the MessageBubble for user messages)
        # Wait, what about other User bubbles? Let me check lines manually
        pass
