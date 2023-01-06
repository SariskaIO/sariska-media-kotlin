package org.sariska.myapplication

import android.util.Log
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException

object GetToken {
    @Throws(IOException::class)
    internal fun generateToken(userID: String?): String? {
        val client = OkHttpClient()
        val JSON = MediaType.parse("application/json; charset=utf-8")
        val url = "https://api.sariska.io/api/v1/misc/generate-token"
        val json = """{
    "apiKey": "249202aabed00b41363794b526eee6927bd35cbc9bac36cd3edcaa",
    "user": {
        "id": "dsds",
        "name": "SomeOne",
        "moderator": false,
        "email": "dipak@work.com",
        "avatar":"null"
    }
}"""
        Log.d("Generated Token", "generateToken: ")
        val body = RequestBody.create(JSON, json)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()
        try {
            client.newCall(request).execute().use { response ->
                var responseString = response.body()!!.string()
                responseString = "[$responseString]"
                val array = JSONArray(responseString)
                var finalResponse: String? = null
                for (i in 0 until array.length()) {
                    val `object` = array.getJSONObject(i)
                    finalResponse = `object`.getString("token")
                }
                return finalResponse
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            println("This cannot be done")
            return null
        }
    }
}