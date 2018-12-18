package com.example.aria.keep_shaking

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.gson.Gson
import kotlinx.android.synthetic.main.login_layout.*
import kotlinx.android.synthetic.main.signup_layout.*
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    lateinit var okHttp: OkHttp
    lateinit var requestData: JSONObject
    lateinit var reponseData: JSONObject
    lateinit var pref : SharePreference
    private lateinit var loadingView: View
    private lateinit var loadingRoot: ViewGroup
    private var token = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)

        pref = SharePreference(this)
        okHttp = OkHttp(this)
        loadingRoot = window.decorView as ViewGroup
        loadingView = LayoutInflater.from(this).inflate(R.layout.loading_layout, loadingRoot, false)
//        test()
//        return
        token = pref.getToken()
        checkisLogin()
        loginEmail.clearFocus()
        loginPassword.clearFocus()
        login.setOnClickListener(listener)
        goSignup.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    fun checkisLogin(){
        if(token!=""){
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("From", "AutoLogin")
            intent.putExtra("Token", token)
            startActivity(intent)
        }
    }

    val listener = View.OnClickListener {
        if (loginEmail.text.toString() == "" ||
            loginPassword.text.toString() == ""
        )  Toast.makeText(this, "不可有欄位為空", Toast.LENGTH_LONG).show()
        else {
            loadingRoot.addView(loadingView)
            requestData = JSONObject()
            requestData.put("email", loginEmail.text.toString())
            requestData.put("password", loginPassword.text.toString())
            val json = requestData.toString()
            okHttp.request(json, "/api/login", ::cb, OkHttp.RequestType.POST)
        }
    }

    private fun cb(jsonObject: JSONObject) {
        runOnUiThread {
            loadingRoot.removeView(loadingView)
            if (jsonObject.get("result") == "success") {
                reponseData = jsonObject.get("data") as JSONObject
                val name = reponseData.get("name") as String
                val balance = reponseData.get("balance") as Int
                val token = reponseData.get("api_token") as String
                pref.saveToken(token)
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("UserInfo", Gson().toJson(UserInfo(name, balance, token)))
                intent.putExtra("From", "General Login")
                startActivity(intent)
            } else {
                Toast.makeText(this, "${jsonObject.get("message")}", Toast.LENGTH_LONG).show()
            }
        }
    }

//    fun test(){
//        loadingRoot.addView(loadingView)
//        requestData = JSONObject()
//        requestData.put("email", "121645")
//        requestData.put("password", "")
//        requestData.put("password_confirmation","")
//        val json = requestData.toString()
//        okHttp.request(json, "/api/register", ::a, OkHttp.RequestType.POST)
//    }
//
//    fun a(jsonObject: JSONObject){
//        println("***************888 test")
//    }
}