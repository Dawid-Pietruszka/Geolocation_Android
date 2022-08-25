package com.example.mobileappdevcoursework;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CheckZonesActivity extends ListActivity {

    TextView selection;
    DatabaseManipulator dm;
    List<String[]> names2 = null;
    String[] stg1;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_zones);
        displayList();

    }
    public void displayList()
    {
        //Code used to display data on the listview
        dm = new DatabaseManipulator(this);
        names2 = dm.selectAll();
        stg1 = new String[names2.size()];
        int x = 0;
        String stg;

        for (String[] name : names2) {
            stg = name[1] + " - "
                    + name[2] + " - "
                    + name[3];
            stg1[x] = stg;
            x++;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                stg1);
        this.setListAdapter(adapter);
        selection = (TextView) findViewById(R.id.check_selection);
    }

    public void onListItemClick(ListView parent, View v, int position, long id) {
        selection.setText(stg1[position]);
    }

    //onClick method used for delete button.
    //Deletes the selected field from the database(via name), then refreshes the activity
    public void onClick(View v) {
        String item = selection.getText().toString();

        int iend = item.indexOf(" ");

        String subString = "";
        if (iend != -1)
        {
            subString = item.substring(0 , iend); //this will give abc
        }

        dm.deleteName(subString);
        selection.setText("");
        Intent activityA = new Intent(CheckZonesActivity.this, MapsActivity.class);
        startActivity(activityA);
    }

}

