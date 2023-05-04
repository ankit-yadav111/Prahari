package com.smart.hbalert

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.smart.hbalert.databinding.ActivityMobileNumberBinding


class ForgetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMobileNumberBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMobileNumberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.register.setOnClickListener{
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        binding.submit.setOnClickListener{
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            val mobile=binding.mobile.text.toString()
            if (mobile.length==10 && mobile.isDigitsOnly()){

                // if mobile number not  in Database
                checkIfUserExists(mobile)
                //else:: binding.warning.text=R.string.already_have_account.toString()
            }
            else{
                binding.warning.text=getString(R.string.wrong_number)
            }
        }
    }

    private fun sendOtp(mobile:String){
        val intent= Intent(this,SendOtp::class.java)
        intent.putExtra("mobile",mobile)
        intent.putExtra("loc","forget")
        startActivity(intent)
        finish()
    }

    private fun checkIfUserExists(mobile: String) {
        auth.fetchSignInMethodsForEmail("$mobile@example.com")
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val result = task.result
                    val signInMethods = result.signInMethods
                    if (signInMethods != null && signInMethods.isNotEmpty()) {
                        sendOtp(mobile)
                    } else {
                        // User does not exist
                        Toast.makeText(this,"User does not exist", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this,"Failed to Connect",Toast.LENGTH_LONG).show()
                }
            }
    }


}