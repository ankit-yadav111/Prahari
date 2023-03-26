package com.smart.hbalert.contact

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface DOA {

    @Query("SELECT * FROM Contact_table")
    fun getAllConts(): LiveData<List<contact>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(cont: contact)

    @Delete
    fun delete(cont:contact)

}