package com.erbe.projectmanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import com.erbe.projectmanager.firebase.FireStoreHandler
import com.erbe.projectmanager.models.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : BaseActivity() {

    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setActionBar()

        auth = FirebaseAuth.getInstance()

        sign_in_page_btn.setOnClickListener {
            signInUser()
        }
    }

    private fun setActionBar() {

        setSupportActionBar(toolbar_sign_in_activity)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_black)
        }
        toolbar_sign_in_activity.setNavigationOnClickListener{onBackPressed()}
    }

    private fun signInUser() {
        val email = sign_in_email_et.text.toString().trim { it <= ' ' }
        val password = sign_in_password_et.text.toString().trim { it <= ' ' }

        if (validateForm(email, password)) {
            showProgressDialog()
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        FireStoreHandler().loadUserData(this)
                    }
                    else {
                        Toast.makeText(baseContext, "Authentication failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    fun signInSuccess(user : User) {
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun validateForm(email:String, password:String) : Boolean{
        return when {
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter an email address")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter a password")
                false
            }else->{
                true
            }
        }
    }
}