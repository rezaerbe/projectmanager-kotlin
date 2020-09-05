package com.erbe.projectmanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        sign_up_btn.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        sign_in_btn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }
}