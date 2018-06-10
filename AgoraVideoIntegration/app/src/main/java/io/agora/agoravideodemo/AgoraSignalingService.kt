package io.agora.agoravideodemo

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.support.v4.app.TaskStackBuilder
import android.util.Log
import com.google.gson.GsonBuilder
import io.agora.AgoraAPIOnlySignal
import io.agora.IAgoraAPI
import io.agora.agoravideodemo.model.*
import io.agora.agoravideodemo.ui.IncomingOutgoingActivity
import io.agora.agoravideodemo.ui.MainActivity
import io.agora.agoravideodemo.ui.VideoChatViewActivity
import io.agora.agoravideodemo.utils.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.util.*

/**
 * Created by saiki on 02-06-2018.
 **/
class AgoraSignalingService : Service() {

    private val mSignal: AgoraAPIOnlySignal by lazy { initSignal() }
    private val mGson = GsonBuilder().setPrettyPrinting().create()
    private val mNotificationHelper: NotificationHelper by lazy { NotificationHelper(this) }
    private val mEventBus = EventBus.getDefault()
    private val agoraAppId: String by lazy { getString(R.string.agora_app_id) }

    override fun onCreate() {
        super.onCreate()
        mSignal.callbackSet(mCustomICallBack)
        loginToAgoraSignaling()
        startSignalingForeground()
        mEventBus.regOnce(this)
    }

    private fun loginToAgoraSignaling() {
        mSignal.login2(agoraAppId, io.agora.agoravideodemo.model.UserInfo.userId, "_no_need_token", 0, "", 5, 2)
    }

