package com.smart.hbalert.contact

import androidx.lifecycle.LiveData

class ContactReporitory (private val contactDoa: DOA){

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allCont: LiveData<List<contact>> = contactDoa.getAllConts()


    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.

    fun insert(cont: contact) {
        contactDoa.insert(cont)
    }

    fun delete(cont: contact){
        contactDoa.delete(cont)
    }
}