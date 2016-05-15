package com.example.phamngochieu.secretreadmessage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Boardcast này dùng để gọi Service DelayedMessageService dậy khi Service bị chết
 */
public class RestartServiceReceiver extends BroadcastReceiver {
    private static final String TAG = "RestartServiceReceiver";
    @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "onReceive");
            context.startService(new Intent(context.getApplicationContext(), DelayedMessageService.class));
        }
}
