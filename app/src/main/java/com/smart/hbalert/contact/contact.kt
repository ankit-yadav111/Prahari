package com.smart.hbalert.contact

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull


@Entity(tableName = "Contact_table")
class contact (@ColumnInfo(name="Name")@NotNull val name:String,
               @ColumnInfo(name="Number")@PrimaryKey val number:String)