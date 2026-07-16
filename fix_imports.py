def fix_file(filename):
    with open(filename, 'r') as f:
        content = f.read()
    content = content.replace("package com.exampleimport", "package com.example\nimport")
    content = content.replace("toMediaTypeOrNullimport", "toMediaTypeOrNull\nimport")
    content = content.replace("ImageGeneratorScreenpackage", "ImageGeneratorScreen\npackage")
    with open(filename, 'w') as f:
        f.write(content)

fix_file('app/src/main/java/com/example/ChatViewModel.kt')
fix_file('app/src/main/java/com/example/MainActivity.kt')
