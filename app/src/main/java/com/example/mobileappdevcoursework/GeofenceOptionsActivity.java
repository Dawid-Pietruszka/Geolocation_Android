package com.example.mobileappdevcoursework;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class GeofenceOptionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geofence_options);

        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        Button btnConfirm = (Button) findViewById(R.id.btnConfirm);


        btnConfirm.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                EditText editName  = (EditText) findViewById(R.id.txtName);
                String name = editName.getText().toString();

                //Checks if user entered a name, displays a toast if not
                if (name.matches("")) {
                    Toast.makeText(GeofenceOptionsActivity.this , "You did not enter a geofence name", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    //Code to scan the database in order to find a matching name
                    List<String[]> names2 = null;

                    DatabaseManipulator dm = new DatabaseManipulator(GeofenceOptionsActivity.this);

                    names2 = dm.selectAll();
                    ArrayList<String> nameList = new ArrayList<String>();
                    for (String[] id : names2) {

                        nameList.add(id[1]);
                    }

                    if(nameList.contains(name))
                    {
                        Toast.makeText(GeofenceOptionsActivity.this , "This name has already been used, please choose a different one", Toast.LENGTH_SHORT).show();
                    }

                    else
                    {
                        Intent activityA = new Intent(GeofenceOptionsActivity.this, MapsActivity.class);
                        activityA.putExtra("Name", name);
                        startActivity(activityA);
                    }


                }

            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Intent activityA = new Intent(GeofenceOptionsActivity.this, MainActivity.class);
                startActivity(activityA);
            }

        });
    }
}