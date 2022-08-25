package com.example.mobileappdevcoursework;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

import static android.provider.Settings.System.getString;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private AudioManager audioManager;
    private static final String TAG = "GeofenceBroadcastReceiv";
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        Notifications notifications = new Notifications(context);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);



        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        //Used to get the max volume
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);

        if(geofencingEvent.hasError())
        {
            Log.d(TAG, "onReceive: Error receiving geofence event");
            return;
        }

        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();

        for(Geofence geofence: geofenceList) //For loop used to trigger all geofences on list
        {
            Log.d(TAG, "onReceive: " + geofence.getRequestId());
        }

        //Location location = geofencingEvent.getTriggeringLocation();

        int transitionType = geofencingEvent.getGeofenceTransition();

        //Display in log what mode the phone is on before dwell is activated
        switch (audioManager.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
                Log.d(TAG,"Silent mode");
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                Log.d(TAG,"Vibrate mode");
                break;
            case AudioManager.RINGER_MODE_NORMAL:
                Log.d(TAG,"Normal mode");
                break;
        }

        //Used to monitor the transition type, let the user know about the transition(via notification/toast),
        //and switch phone ringer mode and volume
        switch (transitionType)
        {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Toast.makeText(context, context.getString(R.string.geofence_Enter), Toast.LENGTH_SHORT).show();
                break;

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Toast.makeText(context, context.getString(R.string.geofence_Exit) + ", " + context.getString(R.string.geofence_Exit_Desc), Toast.LENGTH_SHORT).show();
                notifications.sendNotification(context.getString(R.string.geofence_Exit), context.getString(R.string.geofence_Exit_Desc), MapsActivity.class);
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                audioManager.setStreamVolume(AudioManager.STREAM_RING, maxVolume, AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND);
                break;

            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Toast.makeText(context, context.getString(R.string.geofence_Dwell) + ", " + context.getString(R.string.geofence_Dwell_Desc), Toast.LENGTH_SHORT).show();
                notifications.sendNotification(context.getString(R.string.geofence_Dwell), context.getString(R.string.geofence_Dwell_Desc), MapsActivity.class);
                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                break;
        }
        //Display in log what mode the phone is on after dwell is activated
        switch (audioManager.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
                Log.d(TAG,"Silent mode");
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                Log.d(TAG,"Vibrate mode");
                break;
            case AudioManager.RINGER_MODE_NORMAL:
                Log.d(TAG,"Normal mode");
                break;
        }
    }
}