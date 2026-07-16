package com.example

object ImageStyles {
    val styleSuffixMap = mapOf(
        "Realistic" to ", photorealistic, high detail, natural lighting, 8k",
        "Anime" to ", anime style, cel shaded, vibrant colors, Studio Ghibli inspired linework",
        "Digital Art" to ", digital painting, detailed brushwork, artstation quality",
        "Watercolor" to ", watercolor painting, soft edges, paper texture, hand-painted",
        "3D Render" to ", 3D render, octane render, subsurface scattering, studio lighting",
        "Sketch" to ", pencil sketch, hand-drawn linework, crosshatching",
        "Cinematic" to ", cinematic composition, dramatic lighting, film grain, wide-angle",
        "Pixel Art" to ", pixel art, 16-bit style, limited color palette",
        "Default" to ""
    )

    val supportedSizes = listOf(
        "1024x1024",
        "1024x768",
        "768x1024"
    )
}
