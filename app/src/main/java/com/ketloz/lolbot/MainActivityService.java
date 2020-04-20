package com.ketloz.lolbot;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

public class MainActivityService extends JobIntentService {
    static final int JOB_ID = 1000;

    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, MainActivityService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        // Get summoner info here along with recent match history (up to 20)
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}