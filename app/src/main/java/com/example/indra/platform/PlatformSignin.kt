package com.example.indra.platform

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

@SuppressLint("StaticFieldLeak")
object PlatformSignIn {

    private var activity: Activity? = null
    private var client: GoogleSignInClient? = null
    private var callback: ((Boolean, String?) -> Unit)? = null
    private const val RC_GOOGLE = 9001

    fun init(activity: Activity) {
        this.activity = activity
        client = GoogleSignIn.getClient(
            activity,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("344578211079-4e30t0leftro0dmo4vfvkoihk3tdg3eq.apps.googleusercontent.com")
                .build()
        )
    }

    fun handleResult(data: Intent?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { result ->
                    if (result.isSuccessful) {
                        callback?.invoke(true, null)
                    } else {
                        callback?.invoke(false, result.exception?.message)
                    }
                }
        } catch (e: Exception) {
            callback?.invoke(false, e.message)
        }
    }

    fun signIn(launcher: ActivityResultLauncher<Intent>) {
        val intent = client?.signInIntent ?: return
        launcher.launch(intent)
    }

    fun setCallback(cb: (Boolean, String?) -> Unit) {
        callback = cb
    }
}
