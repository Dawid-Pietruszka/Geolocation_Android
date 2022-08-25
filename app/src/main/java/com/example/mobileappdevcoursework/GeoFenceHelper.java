package com.example.mobileappdevcoursework;

import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.model.LatLng;

public class GeoFenceHelper extends ContextWrapper {

    private static final String TAG = "GeoFenceHelper";
    PendingIntent pendingIntent;

    public GeoFenceHelper(Context base)
    {
        super(base);
    }

    //Get and return a Geofencingrequest
    public GeofencingRequest getGeofencingRequest(Geofence geofence)
    {
        return new GeofencingRequest.Builder()
                .addGeofence(geofence)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build();
    }

    //Returns a geofence
    public Geofence getGeofence(String ID, LatLng latLng, float radius, int transitionTypes)
    {
        return new Geofence.Builder()
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setRequestId(ID)
                .setTransitionTypes(transitionTypes)
                .setLoiteringDelay(5000)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }

    //Returns a pendingIntent
    public PendingIntent getPendingIntent()
    {
        if(pendingIntent != null)
        {
            return pendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 2607, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    //Error checking using GeofenceStatusCodes
    public String getError(Exception e)
    {
        if(e instanceof ApiException)
        {
            ApiException apiException = (ApiException) e;
            switch (apiException.getStatusCode())
            {
                case GeofenceStatusCodes
                        .GEOFENCE_NOT_AVAILABLE:
                return "Geofence is not available";

                case GeofenceStatusCodes
                        .GEOFENCE_TOO_MANY_GEOFENCES:
                    return "Too many Geofences";

                case GeofenceStatusCodes
                        .GEOFENCE_TOO_MANY_PENDING_INTENTS:
                    return "Too many pending Intents";
            }
        }
        return e.getLocalizedMessage();
    }
}

