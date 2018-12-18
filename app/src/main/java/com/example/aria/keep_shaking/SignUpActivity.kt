package com.example.aria.keep_shaking

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.signup_layout.*
import org.json.JSONObject

class SignUpActivity : AppCompatActivity() {

    private lateinit var okHttp: OkHttp
    lateinit var requestData: JSONObject
    lateinit var reponseData: JSONObject
    private lateinit var loadingView: View
    private lateinit var loadingRoot: ViewGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_layout)

        okHttp = OkHttp(this)
        loadingRoot = window.decorView as ViewGroup
        loadingView = LayoutInflater.from(this).inflate(R.layout.loading_layout, loadingRoot, false)
        signUp.setOnClickListener(listener)
    }

    val listener = View.OnClickListener {
        if (signupName.text.toString() == "" ||
            signupEmail.text.toString() == "" ||
            signupPassword.text.toString() == "" ||
            signupComfirmPassword.text.toString() == ""
        )  Toast.makeText(this, "不可有欄位為空", Toast.LENGTH_LONG).show()
        else if(signupPassword.text.toString() != signupComfirmPassword.text.toString())
            Toast.makeText(this, "密碼與確認密碼不符", Toast.LENGTH_LONG).show()
        else {
            loadingRoot.addView(loadingView)
            requestData = JSONObject()
            requestData.put("name", signupName.text.toString())
            requestData.put("email", signupEmail.text.toString())
            requestData.put("password", signupPassword.text.toString())
            requestData.put("password_confirmation", signupComfirmPassword.text.toString())
            val json = requestData.toString()
            okHttp.request(json, "/api/register", ::cb, OkHttp.RequestType.POST)
        }
    }

    private fun cb(jsonObject: JSONObject) {
        runOnUiThread {

            loadingRoot.removeView(loadingView)
            if (jsonObject.get("result") == "success") {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "${jsonObject.get("message")}", Toast.LENGTH_LONG).show()
            }
        }
    }
}