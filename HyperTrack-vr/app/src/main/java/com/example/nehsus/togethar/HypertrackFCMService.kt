package com.example.nehsus.togethar

/**
 * Created by Nehsus on 09/06/18.
 */
import com.hypertrack.lib.HyperTrackFirebaseMessagingService
import com.hypertrack.lib.internal.transmitter.utils.Constants.HT_SDK_NOTIFICATION_KEY
import com.google.firebase.messaging.RemoteMessage

class HypertrackFCMService: HyperTrackFirebaseMessagingService(){
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.data != null) {
            val sdkNotification = remoteMessage.data[HT_SDK_NOTIFICATION_KEY]
            if (sdkNotification != null && sdkNotification.equals("true", ignoreCase = true)) {
                /**
                 * HyperTrack notifications are received here
                 * Dont handle these notifications. This might end up in a crash
                 */
                return
            }
        }
        // Handle your notifications here.
    }
}