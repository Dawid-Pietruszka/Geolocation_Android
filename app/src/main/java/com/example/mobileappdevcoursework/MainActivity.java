package com.example.mobileappdevcoursework;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialises new notifications and notification manager objects
        Notifications notifications = new Notifications(this);
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(this.NOTIFICATION_SERVICE);


        //Code to check if permission is granted & ask for permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !notificationManager.isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(
                    android.provider.Settings
                            .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);

            //Send a notification asking for Do Not Disturb Permission(As permission must be granted manually)
            notifications.sendNotification(this.getString(R.string.do_Not_Disturb_Title), this.getString(R.string.do_Not_Disturb_Desc));
            startActivity(intent);
        }

        Button btnViewGeofence = (Button) findViewById(R.id.btnViewGeofence);

        //Initialises map with displayOnly enabled
        Button btnAddGeofence = (Button) findViewById(R.id.btnAddGeofence);
        //Initialises GeofenceOptionsActivity which lets user enter a name and make new geofences
        Button btnViewMap = (Button) findViewById(R.id.btnViewMap);
        //Used to finish activity
        Button btnExit = (Button) findViewById(R.id.btnExit);

        btnAddGeofence.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent activityA = new Intent(MainActivity.this, GeofenceOptionsActivity.class);
                startActivity(activityA);

            }
        });

        btnViewGeofence.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent activityA = new Intent(MainActivity.this, CheckZonesActivity.class);
                startActivity(activityA);
            }
        });

        btnViewMap.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent activityA = new Intent(MainActivity.this, MapsActivity.class);
                activityA.putExtra("displayOnly", true); //pass displayOnly
                startActivity(activityA);
            }
        });


        btnExit.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                finish();
                System.exit(0);
            }
        });

    }


}