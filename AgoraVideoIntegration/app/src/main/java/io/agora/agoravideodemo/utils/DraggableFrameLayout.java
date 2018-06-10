package io.agora.agoravideodemo.utils;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by saiki on 24-05-2018.
 **/
public class DraggableFrameLayout extends FrameLayout {

    public DraggableFrameLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public DraggableFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DraggableFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DraggableFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        post(new Runnable() {
            @Override
            public void run() {
                setOnTouchListener(new OnDragTouchListener(DraggableFrameLayout.this));
                setSoundEffectsEnabled(false);
            }
        });
    }
}
