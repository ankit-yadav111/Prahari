package com.smart.hbalert

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.smart.hbalert.databinding.ActivityOptVerifyBinding
import java.util.concurrent.TimeUnit

class SendOtp : AppCompatActivity() {

    private lateinit var binding: ActivityOptVerifyBinding
    private lateinit var auth: FirebaseAuth
    private var verifiId:String?=null
    private var flag=true
    private var mode="create"
    private var secondsRemaining = 60
    private val handler = Handler(Looper.getMainLooper())
    lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOptVerifyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        runnable = object : Runnable {
            override fun run() {
                 binding.time.text=getString(R.string.resend_otp_time)+" "+ secondsRemaining.toString()+"s"
                secondsRemaining--
                if (secondsRemaining >= 0) {
                    handler.postDelayed(this, 1000)
                    binding.resend.isEnabled= false
                    val shapeDrawable = GradientDrawable()
                    shapeDrawable.cornerRadius = 20f // Change this value to adjust the corner radius
                    shapeDrawable.setColor(Color.parseColor("#808080"))
                    binding.resend.background=shapeDrawable// Red color
                } else {
                    binding.resend.isEnabled=true
                    val shapeDrawable = GradientDrawable()
                    shapeDrawable.cornerRadius = 20f // Change this value to adjust the corner radius
                    shapeDrawable.setColor(Color.parseColor("#FFC0C0"))
                    binding.resend.background=shapeDrawable
//                    binding.resend.setBackgroundColor(Color.parseColor("#FFC0C0"))
                }
            }
        }

        val mobile=intent.getStringExtra("mobile")
        mode= intent.getStringExtra("loc").toString()
        if(flag) {
            sendOtp(mobile.toString())
            flag = false
        }
        binding.resend.setOnClickListener{sendOtp(mobile.toString())}
        binding.verify.setOnClickListener{
            // This code goes inside the onClick method of your submit button
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

            val genOtp=binding.otp.text.toString()
            if (genOtp.length!=6){
                binding.warning.text=getString(R.string.wrong_input)
            }
            else{
            val credential = PhoneAuthProvider.getCredential(verifiId!!, genOtp)
            Log.d("Ankit","Cred:  $credential")
            Log.d("Ankit","Code:  $verifiId")
            signInWithPhoneAuthCredential(credential)
            }
        }
    }

    private fun sendOtp(mobile:String){
        secondsRemaining=60
        handler.postDelayed(runnable, 1000)
    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
//            Log.d("Ankit", "Verified $credential")
                signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.d("Ankit",e.toString())
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // Save verification ID and resending token so we can use them later
            verifiId = verificationId
            Log.d("Ankit","Code ki baat ho rahi h "+verifiId.toString())
        }

    }

    val options = PhoneAuthOptions.newBuilder(auth)
        .setPhoneNumber("+91$mobile")       // Phone number to verify
        .setTimeout(60, TimeUnit.SECONDS) // Timeout and unit
        .setActivity(this)                 // Activity (for callback binding)
        .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
        .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    if (mode=="create"){
                        startActivity(Intent(this,MainActivity::class.java))
                        finish()
                    }
                    else{
                        startActivity(Intent(this,ChangePassword::class.java))
                        finish()
                    }
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        binding.warning.text=getString(R.string.wrong_input)
                        Toast.makeText(this,"Invalid Code",Toast.LENGTH_LONG).show()
                    }
                    // Update UI
                }
            }
    }
}