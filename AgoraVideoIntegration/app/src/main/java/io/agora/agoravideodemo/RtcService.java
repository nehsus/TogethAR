package io.agora.agoravideodemo;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.HashSet;

import io.agora.agoravideodemo.utils.NotificationHelper;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;

public class RtcService extends Service {
    private static final String TAG = "RtcService";
    private NotificationHelper mNotificationHelper;
    public static final String ACTION_END_CALL = "end_call";
    private static final int NOTIFICATION_ID = 340;
    private HashSet<Integer> mCallUser = new HashSet<>();
    private String currentRoomID;


    public enum IntentAction {
        FIRST_REMOTE_VIDEO_DECODED, USER_OFFLINE, USER_MUTE_VIDEO,
        ON_USER_JOINED, ON_AUDIO_VOLUME_INDICATION, ON_ACTIVE_SPEAKER, RTC_ERROR, CALL_ENDED
    }

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    private RtcEngine mRtcEngine;


    public RtcService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initializeAgoraEngine();
        mNotificationHelper = new NotificationHelper(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null && intent.getAction().equals(ACTION_END_CALL)) {
            notifyEndCallToClients();
            releaseResource();
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void notifyEndCallToClients() {
        Intent intent = new Intent(IntentAction.CALL_ENDED.name());
        sendLocalBroadcast(intent);
    }

    public void joinChannelRequested(String roomId) {
        currentRoomID = roomId;
        startService(new Intent(this, RtcService.class));
        Notification notification = mNotificationHelper.getNotification1("Agora call on going", "Tap to return").build();
        mNotificationHelper.notify(NOTIFICATION_ID, notification);
        startForeground(NOTIFICATION_ID, notification);
    }

    public void initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
            mRtcEngine.enableAudioVolumeIndication(200, 3);
        } catch (Exception e) {
            Log.e(TAG, "Need to check rtc sdk init fatal error", e);
        }
    }

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {

        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
            mCallUser.add(uid);
            Intent intent = new Intent(IntentAction.FIRST_REMOTE_VIDEO_DECODED.name());
            intent.putExtra("uid", uid);
            sendLocalBroadcast(intent);
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            Intent intent = new Intent(IntentAction.USER_OFFLINE.name());
            intent.putExtra("uid", uid);
            sendLocalBroadcast(intent);
            mCallUser.remove(uid);
        }

        @Override
        public void onUserMuteVideo(final int uid, final boolean muted) {
            Intent intent = new Intent(IntentAction.USER_MUTE_VIDEO.name());
            intent.putExtra("uid", uid);
            intent.putExtra("muted", muted);
            sendLocalBroadcast(intent);
        }

        @Override
        public void onError(int err) {
            super.onError(err);
            Intent intent = new Intent(IntentAction.RTC_ERROR.name());
            intent.putExtra("err", err);
            sendLocalBroadcast(intent);
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            super.onUserJoined(uid, elapsed);
            Intent intent = new Intent(IntentAction.ON_USER_JOINED.name());
            intent.putExtra("uid", uid);
            sendLocalBroadcast(intent);
        }

        @Override
        public void onActiveSpeaker(int uid) {
            super.onActiveSpeaker(uid);
            Intent intent = new Intent(IntentAction.ON_ACTIVE_SPEAKER.name());
            intent.putExtra("uid", uid);
            sendLocalBroadcast(intent);
        }
    };


    private void sendLocalBroadcast(Intent intent) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public RtcEngine getRtcEngine() {
        return mRtcEngine;
    }

    public String getCurrentRoomID() {
        return currentRoomID;
    }

    @Override
    public void onDestroy() {
        releaseResource();
        super.onDestroy();
    }

    private void releaseResource() {
        mCallUser.clear();
        RtcEngine.destroy();
        mRtcEngine = null;
        currentRoomID = null;
        stopForeground(true);
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public RtcService getService() {
            // Return this instance of LocalService so clients can call public methods
            return RtcService.this;
        }
    }

    public HashSet<Integer> getOnCallList() {
        return mCallUser;
    }

}
