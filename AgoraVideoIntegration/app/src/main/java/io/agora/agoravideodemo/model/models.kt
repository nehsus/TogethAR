package io.agora.agoravideodemo.model

import com.chibatching.kotpref.KotprefModel




/**
 * Created by saiki on 01-06-2018.
 **/
data class FireUser(val userId: String = "", val name: String = "",
                    val phone: String = "0", val countryCode: Int = 0,
                    val lastUpdated: Long = 0L, val verified: Boolean = false) {
    fun getPhoneWithCountryCode() = "+$countryCode-$phone"
}

object UserInfo : KotprefModel() {
    var userId by stringPref()
    var name by stringPref()
    var phone by stringPref()
    var countryCode by intPref()
}

//Signal Stuffs
enum class SignalMessageAction { MAKE_CALL, END_CALL, ACCEPT_CALL, REJECT_CALL, LINE_BUSY;
    companion object {
        operator fun contains(value: String): Boolean {
            for (c in values()) if (c.name == value) return true
            return false
        }
    }
}


data class SignalSenderInfo(
        val userId: String = UserInfo.userId,
        val name: String = UserInfo.name,
        val phone: String = UserInfo.phone,
        val countryCode: Int = UserInfo.countryCode
)

data class SignalMessage(val action: SignalMessageAction, val senderInfo: SignalSenderInfo,
                         val message: String, val channelID: String? = null)