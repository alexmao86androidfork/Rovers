package com.schiztech.rovers.actions.flashlight;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;


import com.schiztech.roverflashlightaction.R;

import java.io.IOException;

public class FlashlightService extends Service {
    private static final int SERVICE_FOREGROUND_ID = 42;
    private boolean mIsFlashlightOn = false;
    private Camera mCamera;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mCamera == null)
            mCamera = Camera.open();

        if (mIsFlashlightOn)
            turnFlashlightOff(true);
        else
            turnFlashlightOn();

        startForeground(SERVICE_FOREGROUND_ID, getNotificationCompat());
        return START_NOT_STICKY;
    }

    private Notification getNotificationCompat(){
        Intent intent = new Intent(this, FlashlightService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, intent, 0);

        return
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_flashlight_on)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_flashlight_orange))
                        .setContentTitle("Flashlight is on")
                        .setContentText("Touch to turn off")
                        .setContentIntent(pi)
                        .setOngoing(true)
                        .addAction(R.drawable.ic_flashlight_off,"Turn Off", pi)
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void turnFlashlightOn() {
        try {
            Camera.Parameters params = mCamera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(params);
            mCamera.startPreview();
            try {
                mCamera.setPreviewTexture(new SurfaceTexture(0));
            } catch (IOException e) {
                e.printStackTrace();
            }
            mIsFlashlightOn = true;
            Toast.makeText(this, "Flashlight is on", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void turnFlashlightOff(boolean stopService) {
        try {
            Camera.Parameters params = mCamera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(params);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            mIsFlashlightOn = false;
            Toast.makeText(this, "Flashlight is off", Toast.LENGTH_SHORT).show();

            if(stopService)
                stopSelf();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        if(mIsFlashlightOn)
            turnFlashlightOff(false);

        mCamera = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }
}
