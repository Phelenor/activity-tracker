package com.rafaelboban.activitytracker.util

import android.content.Context
import android.util.Base64
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.rafaelboban.activitytracker.R
import java.security.MessageDigest
import java.util.UUID
import kotlin.jvm.Throws

object CredentialHelper {

    suspend fun startGoogleLogin(
        context: Context,
        credentialManager: CredentialManager,
        onSuccess: (token: String, nonce: String) -> Unit,
        onError: (error: GetCredentialException) -> Unit
    ) {
        val nonce = generateNonce()
        val clientId = context.getString(R.string.google_oauth_credential_client_id)

        val request = try {
            buildCredentialRequest(clientId, nonce, filterByAuthorizedAccounts = true)
        } catch (e: IllegalArgumentException) {
            buildCredentialRequest(clientId, nonce, filterByAuthorizedAccounts = false)
        }

        try {
            val result = credentialManager.getCredential(context, request)
            val tokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            val googleIdToken = tokenCredential.idToken
            onSuccess(googleIdToken, nonce)
        } catch (e: GetCredentialException) {
            onError(e)
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun buildCredentialRequest(
        clientId: String,
        nonce: String,
        filterByAuthorizedAccounts: Boolean
    ): GetCredentialRequest {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
            .setServerClientId(clientId)
            .setNonce(nonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return request
    }

    private fun generateNonce(): String {
        val bytes = UUID.randomUUID().toString().toByteArray()
        val hash = MessageDigest.getInstance("SHA-256").digest(bytes)
        val string = Base64.encodeToString(hash, Base64.DEFAULT)
        return string
    }
}
