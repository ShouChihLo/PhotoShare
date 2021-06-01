package com.example.photoshare

import android.net.Uri
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.time.LocalDate

class MyViewModel : ViewModel() {

    companion object {
        private const val TAG = "MyViewModel"
        private const val MESSAGES_CHILD = "messages"
    }

    // Initialize Realtime Database
    private val db = Firebase.database
    val messagesRef = db.reference.child(MESSAGES_CHILD)

    val newMessage = PostedMessage()

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED
    }

    val authenticationState = LoginStateLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }

    fun uploadMessage(activity: FragmentActivity) {
        val user = FirebaseAuth.getInstance().currentUser

        with(newMessage) {
            //text has been set
            //photoUrl has been set via the image picker
            uploader = user!!.displayName
            timestamp = LocalDate.now().toString()
        }

        //upload data to the firebase
        messagesRef.push()
            .setValue(
                newMessage,
                DatabaseReference.CompletionListener { databaseError, databaseReference ->
                    if (databaseError != null) {
                        Log.d(
                            TAG, "Unable to write message to database.",
                            databaseError.toException()
                        )
                        return@CompletionListener
                    }

                    // Build a StorageReference and then upload the image file
                    val uri = Uri.parse(newMessage.photoUrl)
                    val key = databaseReference.key
                    val storageReference = Firebase.storage
                        .getReference(user!!.uid)
                        .child(key!!)
                        .child(uri.lastPathSegment!!)  //get the file name
                    putImageInStorage(activity, storageReference, uri, key)
                })
    }

    private fun putImageInStorage(
        activity: FragmentActivity,
        storageReference: StorageReference,
        uri: Uri,
        key: String?
    ) {
        // First upload the image to Cloud Storage
        //Asynchronously uploads from a content URI
        storageReference.putFile(uri)
            .addOnSuccessListener(
                activity
            ) { taskSnapshot -> // After the image loads, get a public downloadUrl for the image
                // and add it to the message.
                taskSnapshot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { uri ->
                        newMessage.photoUrl = uri.toString()
                        // write the message to the database again
                        messagesRef
                            .child(key!!)
                            .setValue(newMessage)

                        //prepare for the next new posted message
                        newMessage.reset()
                    }
            }
            .addOnFailureListener(activity) { e ->
                Log.d(TAG, "Image upload task was unsuccessful.", e)
            }
    }

}