    private fun startSignalingForeground() {
        val notification = mNotificationHelper.getSignalingNotification("AgoraCall is running", "Tap for more.").build();
        mNotificationHelper.notify(SIGNALING_NOTIFICATION_ID, notification)
        startForeground(SIGNALING_NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        mEventBus.unregOnce(this)
        mSignal.logout()
        stopForeground(true)
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && intent.action != null) {
            if (SignalMessageAction.contains(intent.action)) {
                when (SignalMessageAction.valueOf(intent.action)) {
                    SignalMessageAction.MAKE_CALL -> {
                        makeCall(intent.extras.getString(ON_GOING_USER_ID_KEY),
                                intent.extras.getString(RECEIVER_CALL_USER_NAME_KEY),
                                intent.extras.getString(RECEIVER_CALL_PHONE_KEY),
                                intent.extras.getString(ADD_CALL_CHANNEL_ID))
                    }
                    SignalMessageAction.END_CALL -> {
                        endCall(intent.extras.getString("receiverUserId"))
                    }
                    SignalMessageAction.REJECT_CALL -> {
                        rejectCall(intent.extras.getString("callerUserId"))
                    }
                    SignalMessageAction.ACCEPT_CALL -> {
                        callAccepted(intent.extras.getString("channelID"), intent.extras.getString("callerUserId"))
                    }
                    SignalMessageAction.LINE_BUSY -> {
                        busyLine(intent.extras.getString("callerUserId"))
                    }

                }
            } else if (intent.action == "RETRY_LOGIN") {
                loginToAgoraSignaling()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun busyLine(callerUserId: String) {
        val message = SignalMessage(action = SignalMessageAction.LINE_BUSY, senderInfo = SignalSenderInfo(), message = "")
        mSignal.messageInstantSend(callerUserId, 0, mGson.toBase64Encode(message), SignalMessageAction.LINE_BUSY.name)
    }

    private fun makeCall(receiverUserId: String, receiverName: String, receiverPhone: String, addCallChannelID: String?) {
        val channelID = addCallChannelID ?: UUID.randomUUID().toString()
        val message = SignalMessage(
                action = SignalMessageAction.MAKE_CALL,
                senderInfo = SignalSenderInfo(),
                message = "",
                channelID = channelID)
        mSignal.messageInstantSend(receiverUserId, 0, mGson.toBase64Encode(message), SignalMessageAction.MAKE_CALL.name)
        mEventBus.postSticky(OutgoingCallEvent(channelID, receiverUserId, receiverName, receiverPhone))
    }

    private fun callAccepted(channelID: String, callerUserId: String) {
        val message = SignalMessage(action = SignalMessageAction.ACCEPT_CALL, senderInfo = SignalSenderInfo(), message = "", channelID = channelID)
        mSignal.messageInstantSend(callerUserId, 0, mGson.toBase64Encode(message), SignalMessageAction.ACCEPT_CALL.name)
    }

    private fun rejectCall(callerUserId: String) {
        val message = SignalMessage(action = SignalMessageAction.REJECT_CALL, senderInfo = SignalSenderInfo(), message = "")
        mSignal.messageInstantSend(callerUserId, 0, mGson.toBase64Encode(message), SignalMessageAction.REJECT_CALL.name)
    }

    private fun endCall(receiverUserId: String) {
        val message = SignalMessage(action = SignalMessageAction.END_CALL, senderInfo = SignalSenderInfo(), message = "")
        mSignal.messageInstantSend(receiverUserId, 0, mGson.toBase64Encode(message), SignalMessageAction.END_CALL.name)
    }

    private fun initSignal(): AgoraAPIOnlySignal {
        val appID = getString(R.string.agora_app_id)
        try {
            return AgoraAPIOnlySignal.getInstance(this, appID)
        } catch (e: Exception) {
            Timber.e(Log.getStackTraceString(e))
            throw RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e))
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        const val ON_GOING_USER_ID_KEY = "ON_GOING_USER_ID_KEY"
        const val RECEIVER_CALL_USER_NAME_KEY = "CALL_USER_NAME_KEY"
        const val RECEIVER_CALL_PHONE_KEY = "CALL_PHONE_KEY"
        const val ADD_CALL_CHANNEL_ID = "ADD_CALL_CHANNEL_ID"
        private const val SIGNALING_NOTIFICATION_ID = 550

    }

    private val mCustomICallBack: IAgoraAPI.ICallBack = object : BaseICallBack {

        override fun onLoginSuccess(uid: Int, fd: Int) {
            Timber.d("onLoginSuccess uid = %s and fd = %s", uid, fd)
        }

        override fun onLoginFailed(ecode: Int) {
            Timber.d("onLoginFailed ecode = %s", ecode)
            mEventBus.post(LoginFailedEvent(error = ecode))
        }

        override fun onLogout(ecode: Int) {
            Timber.d("onLogout ecode = %s", ecode)
        }

        override fun onMessageInstantReceive(account: String?, uid: Int, msg: String?) {
            //NOTE We cannot do any UI here.
            super.onMessageInstantReceive(account, uid, msg)
            Timber.d("onMessageInstantReceive account = %s , uid = %s , msg = %s", account, uid,
                    mGson.fromJson(msg?.decodeFromBase64(), SignalMessage::class.java))
            val data = mGson.fromJson(msg!!.decodeFromBase64(), SignalMessage::class.java)
            when (data.action) {
                SignalMessageAction.MAKE_CALL -> {
                    mEventBus.postSticky(IncomingCallEvent(data))
                }
                SignalMessageAction.END_CALL -> {
                    mEventBus.post(EndCallEvent())
                }
                SignalMessageAction.REJECT_CALL -> {
                    mEventBus.post(RejectCallEvent())
                }
                SignalMessageAction.ACCEPT_CALL -> {
                    val resultIntent = Intent(this@AgoraSignalingService, VideoChatViewActivity::class.java)
                    resultIntent.putExtra(VideoChatViewActivity.CHAT_ROOM_KEY, data.channelID)
                    val stackBuilder = TaskStackBuilder.create(this@AgoraSignalingService)
                    stackBuilder.addNextIntent(Intent(this@AgoraSignalingService, MainActivity::class.java))
                    stackBuilder.addNextIntentWithParentStack(resultIntent)
                    stackBuilder.startActivities()
                }
                SignalMessageAction.LINE_BUSY -> {
                    mEventBus.post(LineBusyEvent())
                }
            }
        }

        override fun onMessageSendSuccess(messageID: String?) {
            Timber.d("onMessageSendSuccess messageID = %s", messageID)
            messageID?.let {
                when (SignalMessageAction.valueOf(it)) {
                    SignalMessageAction.MAKE_CALL -> {
                        val intent = Intent(this@AgoraSignalingService, IncomingOutgoingActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                    else -> {
                    }
                }
            }
        }

        override fun onMessageSendError(messageID: String?, ecode: Int) {
            Timber.d("onMessageSendError messageID = %s and ecode = %s", messageID, ecode)
            messageID?.let {
                when (SignalMessageAction.valueOf(it)) {
                    SignalMessageAction.MAKE_CALL -> {
                        showSnackEvent("Call not reachable")
                    }
                    else -> {
                        showSnackEvent("Call failed with error $ecode")
                    }
                }
            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onIncomingCallEvent(event: IncomingCallEvent) {
        val intent = Intent(this@AgoraSignalingService, IncomingOutgoingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}