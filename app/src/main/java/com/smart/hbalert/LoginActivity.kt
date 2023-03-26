package com.smart.hbalert

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
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
    private var visible=0
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
            val pass=binding.password.text.toString()
            loginAccount(mobile,pass)
        }

        //Create Account Activity
        binding.register.setOnClickListener{
            startActivity(Intent(this,RegisterActivity::class.java))
            finish()
        }

        //Forget Passwod Activity
        binding.forgetPassword.setOnClickListener{
            startActivity(Intent(this,ForgetPasswordActivity::class.java))
        }

        //Password Visiblity and Hiding
        binding.passImg.setOnClickListener{
            visible = if(visible==1){
                binding.password.transformationMethod=HideReturnsTransformationMethod.getInstance()
                binding.passImg.setImageResource(R.drawable.ic_baseline_remove_red_eye_24)
                0
            } else{
                binding.password.transformationMethod=PasswordTransformationMethod.getInstance()
                binding.passImg.setImageResource(R.drawable.ic_baseline_visibility_off_24)
                1
            }
            binding.password.setSelection(binding.password.length())
        }
    }

    private fun loginAccount(mobile:String, pass:String) {
        if (mobile.length!=10 && mobile.isDigitsOnly()){
            //Check the Password in database
            binding.warning.text= getString(R.string.wrong_number_password)
        }
        else{
            val intent =Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}