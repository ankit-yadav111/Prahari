package com.smart.hbalert.doa

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.smart.hbalert.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserDoa {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    fun addUser(user: User?) {
        user?.let {
            GlobalScope.launch(Dispatchers.IO) {
                usersCollection.document(user.mobile).set(it)
                    .addOnSuccessListener {
                        Log.d("Ankit","Done")
                    }
                    .addOnFailureListener {
                        Log.d("Ankit","Failed")
                    }
            }
        }
    }

    fun getUserById(mobile: String): Task<DocumentSnapshot> {
        return usersCollection.document(mobile).get()
    }
}