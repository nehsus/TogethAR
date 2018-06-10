package io.agora.agoravideodemo.syncad;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.jetbrains.annotations.Nullable;

public class SyncService extends Service {

    private static final String TAG = "SyncService";
    private static final Object sSyncAdapterLock = new Object();
    private static SyncAdapter mSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.i(TAG, "Sync Service created.");
        synchronized (sSyncAdapterLock) {
            if (mSyncAdapter == null) {
                mSyncAdapter = new SyncAdapter(getApplicationContext(),
                        true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Sync Service binded.");
        return mSyncAdapter.getSyncAdapterBinder();
    }
}