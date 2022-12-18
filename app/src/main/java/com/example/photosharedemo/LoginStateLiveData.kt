package com.example.photosharedemo

import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginStateLiveData: LiveData<FirebaseUser?>() {
    // create a FirebaseAuth instance for calling the Firebase Authentication APIs
    private val firebaseAuth = FirebaseAuth.getInstance()

    // create an AuthStateListener function to get updates on the current Firebase user
    // logged into the app
    private val authStateListener = FirebaseAuth.AuthStateListener {
        // if no logged in, currentUser is equal to null
        value = firebaseAuth.currentUser
    }

    // When this LiveData object has an active observer, start observing the FirebaseAuth
    // state
    override fun onActive() {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    // When this LiveData object no longer has an active observer, stop observing
    override fun onInactive() {
        firebaseAuth.removeAuthStateListener(authStateListener)
    }

}