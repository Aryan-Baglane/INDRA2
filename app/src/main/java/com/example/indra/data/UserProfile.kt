package com.example.indra.data

data class UserProfile(
    val uid: String = "",
    val displayName: String? = null,
    val photoUrl: String? = null
) {
    // Default constructor for Firebase
    constructor() : this(
        uid = "",
        displayName = null,
        photoUrl = null
    )
}