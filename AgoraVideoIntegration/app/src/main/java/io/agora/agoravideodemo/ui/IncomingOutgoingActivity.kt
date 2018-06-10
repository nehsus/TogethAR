package io.agora.agoravideodemo.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.TaskStackBuilder
import io.agora.agoravideodemo.R
import io.agora.agoravideodemo.base.BaseRtcActivity
import io.agora.agoravideodemo.model.*
import io.agora.agoravideodemo.utils.hide
import io.agora.agoravideodemo.utils.regOnce
import io.agora.agoravideodemo.utils.showToastEvent
import io.agora.agoravideodemo.utils.unregOnce
import io.agora.rtc.RtcEngine
import kotlinx.android.synthetic.main.content_incomin_outgoing.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class IncomingOutgoingActivity : BaseRtcActivity() {
    private val mEventBus = EventBus.getDefault()
    private var mScheduleFuture: ScheduledFuture<*>? = null
    private val mFinishActivityTask = Runnable {
        Timber.d("mFinishActivityTask is running !isDestroyed && !isFinishing = %s",
                !isDestroyed && !isFinishing)
        if (!isDestroyed && !isFinishing) {
            this@IncomingOutgoingActivity.finish()
        }
    }

    override fun onRtcServiceConnected(rtcEngine: RtcEngine?) {
        Timber.d("onRtcServiceConnected")
        mEventBus.getStickyEvent(IncomingCallEvent::class.java)?.let {
            if (isCallOnGoing) {
                sendActionToSignalingService(it.callerInfo)
                finish()
                return
            }
            showIncomingActions(it.callerInfo)
            mEventBus.removeStickyEvent(it)
        }
    }

    private fun sendActionToSignalingService(message: SignalMessage) {
        startService(signalingIntent.apply {
            action = SignalMessageAction.LINE_BUSY.name
            putExtra("callerUserId", message.senderInfo.userId)
        })
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incomin_outgoing)
        scheduleAutoCutOff()
        end_btn.setOnClickListener {
            this@IncomingOutgoingActivity.finish()
        }
    }

    private fun scheduleAutoCutOff() {
        val mHandler = Handler()
        val mExecutorService = Executors.newSingleThreadScheduledExecutor()
        if (!mExecutorService.isShutdown)
            mScheduleFuture = mExecutorService.schedule({ mHandler.post(mFinishActivityTask) },
                    FINISH_DELAY_TIME_IN_SECONDS,
                    TimeUnit.SECONDS)
    }

    private fun showOutgoingActions(receiverDetails: OutgoingCallEvent) {
        accept_btn.hide()
        status_tv.text = receiverDetails.run { "Calling $receiverName\n$receiverPhone" }
        end_btn.setOnClickListener {
            sendEndCallAction(receiverDetails)
            this@IncomingOutgoingActivity.finish()
        }
    }

    private fun sendEndCallAction(receiverDetails: OutgoingCallEvent) {
        val intent = signalingIntent
        intent.action = SignalMessageAction.END_CALL.name
        intent.putExtra("receiverUserId", receiverDetails.receiverUserId)
        startService(intent)
    }

    private fun sendCallRejectedAction(message: SignalMessage) {
        val intent = signalingIntent
        intent.action = SignalMessageAction.REJECT_CALL.name
        intent.putExtra("callerUserId", message.senderInfo.userId)
        startService(intent)
    }

    private fun showIncomingActions(message: SignalMessage) {
        Timber.d("showIncomingActions message %s", message.toString())
        status_tv.text = message.senderInfo.run { "$name is calling\n+$countryCode-$phone" }

        val resultIntent = Intent(this, VideoChatViewActivity::class.java)
        resultIntent.putExtra(VideoChatViewActivity.CHAT_ROOM_KEY, message.channelID)
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addNextIntent(Intent(this, MainActivity::class.java))
        stackBuilder.addNextIntentWithParentStack(resultIntent)

        accept_btn.setOnClickListener {
            stackBuilder.startActivities()
            sendCallAcceptedAction(message)
            this@IncomingOutgoingActivity.finish()
        }

        end_btn.setOnClickListener {
            sendCallRejectedAction(message)
            this@IncomingOutgoingActivity.finish()
        }
    }

    private fun sendCallAcceptedAction(message: SignalMessage) {
        val intent = signalingIntent
        intent.action = SignalMessageAction.ACCEPT_CALL.name
        intent.putExtra("channelID", message.channelID)
        intent.putExtra("callerUserId", message.senderInfo.userId)
        startService(intent)
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onIncomingCallEvent(event: IncomingCallEvent) {
        //Make sure to Remove this stick event
        //Pass here and handle it in onRtcServiceConnected
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onOutgoingActions(event: OutgoingCallEvent) {
        showOutgoingActions(event)
        mEventBus.removeStickyEvent(event)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEndCallEvent(event: EndCallEvent) {
        finish()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRejectCallEvent(event: RejectCallEvent) {
        finish()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLineBusyEvent(event: LineBusyEvent) {
        showToastEvent("Line is busy, " +
                "Please try after some time")
        finish()
    }

    override fun onResume() {
        super.onResume()
        mEventBus.regOnce(this)
    }

    override fun onPause() {
        super.onPause()
        mEventBus.getStickyEvent(IncomingCallEvent::class.java)?.let {
            mEventBus.removeStickyEvent(it)
        }
        mEventBus.unregOnce(this)
    }

    companion object {
        const val FINISH_DELAY_TIME_IN_SECONDS = 55L
    }

    override fun onDestroy() {
        mScheduleFuture?.cancel(true)
        super.onDestroy()
    }

}
