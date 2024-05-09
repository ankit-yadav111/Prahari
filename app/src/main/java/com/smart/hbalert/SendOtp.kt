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
import com.smart.hbalert.doa.UserDoa
import com.smart.hbalert.model.User
import java.util.concurrent.TimeUnit


class SendOtp : AppCompatActivity() {

    private lateinit var binding: ActivityOptVerifyBinding
    private lateinit var auth: FirebaseAuth
    private var verifyId:String?=null
    private var flag=true
    private var mode="create"
    private var secondsRemaining = 60
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

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
        binding.otp.setOnClickListener{
            binding.warning.text=""
        }
        val mobile= intent.getStringExtra("mobile")
        mode= intent.getStringExtra("loc").toString()
        if(flag) {
            sendOtp(mobile.toString())
            flag = false
        }
        binding.resend.setOnClickListener{
            binding.warning.text=""
            sendOtp(mobile.toString())
        }
        binding.verify.setOnClickListener{
            // This code goes inside the onClick method of your submit button
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

            val genOtp=binding.otp.text.toString()
            if (genOtp.length!=6){
                binding.warning.text=getString(R.string.wrong_input)
            }
            else{
            val credential = PhoneAuthProvider.getCredential(verifyId!!, genOtp)
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
                signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // Save verification ID and resending token so we can use them later
            verifyId = verificationId
        }

    }
        val formattedPhoneNumber = "+91$mobile".replace("\\s+".toRegex(), "")

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(formattedPhoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
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
                        createAccount()
                    }
                    else{
                        val intent=Intent(this, MainActivity::class.java)
                        intent.putExtra("mobile",intent.getStringExtra("mobile"))
                        startActivity(intent)
                        finish()
                    }
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        binding.warning.text=getString(R.string.wrong_input)
                        Toast.makeText(this,"Invalid Code",Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    private fun createAccount(){
        val name= intent.getStringExtra("name")
        val mobile=intent.getStringExtra("mobile")
        val userName=intent.getStringExtra("user")
        val email=intent.getStringExtra("email")
        val  user= auth.currentUser?.uid.toString()
        auth.createUserWithEmailAndPassword("$mobile@alert.com", "123456")
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this,"Account created successfully.", Toast.LENGTH_SHORT).show()
                    // Redirect to the main activity
                    // Save all the user information
                    val user = User(user,name,userName.toString(), mobile.toString(),email.toString())
                    UserDoa().addUser(user)
                    val intent=Intent(this, MainActivity::class.java)
                    intent.putExtra("mobile",mobile)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Account creation failed.", Toast.LENGTH_SHORT).show()
                    auth.signOut()
                }
            }
    }
}