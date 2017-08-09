package com.naren.quire.util.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import com.sendbird.android.SendBird;

public class QuireService extends Service {

    public final String LOG_TAG = QuireService.this.getClass().getSimpleName();
    private Context mContext = this;

    private static final String sendbirdAppId = "D28B2F73-8E0A-4DB2-B568-96D6033855B6";

    @Override
    public void onCreate() {

        Log.d(LOG_TAG, "Service onCreate...");
        SendBird.init(sendbirdAppId, mContext);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(LOG_TAG, "Service has been started...");

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "Service destroyed...");
    }
}
