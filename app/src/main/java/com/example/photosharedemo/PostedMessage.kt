package com.example.photosharedemo

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp


// define the message structure in the firestore database

data class PostedMessage(
    var text: String? = null,  // image description
    var photoUrl: String? = null,  // image access path
    var uploader: String? = null,  // uploader name
    @ServerTimestamp
    var timestamp: Timestamp? = null  // upload time
)
