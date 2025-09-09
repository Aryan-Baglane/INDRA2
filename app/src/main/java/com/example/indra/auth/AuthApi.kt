package com.example.indra.auth

import android.net.Uri
import com.example.indra.data.AuthUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import kotlinx.coroutines.tasks.await

object AuthApi {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    suspend fun currentUser(): AuthUser? {
        val u = auth.currentUser ?: return null
        return AuthUser(
            uid = u.uid,
            displayName = u.displayName,
            photoUrl = u.photoUrl?.toString(),
            email = u.email
        )
    }

    suspend fun signInWithEmail(email: String, password: String): Result<AuthUser> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await()
        currentUser()!!
    }

    suspend fun signUpWithEmail(email: String, password: String, displayName: String?): Result<AuthUser> = runCatching {
        auth.createUserWithEmailAndPassword(email, password).await()
        displayName?.let {
            val profileUpdates = userProfileChangeRequest {
                this.displayName = it
            }
            auth.currentUser?.updateProfile(profileUpdates)?.await()
        }
        currentUser()!!
    }

    suspend fun updateProfile(displayName: String?, photoUrl: String?): Result<Unit> = runCatching {
        val profileUpdates = userProfileChangeRequest {
            this.displayName = displayName
            photoUrl?.let { this.photoUri = Uri.parse(it) }
        }
        auth.currentUser?.updateProfile(profileUpdates)?.await()
        Unit
    }

    suspend fun signOut(): Result<Unit> = runCatching {
        auth.signOut()
        Unit
    }
}
