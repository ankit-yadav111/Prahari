package com.smart.hbalert

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.smart.hbalert.contact.ContactViewModel
import com.smart.hbalert.databinding.ActivityMainBinding
import com.smart.hbalert.doa.UserDoa

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: ContactViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    var locationManager: LocationManager? = null
    var latitude = 0.0
    var longitude = 0.0
    private var flag=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        val mobile=intent.getStringExtra("mobile")

        val user=auth.currentUser
        if(user!=null) {
            val data = UserDoa().getUserById(mobile.toString())
            data.addOnSuccessListener { document ->
                if (document != null) {
                    val name = document.get("userName").toString().replaceFirstChar { it.uppercase() }
                    binding.welcome.text="Welcome $name"
                }
            }
        }
        viewModel= ViewModelProvider(this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application))[ContactViewModel::class.java]

        if((ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED)
            &&(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(
                  this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)&&
            (ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED))
        {
            checkPermission()
        }
        if (flag==1){
        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
        locationManager!!.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0, 10f, locationListenerGPS
        )}

        binding.login.setOnClickListener{
            auth.signOut()
            onStart()
        }

        binding.addContacts.setOnClickListener{
            startActivity(Intent(this,ContactActivity::class.java))
        }

        binding.enableAlert.setOnClickListener{
            enabledAlert()
        }

        binding.sosMode.setOnClickListener{
            sosMode()
        }

        binding.Setting.setOnClickListener{
            Toast.makeText(this,"Setting",Toast.LENGTH_SHORT).show()
//            startActivity(Intent,SettingActivity::class.java)
        }

        binding.PairSmartBand.setOnClickListener{
            Toast.makeText(this,"Connect With Your SmartBand",Toast.LENGTH_SHORT).show()
        }

    }

    private fun sosMode() {
        if (!fetchSize()){
            sendMessage()
        }
        else{
            Toast.makeText(this, "Add Emergency Contacts",Toast.LENGTH_LONG).show()
        }
    }

    private fun enabledAlert() {
        Toast.makeText(this,"Enabled Alert",Toast.LENGTH_SHORT).show()
    }


    //Fetch the Size of Contact List
    private fun fetchSize():Boolean{
        var n=0
        viewModel.allCont.observe(this,  Observer{list->
            list?.let {
                n=list.size
            }
        })
        return n==0
    }

    // Send the Emergency
    private fun sendMessage(){
        val context: Context = this // or getApplicationContext()
        val smsManager = context.getSystemService(SmsManager::class.java)
        val strUri = "http://maps.google.com/maps?q=loc:$latitude,$longitude (Help!)"
        viewModel.allCont.observe(this,  Observer{list->
            list?.let {
                var i=0
                while(i<list.size){
                    val name=list[i].name
                    val  number= list[i].number
                    val msg = "Hii $name,I am in trouble.\nPlease Help!\nMap Link: $strUri \n"
                    smsManager.sendTextMessage(number,null,
                        msg,null,null)
                    i++
                }
            }
        })

    }

    // Check ALl the permissions are granted
    private fun checkPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.SEND_SMS,Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO),
            RequestCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== RequestCode && grantResults.isNotEmpty()){
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            flag=1
        }
        else{
            Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show()
            flag=0
            finish()
        }
    }

    companion object{
        const val RequestCode= 1
    }

    private var locationListenerGPS: LocationListener = object : LocationListener {

        override fun onLocationChanged(location: Location) {
            latitude = location.latitude
            longitude = location.longitude
            locationManager?.removeUpdates(this)
        }

        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser == null){
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}