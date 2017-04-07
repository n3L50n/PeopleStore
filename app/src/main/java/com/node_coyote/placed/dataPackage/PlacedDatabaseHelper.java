package com.node_coyote.placed.dataPackage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by node_coyote on 4/7/17.
 */

public class PlacedDatabaseHelper extends SQLiteOpenHelper {

    /** Name of the database **/
    private static final String DATABASE_NAME = "inventory.db";

    /** Change this number whenever the database schema is changed **/
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructor to use for a new instance of the helper
     * @param context
     */
    public PlacedDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /** called when the database is first created **/
    @Override
    public void onCreate(SQLiteDatabase db) {
        // A String holding a SQL statement to create the inventory table
        String SQL_CREATE_INVENTORY_TABLE  = "CREATE TABLE "  ;

        db.execSQL(SQL_CREATE_INVENTORY_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // No need to upgrade to another version as the first version hasn't been finished
    }
}
