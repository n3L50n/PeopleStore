package com.node_coyote.placed.dataPackage;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.node_coyote.placed.dataPackage.PeopleStoreContract.PeopleStoreEntry;

/**
 * Created by node_coyote on 5/17/17.
 */

/**
 * {@link ContentProvider} for PeopleStore app
 */
public class PeopleStoreProvider extends ContentProvider {

    /** tag for log messages **/
    public static final String LOG_TAG = PeopleStoreProvider.class.getSimpleName();

    /** uri matcher code for the content uri for the contacts table **/
    private static final int CONTACTS = 42;

    /** uri matcher code for the content uri for a single contact item in the people store table **/
    private static final int CONTACT_ID = 9;

    /**
     * UriMatcher object to match a content uri to a code
     */
    private static final UriMatcher sMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /**
     *
     */
    static{
        sMatcher.addURI(PeopleStoreContract.CONTENT_AUTHORITY, PeopleStoreContract.PATH_CONTACTS, CONTACTS);
        sMatcher.addURI(PeopleStoreContract.CONTENT_AUTHORITY, PeopleStoreContract.PATH_CONTACTS + "/#", CONTACT_ID);
    }

    /** A database helper object **/
    private PeopleStoreDatabaseHelper mHelper;

    @Override
    public boolean onCreate() {
            mHelper = new PeopleStoreDatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        // We need a readable database
        SQLiteDatabase database = mHelper.getReadableDatabase();

        Cursor cursor;

        // match uri to code
        int match = sMatcher.match(uri);
        switch (match) {
            case CONTACTS:
                // query inventory table directly
                cursor = database.query(PeopleStoreEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CONTACT_ID:
                // query a row by id
                selection = PeopleStoreEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(PeopleStoreEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI" + uri);
        }

        // Set notification uri on cursor to update us if data at this uri changes
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sMatcher.match(uri);
        switch (match) {
            case CONTACTS:
                return PeopleStoreEntry.CONTENT_LIST_TYPE;
            case CONTACT_ID:
                return PeopleStoreEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown uri " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        // Insert a new item into the inventory
        final int match = sMatcher.match(uri);
        switch (match) {
            case CONTACTS:
                return insertItem(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Helper method to insert a new contact into the database
     * @param uri
     * @param values
     * @return new content uri for that specific row in the database
     */
    public Uri insertItem(Uri uri, ContentValues values){

        // Let's check if the contact first name is null
        String firstName = values.getAsString(PeopleStoreEntry.COLUMN_FIRST_NAME);
        if (firstName == null){
            throw new IllegalArgumentException("All contacts need a first name");
        }

        // Let's check if the contact last name is null
        String lastName = values.getAsString(PeopleStoreEntry.COLUMN_LAST_NAME);
        if (lastName == null){
            throw new IllegalArgumentException("All contacts need a last name");
        }

        Integer zip = values.getAsInteger(PeopleStoreEntry.COLUMN_ZIP);
        if (zip != null && zip < 0){
            throw new IllegalArgumentException("Zip should not be empty or negative");
        }

        // Let's check if the contact date of birth is null
        String birth = values.getAsString(PeopleStoreEntry.COLUMN_BIRTH);
        if (birth == null ){
            throw new IllegalArgumentException("Birth should not be empty");
        }

        // Let's check if the contact phone number is null
        String number = values.getAsString(PeopleStoreEntry.COLUMN_PHONE_NUMBER);
        if (number == null ){
            throw new IllegalArgumentException("Phone number should not be empty");
        }

        // Get writable database
        SQLiteDatabase database = mHelper.getWritableDatabase();

        // insert new contact with given values
        long id = database.insert(PeopleStoreEntry.TABLE_NAME, null, values);

        // Insertion fails if id is -1. Log it with error and return null
        if (id == -1){
            Log.v(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify listeners of change
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Get the writable database
        SQLiteDatabase database = mHelper.getWritableDatabase();

        // Track number of deleted rows
        int deletedRows;

        final int match = sMatcher.match(uri);
        switch (match) {
            case CONTACTS:
                // Delete all rows that match selection and selectionArgs
                deletedRows = database.delete(PeopleStoreEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CONTACT_ID:
                // Delete single row given by id in the uri
                selection = PeopleStoreEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri)) };
                deletedRows = database.delete(PeopleStoreEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion not supported for " + uri);
        }

        if (deletedRows != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return deletedRows;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sMatcher.match(uri);
        // let's check if this case is updating the whole database or a row
        switch (match) {
            case CONTACTS:
                return updateItem(uri, values, selection, selectionArgs);
            case CONTACT_ID:
                // let's pull out the id from the uri so we know which row to update
                selection = PeopleStoreEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateItem(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Helper update method
     * @param uri
     * @param values
     * @param selection
     * @param selectionArgs
     * @return
     */
    public int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        // Let's validate our values. Valuedate.
        if (values.containsKey(PeopleStoreEntry.COLUMN_FIRST_NAME)) {
            String firstName = values.getAsString(PeopleStoreEntry.COLUMN_FIRST_NAME);
            if (firstName == null){
                throw new IllegalArgumentException("PeopleStore contacts require a first name");
            }
        }

        if (values.containsKey(PeopleStoreEntry.COLUMN_LAST_NAME)) {
            String lastName = values.getAsString(PeopleStoreEntry.COLUMN_LAST_NAME);
            if (lastName == null){
                throw new IllegalArgumentException("PeopleStore contacts require a last name");
            }
        }

        if (values.containsKey(PeopleStoreEntry.COLUMN_ZIP)) {
            Integer zip = values.getAsInteger(PeopleStoreEntry.COLUMN_ZIP);
            if (zip != null){
                throw new IllegalArgumentException("PeopleStore contacts require a zip");
            }
        }

        if (values.containsKey(PeopleStoreEntry.COLUMN_BIRTH)) {
            String birth = values.getAsString(PeopleStoreEntry.COLUMN_BIRTH);
            if (birth == null){
                throw new IllegalArgumentException("PeopleStore contacts require birth date");
            }
        }

        if (values.containsKey(PeopleStoreEntry.COLUMN_PHONE_NUMBER)) {
            String number = values.getAsString(PeopleStoreEntry.COLUMN_PHONE_NUMBER);
            if (number == null){
                throw new IllegalArgumentException("PeopleStore contacts require a phone number");
            }
        }


        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mHelper.getWritableDatabase();

        int updatedRows = database.update(PeopleStoreEntry.TABLE_NAME, values, selection, selectionArgs);

        if (updatedRows != 0 ){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return updatedRows;
    }
}
