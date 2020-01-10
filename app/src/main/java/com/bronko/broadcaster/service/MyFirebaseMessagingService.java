package com.bronko.broadcaster.service;

import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d("FirebaseMessaging", "From: " + remoteMessage.getFrom());


        if(remoteMessage.getData().containsKey("publishing")) {
            boolean publishing = Boolean.valueOf(remoteMessage.getData().get("publishing")).booleanValue();
            String broadcastId = remoteMessage.getData().get("id");

            if(publishing) {
                String streamAddress = remoteMessage.getData().get("streamAddress");
                if (streamAddress != null) {
                    startService(streamAddress);
                    addBroadcast(broadcastId,true, 0,0,0);
                }
            }else{
                stopService();
            }
        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.d("FirebaseMessaging", "new token: " + s);
        this.addDevice(s);
    }

    public void startService(String streamAddress) {
        Log.i("FirebaseMessaging", "Starting Service streamAddress: " + streamAddress);
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.putExtra("streamAddress", streamAddress);
        ContextCompat.startForegroundService(this, serviceIntent);
    }
    public void stopService() {
        Log.d("FirebaseMessaging", "Stopping Service");
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);
    }

    private void addDevice(String token){
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        Map<String, Object> map = new HashMap<>();
        map.put("deviceModel", Build.MODEL);
        map.put("token", token);
        map.put("publishing", false);
        map.put("locationtracking", false);
        map.put("cameraId", 1);
        map.put("battery", 0);
        map.put("profile", "240");
        map.put("screenOn", true);
        map.put("charging", "Discharging");
        map.put("privacyStatus", "private");
        map.put("deviceDown", false);
        map.put("audioDetection", false);
        map.put("amplitudeThreshold", 5000);
        map.put("silenceThreshold", 5000 * 60);
        map.put("soundThreshold", 200);
        firestore.collection("devices").document(this.getAndroidId(firestore)).set(map);
    }

    private void addBroadcast(String broadcastId, boolean running, double latitude, double longitude, double altitude){
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        Map<String, Object> data = new HashMap<>();
        data.put("latitude", latitude);
        data.put("longitude", longitude);
        data.put("altitude", altitude);
        data.put("running", running);
        data.put("time", System.currentTimeMillis());

        firestore.collection("devices").document(this.getAndroidId(firestore)).collection("broadcasts").document(broadcastId).set(data);
    }

    private String getAndroidId(FirebaseFirestore firestore){
       return Settings.Secure.getString(firestore.getApp().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
