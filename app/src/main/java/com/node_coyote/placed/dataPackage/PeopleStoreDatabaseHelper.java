package com.node_coyote.placed.dataPackage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.node_coyote.placed.dataPackage.PeopleStoreContract.PeopleStoreEntry;

/**
 * Created by node_coyote on 5/17/17.
 */

public class PeopleStoreDatabaseHelper extends SQLiteOpenHelper {

    /** Name of the database **/
    private static final String DATABASE_NAME = "peopleStore.db";

    /** Change this number whenever the database schema is changed **/
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructor to use for a new instance of the helper
     * @param context
     */
    public PeopleStoreDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /** called when the database is first created **/
    @Override
    public void onCreate(SQLiteDatabase db) {
        // A String holding a SQL statement to create the people store table
        String SQL_CREATE_PEOPLESTORE_TABLE  = "CREATE TABLE " + PeopleStoreEntry.TABLE_NAME + " ("
                + PeopleStoreEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PeopleStoreEntry.COLUMN_FIRST_NAME + " TEXT NOT NULL, "
                + PeopleStoreEntry.COLUMN_LAST_NAME + " TEXT NOT NULL, "
                + PeopleStoreEntry.COLUMN_ZIP + " INTEGER NOT NULL DEFAULT 0, "
                + PeopleStoreEntry.COLUMN_PHONE_NUMBER + " TEXT NOT NULL, "
                + PeopleStoreEntry.COLUMN_BIRTH + " TEXT NOT NULL, "
                + PeopleStoreEntry.COLUMN_CONTACT_IMAGE +  " TEXT);";

        db.execSQL(SQL_CREATE_PEOPLESTORE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // No need to upgrade to another version as the first version hasn't been finished
    }
}
