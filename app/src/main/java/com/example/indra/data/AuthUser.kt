package com.example.indra.data

data class AuthUser(
    val uid: String,
    val displayName: String?,
    val photoUrl: String?,
    val email: String?
)