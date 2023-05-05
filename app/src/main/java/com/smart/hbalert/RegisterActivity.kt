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
import com.smart.hbalert.databinding.ActivityCreateAccountBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateAccountBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.login.setOnClickListener{
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }

        binding.mobile.setOnClickListener{
            binding.warning.text=""
        }
        binding.email.setOnClickListener{
            binding.warning.text=""
        }
        binding.name.setOnClickListener{
            binding.warning.text=""
        }
        binding.userName.setOnClickListener{
            binding.warning.text=""
        }

        //click on submit button
        binding.submit.setOnClickListener{
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            createAccount()
        }
    }

    private fun createAccount(){
        val name=binding.name.text.toString()
        val mobile=binding.mobile.text.toString()
        val email=binding.email.text.toString()
        val userName=binding.userName.text.toString()
        if (name!=""){
            if (mobile.length==10 && mobile.isDigitsOnly()){
                if (email!=""){
                    if (userName!=""){
                        //Check the Mobile number in data base
                        FirebaseAuth.getInstance().fetchSignInMethodsForEmail("$mobile@alert.com")
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val result = task.result
                                    if (result != null && result.signInMethods != null && result.signInMethods!!.isNotEmpty()) {
                                        binding.warning.text=getString(R.string.already_have_account)
                                    } else {

                                        sendOtp(mobile,name,email,userName.lowercase())
                                    }
                                } else {
                                    // Error occurred while checking if user exists
                                    Log.d("Ankit",task.exception.toString())
                                    Toast.makeText(this, "Error Occurred", Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                    else{
                        binding.warning.text=getString(R.string.userName)
                    }
                }
                else{
                    binding.warning.text=getString(R.string.email)
                }
            }
            else{
                binding.warning.text=getString(R.string.wrong_number)
            }
        }
        else{
            binding.warning.text=getString(R.string.blank_input)
        }
    }

    private fun sendOtp(mobile:String,name:String,email:String,userName:String){
        val intent= Intent(this,SendOtp::class.java)
        intent.putExtra("mobile",mobile)
        intent.putExtra("loc","create")
        intent.putExtra("email",email)
        intent.putExtra("name",name)
        intent.putExtra("user",userName)
        startActivity(intent)
        finish()
    }
}

