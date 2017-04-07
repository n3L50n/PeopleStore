package com.node_coyote.placed.dataPackage;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by node_coyote on 4/7/17.
 */

/**
 * {@link ContentProvider} for Placed app
 */
public class PlacedProvider extends ContentProvider {

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
        sMatcher.addURI(PlacedContract.CONTENT_AUTHORITY, PlacedContract.PATH_INVENTORY, INVENTORY_ID);
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
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
