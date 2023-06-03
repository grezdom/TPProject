package com.example.httpsrequest.piv

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract
import com.yubico.yubikit.android.ui.YubiKeyPromptActivity
import com.yubico.yubikit.android.ui.YubiKeyPromptConnectionAction
import com.yubico.yubikit.core.application.CommandState
import com.yubico.yubikit.core.smartcard.SmartCardConnection
import com.yubico.yubikit.core.util.Pair
import com.yubico.yubikit.piv.KeyType
import com.yubico.yubikit.piv.PivSession
import com.yubico.yubikit.piv.Slot
import com.yubico.yubikit.piv.jca.PivProvider
import java.security.*
import java.security.spec.X509EncodedKeySpec

private const val  EXTRA_SLOT = "SLOT"
private const val SIGNED_MESSAGE = "SIGNED"
private const val UNSIGNED_MESSAGE = "UNSIGNED"

class pivSignMessageContract : ActivityResultContract<Pair<Slot, String>, ByteArray?>() {
    override fun createIntent(context: Context, input: Pair<Slot,String>): Intent =
        YubiKeyPromptActivity.createIntent(context, pivGetSignMessageAction::class.java).apply{
            putExtra(EXTRA_SLOT, input.first)
            putExtra(UNSIGNED_MESSAGE, input.second)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): ByteArray? {
        return when(resultCode){
            Activity.RESULT_OK -> intent!!.extras!!.getByteArray(SIGNED_MESSAGE)
            else -> null
        }
    }
}



@Suppress("DEPRECATION")//For the second extras.getSerializable with Version < TIRAMISU
class pivGetSignMessageAction : YubiKeyPromptConnectionAction<SmartCardConnection>(SmartCardConnection::class.java){
    override fun onYubiKeyConnection(
        connection: SmartCardConnection,
        extras: Bundle,
        commandState: CommandState?
    ): Pair<Int, Intent> {
        val slot = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            extras.getSerializable(EXTRA_SLOT, Slot::class.java)
        } else {
            extras.getSerializable(EXTRA_SLOT) as Slot
        }
        val piv = PivSession(connection)
        val publicKey = piv.getCertificate(slot!!).publicKey
        val pin = "123456".toCharArray()
        piv.verifyPin(pin)
        val message = extras.getString(UNSIGNED_MESSAGE)
        val decodedToken = Base64.decode(message,Base64.NO_WRAP)

        val provider = PivProvider(piv)
        val algorithm = when (KeyType.fromKey(publicKey).params.algorithm) {
            KeyType.Algorithm.RSA -> "SHA256withRSA"
            KeyType.Algorithm.EC -> "SHA256withECDSA"
        }

        val keyStore = KeyStore.getInstance("YKPiv", provider)
        keyStore.load(null)
        val privateKey = keyStore.getKey(slot.stringAlias, pin) as PrivateKey
        val signature = Signature.getInstance(algorithm, provider).apply {
            initSign(privateKey)
            update(decodedToken)
        }.sign()

        val result = Signature.getInstance(algorithm).apply {
            initVerify(publicKey)
            update(decodedToken)
        }.verify(signature)

        Log.d("SignatureMessage", message!!)
        Log.d("SignatureMessageBase64", Base64.encodeToString(signature,Base64.NO_WRAP))//Signature + FLag
        Log.d("SignaturePKBase64", Base64.encodeToString(publicKey.encoded,Base64.NO_WRAP))
        Log.d("SignatureInitBase64", Base64.encodeToString(decodedToken,Base64.NO_WRAP))
        Log.d("SignatureMessageResult", result.toString())


        return Pair(Activity.RESULT_OK, Intent().apply {
        putExtra(SIGNED_MESSAGE, signature) })
    }
}