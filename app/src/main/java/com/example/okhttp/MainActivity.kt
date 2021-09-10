package com.example.okhttp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.EMPTY_REQUEST

import org.json.JSONArray
import org.json.JSONObject


private const val ENDPOINT =
    "http://10.0.2.2:3000"  // Im using json-server running on my localhost and emulator
private const val BOOKS_URI = "/books"
private const val TITLE = "title"
private const val ID = "id"


class MainActivity : AppCompatActivity() {
    private lateinit var textView: TextView
    private val typeJson = "application/json".toMediaType()
    var okHttpClient: okhttp3.OkHttpClient = okhttp3.OkHttpClient()
    var books2: MutableList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.textView)
        val books = mutableListOf<String>()
        Thread {
            addBook("Pushkin!")
        }.start()

        Thread {
            deleteBook(7)
        }.start()

        Thread {
            updateBook(9,"Dostoevskiy")
        }.start()

        Thread {
            getList(ENDPOINT+BOOKS_URI)
        }.start()
    }

    @WorkerThread
    fun getList(url:String) {
        val request: Request = Request.Builder().url(url).build()
        okHttpClient.newCall(request).enqueue(object : Callback {


            override fun onFailure(call: Call, e: java.io.IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {

                val responseData = response.body?.string()

                try {
                    val books = mutableListOf<String>()
                    val json = JSONArray(responseData)
                    for (i in 0 until json.length()) {
                        val jsonBook = json.getJSONObject(i)
                        val title = jsonBook.getString(TITLE)
                        books.add(title)
                    }
                    Handler(Looper.getMainLooper()).post {
                        textView.text = books.reduce { acc, s -> "$acc\n$s" }
                    }
                } catch (e: org.json.JSONException) {
                    e.printStackTrace()
                }
            }

        })
    }

    @WorkerThread
    fun addBook(book: String) {
        try {
        val json = JSONObject().apply {
            put(TITLE, book)
        }
        val request: Request = Request.Builder()
            .url("http://10.0.2.2:3000/books")
            .post(json.toString().toRequestBody(contentType = typeJson))
            .build()

        val call: Call = okHttpClient.newCall(request)
       call.execute()
        } catch (e: org.json.JSONException) {
            e.printStackTrace()
        }
    }

    @WorkerThread
    fun deleteBook(id:Int) {
        try {
        val request: Request = Request.Builder()
            .url("http://10.0.2.2:3000/books/$id")
            .delete(EMPTY_REQUEST)
            .build()
        val call: Call = okHttpClient.newCall(request)
        call.execute()
        } catch (e: org.json.JSONException) {
            e.printStackTrace()
        }
    }


    @WorkerThread
    fun updateBook(id:Int,book:String) {
        try {
        val json = JSONObject().apply {
            put(TITLE, book)
            put(ID,id)
        }
        val request: Request = Request.Builder()
            .url("http://10.0.2.2:3000/books/$id")
            .put(json.toString().toRequestBody(contentType = typeJson))
            .build()
        val call: Call = okHttpClient.newCall(request)
        call.execute()
        } catch (e: org.json.JSONException) {
            e.printStackTrace()
        }
    }


}