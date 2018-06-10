package io.agora.agoravideodemo.base;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import java.util.HashSet;

import io.agora.agoravideodemo.AgoraSignalingService;
import io.agora.agoravideodemo.RtcService;
import io.agora.agoravideodemo.model.SignalMessageAction;
import io.agora.rtc.RtcEngine;

/**
 * Created by saiki on 18-05-2018.
 **/
abstract public class BaseRtcActivity extends AppCompatActivity {
    private RtcService mService;
    public boolean mBound = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, AgoraSignalingService.class));
    }

    final public void makeCall(String userId, String name, String phoneWithCountryCode, String channelID) {
        Intent callIntent = new Intent(this, AgoraSignalingService.class);
        callIntent.setAction(SignalMessageAction.MAKE_CALL.name());
        callIntent.putExtra(AgoraSignalingService.ON_GOING_USER_ID_KEY, userId);
        callIntent.putExtra(AgoraSignalingService.RECEIVER_CALL_USER_NAME_KEY, name);
        callIntent.putExtra(AgoraSignalingService.RECEIVER_CALL_PHONE_KEY, phoneWithCountryCode);
        callIntent.putExtra(AgoraSignalingService.ADD_CALL_CHANNEL_ID, channelID);
        startService(callIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, RtcService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
        mBound = false;
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            RtcService.LocalBinder binder = (RtcService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            if (mService.getRtcEngine() == null)
                mService.initializeAgoraEngine();
            onRtcServiceConnected(mService.getRtcEngine());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        retrySignalLogin();
        // This registers mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, getIntentFilter());
    }

    private IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        for (RtcService.IntentAction action : RtcService.IntentAction.values())
            intentFilter.addAction(action.name());
        return intentFilter;
    }

    @Override
    protected void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String action = intent.getAction();
            if (action == null) return;
            int uid;
            switch (RtcService.IntentAction.valueOf(action)) {
                case FIRST_REMOTE_VIDEO_DECODED:
                    uid = intent.getIntExtra("uid", 0);
                    setupRemoteVideo(uid);
                    break;
                case USER_OFFLINE:
                    uid = intent.getIntExtra("uid", 0);
                    onRemoteUserLeft(uid);
                    break;
                case USER_MUTE_VIDEO:
                    uid = intent.getIntExtra("uid", 0);
                    Boolean muted = intent.getBooleanExtra("muted", false);
                    onRemoteUserVideoMuted(uid, muted);
                    break;
                case RTC_ERROR:
                    int error = intent.getIntExtra("err", 0);
                    onRtcError("Rtc Event error " + error + " Please try again.");
                    break;
                case ON_USER_JOINED:
                    uid = intent.getIntExtra("uid", 0);
                    onUserJoined(uid);
                    break;
                case ON_ACTIVE_SPEAKER:
                    uid = intent.getIntExtra("uid", 0);
                    onActiveSpeaker(uid);
                    break;
                case CALL_ENDED:
                    onCallEnded();
                    break;
            }
        }
    };

    public RtcEngine getRtcEngine() {
        return mService.getRtcEngine();
    }

    public String getLastKnownRoomID() {
        return mBound ? mService.getCurrentRoomID() : null;
    }

    abstract public void onRtcServiceConnected(RtcEngine rtcEngine);

    public void onCallEnded() {
        //Implement in client if required
    }

    public void onActiveSpeaker(int uid) {
        //Implement in client if required
    }

    public void onAudioVolumeIndication(int uid, int totalVolume) {
        //Implement in client if required
    }

    public void onRemoteUserVideoMuted(int uid, boolean muted) {
        //Implement in client if required
    }

    public void onRemoteUserLeft(int uid) {
        //Implement in client if required
    }

    public void setupRemoteVideo(int uid) {
        //Implement in client if required
    }

    public void onRtcError(String error) {
        //Implement in client if required
    }

    public void onUserJoined(int uid) {
        //Implement in client if required
    }

    final public void joinChannelRequested(String roomId) {
        if (mBound) mService.joinChannelRequested(roomId);
    }

    public HashSet<Integer> getOnCallList() {
        return mBound ? mService.getOnCallList() : new HashSet<Integer>();
    }

    final public void stopRtcService() {
        Intent endCallIntent = new Intent(this, RtcService.class);
        endCallIntent.setAction(RtcService.ACTION_END_CALL);
        startService(endCallIntent);
    }

    final public Boolean isCallOnGoing() {
        return getRtcEngine() != null && getRtcEngine().getCallId() != null;
    }

    public final Intent getSignalingIntent() {
        return new Intent(this, AgoraSignalingService.class);
    }

    public final void retrySignalLogin() {
        Intent intent = getSignalingIntent();
        intent.setAction("RETRY_LOGIN");
        startService(intent);
    }
}
