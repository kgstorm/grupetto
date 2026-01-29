package com.spop.poverlay.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object NetworkClient {
    private val client = OkHttpClient()

    suspend fun postExerciseResult(url: String, token: String, jsonBody: String): Boolean = withContext(Dispatchers.IO) {
        val mediaType = "application/json".toMediaType()
        val body = jsonBody.toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { resp ->
            resp.isSuccessful
        }
    }
}
