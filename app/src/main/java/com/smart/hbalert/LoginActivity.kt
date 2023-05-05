package com.smart.hbalert

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.smart.hbalert.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        //Click on Login Button
        binding.submit.setOnClickListener{
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            val mobile=binding.mobile.text.toString()
            loginAccount(mobile)
        }
        binding.mobile.setOnClickListener{
            binding.warning.text=""
        }
        //Create Account Activity
        binding.register.setOnClickListener{
            startActivity(Intent(this,RegisterActivity::class.java))
            finish()
        }

    }

    private fun loginAccount(mobile:String) {
        if (mobile.length!=10 && mobile.isDigitsOnly()){
            binding.warning.text=getString(R.string.wrong_number)
            Toast.makeText(this,"Enter valid number",Toast.LENGTH_LONG).show()
        }
        else{
            // Check user already exists or not
            FirebaseAuth.getInstance().fetchSignInMethodsForEmail("$mobile@alert.com")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val result = task.result
                        Log.d("Ankit",result.toString())
                        if (result != null && result.signInMethods != null && result.signInMethods!!.isNotEmpty()) {
                            val intent= Intent(this,SendOtp::class.java)
                            intent.putExtra("mobile",mobile)
                            intent.putExtra("loc","login")
                            startActivity(intent)
                            finish()
                        } else {
                            binding.warning.text=getString(R.string.do_not_have_account)
                        }
                    } else {
                        // Error occurred while checking if user exists
                        Log.d("Ankit",task.exception.toString())
                        Toast.makeText(this, "Error Occurred", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}