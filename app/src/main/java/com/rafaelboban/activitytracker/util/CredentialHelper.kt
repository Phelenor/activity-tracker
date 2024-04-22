package com.rafaelboban.activitytracker.util

import android.content.Context
import android.util.Base64
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.rafaelboban.activitytracker.R
import timber.log.Timber
import java.security.MessageDigest
import java.util.UUID

object CredentialHelper {

    suspend fun startGoogleLogin(context: Context, credentialManager: CredentialManager): Pair<String, String>? {
        val nonce = generateNonce()
        val clientId = context.getString(R.string.google_oauth_credential_client_id)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(clientId)
            .setNonce(nonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val result = credentialManager.getCredential(context, request)
            val tokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            val googleIdToken = tokenCredential.idToken
            return googleIdToken to nonce
        } catch (e: Exception) {
            Timber.e(e)
        }

        return null
    }

    private fun generateNonce(): String {
        val bytes = UUID.randomUUID().toString().toByteArray()
        val hash = MessageDigest.getInstance("SHA-256").digest(bytes)
        val string = Base64.encodeToString(hash, Base64.DEFAULT)
        return string
    }
}
