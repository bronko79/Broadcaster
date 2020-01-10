package com.bronko.broadcaster.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.bronko.broadcaster.MainActivity;
import com.bronko.broadcaster.R;
import com.pedro.encoder.input.gl.render.filters.object.TextObjectFilterRender;
import com.pedro.encoder.utils.gl.TranslateTo;
import com.pedro.rtplibrary.rtmp.RtmpCamera1;
import com.pedro.rtplibrary.rtmp.RtmpCamera2;

import net.ossrs.rtmp.ConnectCheckerRtmp;

public class ForegroundService extends Service {

    RtmpCamera2 rtmpCamera;

    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    @Override
    public void onCreate() {
        super.onCreate();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String streamAddress = intent.getStringExtra("streamAddress");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Broadcast-Service")
                .setContentText("Start streaming.")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
        //do heavy work on a background thread

        rtmpCamera = new RtmpCamera2(this, true, new ConnectCheckerRtmp() {
            @Override
            public void onConnectionSuccessRtmp() {

            }

            @Override
            public void onConnectionFailedRtmp(String reason) {

            }

            @Override
            public void onNewBitrateRtmp(long bitrate) {

            }

            @Override
            public void onDisconnectRtmp() {

            }

            @Override
            public void onAuthErrorRtmp() {

            }

            @Override
            public void onAuthSuccessRtmp() {

            }
        });
        rtmpCamera.prepareAudio(128000,44100,false,false, false);
        rtmpCamera.prepareVideo(240,320, 30,400,false, 0);
        rtmpCamera.startStream(streamAddress);

        //stopSelf();
        return START_NOT_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        rtmpCamera.stopStream();
        Log.i("ForegroundService", "onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void setTextToStream(String text, float textSize, int color) {
        TextObjectFilterRender textObjectFilterRender = new TextObjectFilterRender();
        rtmpCamera.getGlInterface().setFilter(textObjectFilterRender);
        //textObjectFilterRender.setText(text, 22, Color.RED);
        textObjectFilterRender.setText(text, textSize, color);
        textObjectFilterRender.setDefaultScale(rtmpCamera.getStreamWidth(),
                rtmpCamera.getStreamHeight());
        textObjectFilterRender.setPosition(TranslateTo.BOTTOM_LEFT);
        //spriteGestureController.setBaseObjectFilterRender(textObjectFilterRender); //Optional
    }
}