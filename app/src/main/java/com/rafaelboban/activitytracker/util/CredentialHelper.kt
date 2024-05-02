package com.rafaelboban.activitytracker.util

import android.content.Context
import android.util.Base64
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.rafaelboban.activitytracker.R
import java.security.MessageDigest
import java.util.UUID

object CredentialHelper {

    suspend fun startGoogleLogin(
        context: Context,
        credentialManager: CredentialManager,
        onSuccess: (token: String, nonce: String) -> Unit,
        onError: (error: Exception) -> Unit
    ) {
        val nonce = generateNonce()
        val clientId = context.getString(R.string.google_oauth_credential_client_id)

        try {
            val token = buildCredentialRequest(
                context,
                credentialManager,
                clientId,
                nonce,
                filterByAuthorizedAccounts = false // TODO: change this to true
            )

            onSuccess(token, nonce)
        } catch (e: NoCredentialException) {
            try {
                val token = buildCredentialRequest(
                    context,
                    credentialManager,
                    clientId,
                    nonce,
                    filterByAuthorizedAccounts = false
                )

                onSuccess(token, nonce)
            } catch (e: Exception) {
                onError(e)
            }
        } catch (e: Exception) {
            onError(e)
        }
    }

    @Throws(IllegalArgumentException::class)
    private suspend fun buildCredentialRequest(
        context: Context,
        credentialManager: CredentialManager,
        clientId: String,
        nonce: String,
        filterByAuthorizedAccounts: Boolean
    ): String {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
            .setServerClientId(clientId)
            .setNonce(nonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(context, request)
        val tokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)

        return tokenCredential.idToken
    }

    suspend fun logout(credentialManager: CredentialManager) {
        runCatching {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        }
    }

    private fun generateNonce(): String {
        val bytes = UUID.randomUUID().toString().toByteArray()
        val hash = MessageDigest.getInstance("SHA-256").digest(bytes)
        val string = Base64.encodeToString(hash, Base64.DEFAULT)
        return string
    }
}
