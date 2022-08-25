package com.example.mobileappdevcoursework;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.google.android.gms.tasks.OnSuccessListener;


public class DatabaseManipulator {
    private static final String DATABASE_NAME = "mydatabase.db";
    private static int DATABASE_VERSION = 1;
    static final String TABLE_NAME = "newtable";
    private static Context context;
    static SQLiteDatabase db;
    private SQLiteStatement insertStmt;

    private static final String INSERT = "insert into " + TABLE_NAME
            + " (name,latLng,radius) values (?,?,?)";

    public DatabaseManipulator(Context context) {
        DatabaseManipulator.context = context;
        OpenHelper openHelper = new OpenHelper(this.context);
        DatabaseManipulator.db = openHelper.getWritableDatabase();
        this.insertStmt = DatabaseManipulator.db.compileStatement(INSERT);
    }

    //Insert method - name, latLng, radius
    public long insert(String name, String latLng, String radius) {
        this.insertStmt.bindString(1, name);
        this.insertStmt.bindString(2, latLng);
        this.insertStmt.bindString(3, radius);
        return this.insertStmt.executeInsert();
    }
    //Delete method (via name)
    public Integer deleteName(String name)
    {
        return db.delete(TABLE_NAME,"name=?",new String[]{name});
    }

    //Delete all method
    public void deleteAll() {
        db.delete(TABLE_NAME, null, null);
    }

    //Select all data method
    public List<String[]> selectAll() {
        List<String[]> list = new ArrayList<String[]>();
        Cursor cursor = db.query(TABLE_NAME, new String[]{"id", "name", "latLng", "radius"}, null, null, null, null, "name asc");
        int x = 0;
        if (cursor.moveToFirst()) {
            do {
                String[] b1 = new String[]{
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3)};
                list.add(b1);
                x++;
            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        cursor.close();
        return list;
    }

    private static class OpenHelper extends SQLiteOpenHelper {
        OpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE "
                    + TABLE_NAME
                    + " (id INTEGER PRIMARY KEY, name TEXT, latLng TEXT, radius TEXT)");
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            DATABASE_VERSION = newVersion;
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}

