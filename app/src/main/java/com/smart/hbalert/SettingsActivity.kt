package com.smart.hbalert

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.smart.hbalert.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.title="Add Heart Rate Range"

        sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

        val min = sharedPreferences.getString("Min", "").toString()
        val max = sharedPreferences.getString("Max", "").toString()
        // Retrieve data
        binding.minField.setText(min)
        binding.maxField.setText(max)

        binding.submitSettings.setOnClickListener{onClickSubmit()}
    }

    private fun onClickSubmit(){
        val min = binding.minField.text.toString().trim()
        val max = binding.maxField.text.toString().trim()
        binding.minLayout.error = null
        binding.maxLayout.error = null
        if(min=="" || max=="" ){
            if(min==""){
                binding.minLayout.error = "FIELD CANNOT BE EMPTY"}
            if(max==""){
                binding.maxLayout.error="FIELD CANNOT BE EMPTY"}
        }
        else{
            binding.minField.clearFocus()
            binding.maxField.clearFocus()
            insertData(min,max)
        }
    }


    private fun insertData(min: String, max: String) {
        val editor = sharedPreferences.edit()
        editor.putString("Min", min)
        editor.putString("Max", max)
        Toast.makeText(this,"Heart Rate updated successfully", Toast.LENGTH_SHORT).show()
        editor.apply()
    }

}

