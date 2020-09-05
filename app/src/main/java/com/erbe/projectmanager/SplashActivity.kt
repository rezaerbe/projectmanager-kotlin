package com.erbe.projectmanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.erbe.projectmanager.firebase.FireStoreHandler

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        Handler().postDelayed({

            var currentUserID = FireStoreHandler().getCurrentUserID()
            if (currentUserID.isNotEmpty()) {
                startActivity(Intent(this, MainActivity::class.java))
            }
            else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 1000)
    }
}