package com.example.gradia.data.firebase

import android.app.Activity
import com.facebook.CallbackManager
import com.facebook.login.LoginManager

object FacebookSignInUtil {
    val callbackManager = CallbackManager.Factory.create()

    fun signOut() {
        LoginManager.getInstance().logOut()
    }

    fun loginWithAccountPicker(activity: Activity, permissions: List<String>) {
        LoginManager.getInstance().logOut()
        LoginManager.getInstance().logInWithReadPermissions(activity, permissions)
    }
}
