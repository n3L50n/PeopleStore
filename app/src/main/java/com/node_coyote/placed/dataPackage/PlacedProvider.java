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

import com.node_coyote.placed.dataPackage.PlacedContract.PlacedEntry;

/**
 * Created by node_coyote on 4/7/17.
 */

/**
 * {@link ContentProvider} for Placed app
 */
public class PlacedProvider extends ContentProvider {

    /** tag for log messages **/
    public static final String LOG_TAG = PlacedProvider.class.getSimpleName();

    /** uri matcher code for the content uri for the inventory table **/
    private static final int INVENTORY = 42;

    /** uri matcher code for the content uri for a single inventory item in the inventory table **/
    private static final int INVENTORY_ID = 9;

    /**
     * UriMatcher object to match a content uri to a code
     */
    private static final UriMatcher sMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /**
     *
     */
    static{
        sMatcher.addURI(PlacedContract.CONTENT_AUTHORITY, PlacedContract.PATH_INVENTORY, INVENTORY);
        sMatcher.addURI(PlacedContract.CONTENT_AUTHORITY, PlacedContract.PATH_INVENTORY + "/#", INVENTORY_ID);
    }

    /** A database helper object **/
    private PlacedDatabaseHelper mHelper;

    @Override
    public boolean onCreate() {
            mHelper = new PlacedDatabaseHelper(getContext());
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
            case INVENTORY:
                // query inventory table directly
                cursor = database.query(PlacedContract.PlacedEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case INVENTORY_ID:
                // query a row by id
                selection = PlacedEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(PlacedContract.PlacedEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
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
            case INVENTORY:
                return PlacedEntry.CONTENT_LIST_TYPE;
            case INVENTORY_ID:
                return PlacedEntry.CONTENT_ITEM_TYPE;
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
            case INVENTORY:
                return insertItem(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Helper method to insert a new item into the database
     * @param uri
     * @param values
     * @return new content uri for that specific row in the database
     */
    public Uri insertItem(Uri uri, ContentValues values){

        // Let's check if the product name is null
        String name = values.getAsString(PlacedEntry.COLUMN_PRODUCT_NAME);
        if (name == null){
            throw new IllegalArgumentException("All inventory items need a name");
        }

        // We should make sure the quantity doesn't go below 0. We don't want a negative inventory
        Integer quantity = values.getAsInteger(PlacedEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity != null && quantity < 0){
            throw new IllegalArgumentException("Our inventory should not be empty or negative");
        }

        // The price should also not go below 0.00
        Double price = values.getAsDouble(PlacedEntry.COLUMN_PRODUCT_PRICE);
        if (price != null && price < 0){
            throw new IllegalArgumentException("Our prices should not be empty or in the negative");
        }

        // Get writable database
        SQLiteDatabase database = mHelper.getWritableDatabase();

        // insert new pet with given values
        long id = database.insert(PlacedEntry.TABLE_NAME, null, values);

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
            case INVENTORY:
                // Delete all rows that match selection and selectionArgs
                deletedRows = database.delete(PlacedEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INVENTORY_ID:
                // Delete single row given by id in the uri
                selection = PlacedEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri)) };
                deletedRows = database.delete(PlacedEntry.TABLE_NAME, selection, selectionArgs);
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
            case INVENTORY:
                return updateItem(uri, values, selection, selectionArgs);
            case INVENTORY_ID:
                // let's pull out the id from the uri so we know which row to update
                selection = PlacedEntry._ID + "=?";
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
        if (values.containsKey(PlacedEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(PlacedEntry.COLUMN_PRODUCT_NAME);
            if (name == null){
                throw new IllegalArgumentException("Items require a name");
            }
        }

        if (values.containsKey(PlacedEntry.COLUMN_PRODUCT_QUANTITY)) {
            Integer quantity = values.getAsInteger(PlacedEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity != null && quantity < 0){
                throw new IllegalArgumentException("Items require a valid quantity");
            }
        }

        if (values.containsKey(PlacedEntry.COLUMN_PRODUCT_PRICE)) {
            Double price = values.getAsDouble(PlacedEntry.COLUMN_PRODUCT_PRICE);
            if (price != null && price < 0.00){
                throw new IllegalArgumentException("Items require a valid price");
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mHelper.getWritableDatabase();

        int updatedRows = database.update(PlacedEntry.TABLE_NAME, values, selection, selectionArgs);

        if (updatedRows != 0 ){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return updatedRows;
    }
}
