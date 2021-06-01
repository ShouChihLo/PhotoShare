package com.example.photoshare

class PostedMessage {
    var text: String? = null
    var photoUrl: String? = null
    var uploader: String? = null
    var timestamp: String? = null

    // Empty constructor needed for Firestore serialization
    constructor()

    constructor(text: String?, photoUrl: String?, uploader: String?, timestamp: String?) {
        this.text = text
        this.photoUrl = photoUrl
        this.uploader = uploader
        this.timestamp = timestamp
    }

    fun reset() {
        this.text = null
        this.photoUrl = null
        this.uploader = null
        this.timestamp = null
    }

}