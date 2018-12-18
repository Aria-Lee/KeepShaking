package com.example.aria.keep_shaking

import android.content.Context
import com.google.gson.JsonObject
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class OkHttp(val context: Context) {

    enum class RequestType{
        POST, GET, DELETE
    }

    private val baseURL = context.resources.getString(R.string.baseURL)
    private var client = OkHttpClient()
    private val JSON = MediaType.get("application/json; charset=utf-8")
    lateinit var body : RequestBody


    fun request(dataJsonString: String?, url: String, cb: (JSONObject) -> Unit, requestType: RequestType){
        val builder = Request.Builder()
                .url(baseURL + url)
        when (requestType) {
            RequestType.POST -> {
                val header = Headers.Builder()
                    .add("Content-Type", "application/json")
                    .add("Accept", "application/json")
                    .build()
                builder.headers(header)

                body = RequestBody.create(JSON, dataJsonString!!)
                builder.post(body)
            }
            RequestType.GET -> {
                val header = Headers.Builder()
                    .add("Content-Type","application/json")
                    .add("Accept","application/json")
                    .build()
                builder.headers(header)
                builder.get()
            }
            RequestType.DELETE -> {
                val header = Headers.Builder()
                    .add("Content-Type","application/json")
                    .add("Accept","application/json")
                    .build()
                builder.headers(header)


                body = RequestBody.create(JSON, dataJsonString!!)
                builder.delete(body)
            }
        }

        val request = builder.build()

        client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { }

            override fun onResponse(call: Call, response: Response) {
                val responseString = response.body()?.string()!!
                val response = JSONObject(responseString)
                println("************ $responseString")
                cb.invoke(response)
            }
        })


    }

}