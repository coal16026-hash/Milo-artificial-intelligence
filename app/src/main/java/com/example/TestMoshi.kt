package com.example

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@JsonClass(generateAdapter = true)
data class TestMessage(
    val role: String,
    val content: Any
)

fun testMoshi() {
    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    val adapter = moshi.adapter(TestMessage::class.java)
    val json1 = adapter.toJson(TestMessage("user", "Hello"))
    val json2 = adapter.toJson(TestMessage("user", listOf(mapOf("type" to "text", "text" to "Hello"))))
    println(json1)
    println(json2)
}
