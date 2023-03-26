package com.smart.hbalert.contact

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContactViewModel(application: Application) : AndroidViewModel(application) {
    val allCont: LiveData<List<contact>>
    private val repository : ContactReporitory

    init {
        val dao = ContactDatabase.getDatabase(application).contactDao()
        repository= ContactReporitory(dao)
        allCont=repository.allCont
    }

    fun deleteCont(cont:contact) = viewModelScope.launch(Dispatchers.IO){
        repository.delete(cont)
    }

    fun insertCont(cont: contact) = viewModelScope.launch(Dispatchers.IO){
        repository.insert(cont)
    }
}