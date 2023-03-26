package com.smart.hbalert

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import com.smart.hbalert.databinding.ActivityCreateAccountBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateAccountBinding
    private var visible=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.login.setOnClickListener{
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }

        //click on submit button
        binding.submit.setOnClickListener{
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            createAccount()
        }

        //Password Visiblity and Hiding
        binding.passImg.setOnClickListener{
            visible = if(visible==1){
                binding.password.transformationMethod= HideReturnsTransformationMethod.getInstance()
                binding.passImg.setImageResource(R.drawable.ic_baseline_remove_red_eye_24)
                0
            } else{
                binding.password.transformationMethod= PasswordTransformationMethod.getInstance()
                binding.passImg.setImageResource(R.drawable.ic_baseline_visibility_off_24)
                1
            }
        }
    }

    private fun createAccount(){
        val name=binding.name.text.toString()
        val mobile=binding.mobile.text.toString()
        val pass=binding.password.text.toString()
        val passCon=binding.confirmPassword.text.toString()
        if (name!=""){
            if (mobile.length==10 && mobile.isDigitsOnly()){
                if (pass.length>5){
                    if (passCon.length>5 && pass==passCon){

                        //Check the Mobile number in data base

                        sendOtp(mobile,name,pass)

                    }
                    else{
                        binding.warning.text=getString(R.string.password_not_match)
                    }
                }
                else{
                    binding.warning.text=getString(R.string.short_password)
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

    private fun sendOtp(mobile:String,name:String,password:String){
        val intent= Intent(this,SendOtp::class.java)
        intent.putExtra("mobile",mobile)
        intent.putExtra("loc","create")
        intent.putExtra("pass",password)
        intent.putExtra("name",name)
        startActivity(intent)
        finish()
    }
}

