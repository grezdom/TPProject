package com.example.httpsrequest.data

import com.yubico.yubikit.piv.Slot
import java.io.Serializable

data class publicKeySlots(
    val authSlot : Slot,
    val signSlot: Slot
) : Serializable