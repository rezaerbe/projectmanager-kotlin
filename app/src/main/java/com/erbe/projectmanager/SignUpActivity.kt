package com.erbe.projectmanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import com.erbe.projectmanager.firebase.FireStoreHandler
import com.erbe.projectmanager.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setActionBar()

        sign_up_page_btn.setOnClickListener {
            registerUser()
        }
    }

    private fun setActionBar() {
        setSupportActionBar(toolbar_sign_up_activity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_black)
        }
        toolbar_sign_up_activity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun registerUser() {

        val name = name_et.text.toString().trim { it <= ' ' }
        val email = email_et.text.toString().trim { it <= ' ' }
        val password = password_et.text.toString().trim { it <= ' ' }

        if (validateForm(name, email, password)) {
            showProgressDialog()
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        val firebaseUser : FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!
                        val user = User(firebaseUser.uid, name, registeredEmail)
                        FireStoreHandler().registerUser(this, user)
                    }
                    else {
                        Toast.makeText(this, "Registration Failed. Try Again", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    fun userRegisteredSuccess() {
        Toast.makeText(this, "You have successfully registered", Toast.LENGTH_SHORT).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }

    private fun validateForm(name : String, email : String, password : String) : Boolean {

        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Please enter a name")
                false
            }
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter a email")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter a password")
                false
            }
            else -> {
                true
            }
        }
    }
}