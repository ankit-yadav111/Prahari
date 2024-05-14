package com.smart.hbalert

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.smart.hbalert.contact.ContactViewModel
import com.smart.hbalert.databinding.ActivityMainBinding
import com.smart.hbalert.doa.UserDoa
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: ContactViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    var locationManager: LocationManager? = null
    var latitude = 0.0
    var longitude = 0.0
    var min = 0.0
    var max = 0.0
    private var flag=0
    private lateinit var sharedPreferences: SharedPreferences
    private val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1
    private val TAG = "Ankit"
    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
        .build()



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
                    if(name!=null)
                        binding.welcome.text = "Welcome"
                    binding.welcome.text="Welcome"
                }
            }
        }

        viewModel= ViewModelProvider(this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application))[ContactViewModel::class.java]

        if((ContextCompat.checkSelfPermission(this,Manifest.permission.ACTIVITY_RECOGNITION)!= PackageManager.PERMISSION_GRANTED)
            &&(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(
                  this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)&&
            (ContextCompat.checkSelfPermission(this,Manifest.permission.BODY_SENSORS)!= PackageManager.PERMISSION_GRANTED))
        {
            checkPermission()
        }else{
            flag=1
        }
        if (flag==1){
            fetchSize()
            locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
            locationManager!!.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0, 10f, locationListenerGPS)

            sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
            min = sharedPreferences.getString("Min", "").toString().toDouble()
            max = sharedPreferences.getString("Max", "").toString().toDouble()
        }

        binding.login.setOnClickListener{
            auth.signOut()
            onStart()
        }

        binding.addContacts.setOnClickListener{
            startActivity(Intent(this,ContactActivity::class.java))
        }

//        binding.enableAlert.setOnClickListener{
//            enabledAlert()
//        }

        binding.sosMode.setOnClickListener{
            sosMode()
        }

        binding.Setting.setOnClickListener{
            startActivity(Intent(this,SettingsActivity::class.java))
        }

        binding.PairSmartBand.setOnClickListener{
            startActivity(Intent(this,WatchActivity::class.java))
        }



    }

    override fun onResume() {
        super.onResume()
        if(flag==1){
            if(min ==0.0 || max==0.0){
                Toast.makeText(this,"Set the heart rate range",Toast.LENGTH_LONG).show()
            }else{
                fitSignIn(GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) }
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
                Manifest.permission.BODY_SENSORS,Manifest.permission.ACTIVITY_RECOGNITION),
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

    private fun requestPermissions() {
        GoogleSignIn.requestPermissions(
            this, // your activity
            GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
            getGoogleAccount(),
            fitnessOptions)
    }

    private fun fitSignIn(requestCode: Int) {

        if (!GoogleSignIn.hasPermissions(getGoogleAccount(), fitnessOptions)) {
            requestPermissions()
        } else {
            Log.d(TAG,"FitSignIn Else")
            binding.watchStatus.text="Smartband is connected"
            accessGoogleFit()
        }
    }
    private fun getGoogleAccount() = GoogleSignIn.getAccountForExtension(this, fitnessOptions)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> when (requestCode) {
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE -> accessGoogleFit()
                else -> {
                    // Result wasn't from Google Fit
                }
            }
            else -> {
                // Permission not granted
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_LONG).show()
                requestPermissions()
            }
        }
    }

    private fun accessGoogleFit() {

        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.MINUTES.toMillis(30)

        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_HEART_RATE_BPM)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .bucketByTime(1, TimeUnit.DAYS)
            .build()
        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        Fitness.getHistoryClient(this, account)
            .readData(readRequest)
            .addOnSuccessListener { response ->
                // Use response data here
                Log.i(TAG, "OnSuccess()")
                for (dataSet in response.buckets.flatMap { it.dataSets }) {
                    dumpDataSet(dataSet)
                }
            }
            .addOnFailureListener({ e -> Log.d(TAG, "OnFailure()", e) })
    }
    fun dumpDataSet(dataSet: DataSet) {
        for (dp in dataSet.dataPoints) {
            Log.i(TAG,"Data point:")
            Log.i(TAG,"\tStart: ${dp.getStartTimeString()}")
            Log.i(TAG,"\tEnd: ${dp.getEndTimeString()}")
            for (field in dp.dataType.fields) {
                Log.i(TAG,"\tField: ${field.name.toString()} Value: ${dp.getValue(field)}")
                Log.d(TAG,dp.getValue(field).toString())
                if(dp.getValue(field).toString().toDouble() < min || dp.getValue(field).toString().toDouble() > max){
                    Log.d(TAG,"${dp.getValue(field).toString().toDouble()},${min},${max},${dp.getValue(field).toString().toDouble() < min}")
                    sosMode()
                     break
                }

            }
        }
    }

    fun DataPoint.getStartTimeString() = Instant.ofEpochSecond(this.getStartTime(TimeUnit.SECONDS))
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime().toString()

    fun DataPoint.getEndTimeString() = Instant.ofEpochSecond(this.getEndTime(TimeUnit.SECONDS))
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime().toString()
}