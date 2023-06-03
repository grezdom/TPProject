package com.example.httpsrequest.piv

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract
import com.example.httpsrequest.data.publicKeySlots
import com.example.httpsrequest.data.publicKeys
import com.example.httpsrequest.data.publicKeysEncoded
import com.yubico.yubikit.android.ui.YubiKeyPromptActivity
import com.yubico.yubikit.android.ui.YubiKeyPromptConnectionAction
import com.yubico.yubikit.core.application.CommandState
import com.yubico.yubikit.core.smartcard.SmartCardConnection
import com.yubico.yubikit.core.util.Pair
import com.yubico.yubikit.piv.PivSession
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec

private const val  EXTRA_SLOT = "SLOT"
private const val AUTH_PUBLIC_KEY = "AUTH_PUBLIC_KEY"

@Suppress("DEPRECATION")// If the version is bellow TIRAMISU
class pivRegPublicKeyContract : ActivityResultContract<publicKeySlots, publicKeys?>() {
    override fun createIntent(context: Context, input: publicKeySlots): Intent =
        YubiKeyPromptActivity.createIntent(context, pivGetRegPublicKeyAction::class.java).apply{
        putExtra(EXTRA_SLOT, input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): publicKeys? {
        return when(resultCode){
            Activity.RESULT_OK -> {
                val myPublicKeys = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent!!.getSerializableExtra(AUTH_PUBLIC_KEY, publicKeysEncoded::class.java)
                } else {
                    intent!!.getSerializableExtra(AUTH_PUBLIC_KEY) as publicKeysEncoded
                }
                val authPublicKey = myPublicKeys!!.authPKEncoded
                val signPublicKey = myPublicKeys.signPKEncoded
                val kf = KeyFactory.getInstance("RSA")
                val specAuth = X509EncodedKeySpec(authPublicKey)
                val specSign = X509EncodedKeySpec(signPublicKey)
                val publicKeys = publicKeys(kf.generatePublic(specAuth),kf.generatePublic(specSign))
                Log.d("SignaturePKBase64", Base64.encodeToString(publicKeys.authPublicKey.encoded, Base64.NO_WRAP))

                publicKeys
            }
            else -> null
        }
    }
}


@Suppress("DEPRECATION")//For the second extras.getSerializable with Version < TIRAMISU
class pivGetRegPublicKeyAction : YubiKeyPromptConnectionAction<SmartCardConnection>(SmartCardConnection::class.java){
    override fun onYubiKeyConnection(
        connection: SmartCardConnection,
        extras: Bundle,
        commandState: CommandState?
    ): Pair<Int, Intent> {
        val slots = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            extras.getSerializable(EXTRA_SLOT, publicKeySlots::class.java)
        } else {
            extras.getSerializable(EXTRA_SLOT) as publicKeySlots
        }
        val piv = PivSession(connection)
        val signPublicKey = piv.getCertificate(slots!!.signSlot).publicKey
        val authPublicKey = piv.getCertificate(slots.authSlot).publicKey
        Log.d("SignaturePKBase64", Base64.encodeToString(authPublicKey.encoded, Base64.NO_WRAP))

        val myPublicKeys = publicKeysEncoded(authPublicKey.encoded,signPublicKey.encoded)
        return Pair(Activity.RESULT_OK, Intent().apply {
            putExtra(AUTH_PUBLIC_KEY, myPublicKeys)
        })
    }
}