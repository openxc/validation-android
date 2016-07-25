package com.openxc.openxcstarter;

import android.os.Looper;

import com.openxcplatform.openxcstarter.BuildConfig;

/**
 * Created by roberhol on 6/24/2016.
 */
public class ThreadPreconditions {
    public static void checkOnMainThread() {
        if (BuildConfig.DEBUG) {
            if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
                throw new IllegalStateException("This method should be called from the Main Thread");
            }
        }
    }
}