package io.agora.agoravideodemo;

import android.app.Application;
import android.support.multidex.MultiDexApplication;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.agora.agoravideodemo.model.ShowToastEvent;
import io.agora.agoravideodemo.utils.CommonUtilsKt;
import timber.log.Timber;

/**
 * Created by saikiran on 02-06-2018.
 **/
public class AgoraApp extends MultiDexApplication {
    private Toast toast;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) Timber.plant(new Timber.DebugTree());
        CommonUtilsKt.regOnce(EventBus.getDefault(),this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showToastEvent(ShowToastEvent event){
        if (toast != null)toast.cancel();
        toast = Toast.makeText(this, event.getMessage(), Toast.LENGTH_SHORT);
        toast.show();
    }
}