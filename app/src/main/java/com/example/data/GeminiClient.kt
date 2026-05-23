package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeServiceRequest(userDescription: String): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API_KEY_MISSING"
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val prompt = """
            You are "KaamChha Assistant" in a Nepali on-demand trust marketplace. 
            Interpret the following home maintenance problem described by a customer.
            Analyze which worker category fits best (Electrician, Plumber, Mason, Carpenter, Painter, or Technician).
            Suggest a budget range in Nepali Rupees (NRs) representing a fair price.
            Summarize brief diagnostic advice in 2 clear points.
            Include encouragement about safety and using our secure Escrow feature.
            
            Customer description: "$userDescription"
            
            Return a well-formatted response with:
            1. Recommended Category: [Insert Category]
            2. Suggested Escrow Base: NRs. [Insert estimated price, e.g. 500 - 1500]
            3. Smart Diagnostic: [Insert analysis]
            4. Next Step recommendation: [Instruct them to post or choose emergency mode]
            
            Keep it professional, encouraging, and informative!
        """.trimIndent()

        // Build standard JSON representation payload natively
        val requestJson = JSONObject().apply {
            val partsArray = JSONArray().apply {
                put(JSONObject().put("text", prompt))
            }
            val contentObj = JSONObject().put("parts", partsArray)
            put("contents", JSONArray().put(contentObj))
            
            val configObj = JSONObject().apply {
                put("temperature", 0.4)
                put("maxOutputTokens", 1000)
            }
            put("generationConfig", configObj)
        }

        val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
        val okRequest = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(okRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "API_ERROR: HTTP ${response.code} (Check your API Key / Billing status)"
                }
                val bodyStr = response.body?.string() ?: return@withContext "API_ERROR: Empty response body"
                val responseJson = JSONObject(bodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                val content = candidates?.optJSONObject(0)?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val text = parts?.optJSONObject(0)?.optString("text")
                text ?: "Unable to evaluate this. Try describing in details."
            }
        } catch (e: Exception) {
            "API_ERROR: ${e.localizedMessage}"
        }
    }
}
