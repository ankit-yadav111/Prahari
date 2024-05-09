package com.smart.hbalert

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.smart.hbalert.databinding.ActivityConnectWatchBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class WatchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConnectWatchBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1
    private val TAG = "Ankit"
    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
        .build()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConnectWatchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.title="Connect To Smart Watch"

        sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

        val min = sharedPreferences.getString("Min", "").toString()
        val max = sharedPreferences.getString("Max", "").toString()
        fitSignIn(GOOGLE_FIT_PERMISSIONS_REQUEST_CODE)
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
                binding.heartRate.text = "Time: ${dp.getStartTimeString()} | Value: ${dp.getValue(field)}"
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

