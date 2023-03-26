package com.smart.hbalert

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.smart.hbalert.contact.ContactAdapter
import com.smart.hbalert.contact.ContactViewModel
import com.smart.hbalert.contact.contact
import com.smart.hbalert.databinding.ActivityContactBinding

class ContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactBinding
    private lateinit var viewModel :ContactViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.title="Add Contacts"


        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter =ContactAdapter(this,this)

        binding.recyclerView.adapter=adapter

        viewModel= ViewModelProvider(this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application))[ContactViewModel::class.java]

        viewModel.allCont.observe(this, Observer {list->
            list?.let{
                adapter.updateList(it)
            }
        })

        binding.submitButton.setOnClickListener{onClickSubmit()}
    }

    private fun onClickSubmit(){
        val name = binding.nameField.text.toString().trim()
        val number = binding.numberField.text.toString().trim()
        binding.nameLayout.error = null
        binding.numberLayout.error = null
        if(name=="" || number=="" ){
            if(name==""){
                binding.nameLayout.error = "FIELD CANNOT BE EMPTY"}
            if(number==""){
                binding.numberLayout.error="FIELD CANNOT BE EMPTY"}
        }
        else if(number.length!=10){
            binding.numberLayout.error = "Wrong Input"
        }
        else{
            insertData(name,number)
        }
    }

    fun onItemClicked(cont: contact) {
        viewModel.deleteCont(cont)
    }


    private fun insertData(name: String, number: String) {
        viewModel.insertCont(contact(name,number))
    }

}