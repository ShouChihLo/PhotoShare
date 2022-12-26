package com.example.photosharedemo

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage


class MyViewModel: ViewModel() {

    // get the reference to the firestore
    private val db = Firebase.firestore
    val collectionRef = db.collection("messages")

    // define two types of login state
    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED
    }

    // declare a LoginState LiveData and remap the value to the type case
    val authenticationState = LoginStateLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        }
        else {
            AuthenticationState.UNAUTHENTICATED
        }
    }

    fun uploadMessage(photoUrl: String?, description: String?) {
        // get the user's info
        val user = FirebaseAuth.getInstance().currentUser
        // get the user's email
        val email = user!!.email!!
        val index = email.indexOf('@')
        val username = email.substring(0, index)
        // create a new posted message
        val message = PostedMessage(description, photoUrl, username, null)

        // write the message to the firestore
        collectionRef.add(message)  // add the message and listen the event
            .addOnSuccessListener {  docRef ->
                // upload the photo image to the firebase storage
                // build a storage reference path: firebase storage/user's id/document's id/ file_name
                val uri = Uri.parse(message.photoUrl)
                val key = docRef.id
                val storageRef = Firebase.storage.reference
                    .child(user!!.uid)
                    .child(key)
                    .child(uri.lastPathSegment!!)
                putImageInStorage(storageRef, uri, docRef)
            }
            .addOnFailureListener { e ->
                Log.d("ViewModel", "Error adding the message")
            }
    }

    private fun putImageInStorage(storageReference: StorageReference, uri: Uri, docRef: DocumentReference) {
        // write the image file to the storage
        storageReference.putFile(uri)
            .addOnSuccessListener {  taskSnapshot ->
                // get the public download Url for the image in the storage
                taskSnapshot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { url ->
                        // update the photoUrl in the firestore
                        val photoUrl = url.toString()
                        docRef.update("photoUrl", photoUrl)
                    }

            }
            .addOnFailureListener { e ->
                Log.d("ViewModel", "Error on writing the image to the storage")
            }
    }


}