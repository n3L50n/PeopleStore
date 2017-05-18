package com.node_coyote.placed;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.node_coyote.placed.dataPackage.PeopleStoreContract.PeopleStoreEntry;

/**
 * Created by node_coyote on 4/8/17.
 */

public class PeopleStoreCursorAdapter extends CursorAdapter {

    public PeopleStoreCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    /**
     * Create a new empty list item view. Use bindView below to set data to the new view
     * @param context app context
     * @param cursor Cursor where the new data comes from, already in correct position
     * @param parent Attached to parent view
     * @return new list item view
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * Bind item data to the new view made above, pointed to current row by the cursor
     * @param view  the newView
     * @param context app context
     * @param cursor    Cursor where we get the data, already in correct row position
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {


        // Find views to modify
        TextView contactFirstNameView = (TextView) view.findViewById(R.id.contact_first_name_text);
        TextView contactLastNameView = (TextView) view.findViewById(R.id.contact_last_name_text);
        TextView contactZipView = (TextView) view.findViewById(R.id.zip_text);

        // Find the Columns to grab data
        int firstNameColumnIndex = cursor.getColumnIndex(PeopleStoreEntry.COLUMN_FIRST_NAME);
        int lastNameColumnIndex = cursor.getColumnIndex(PeopleStoreEntry.COLUMN_LAST_NAME);
        int zipColumnIndex = cursor.getColumnIndex(PeopleStoreEntry.COLUMN_ZIP);

        // Read attributes from Cursor for current inventory item
        String firstName = cursor.getString(firstNameColumnIndex);
        String lastName = cursor.getString(lastNameColumnIndex);
        final int zip = cursor.getInt(zipColumnIndex);

        if (TextUtils.isEmpty(firstName)){
            firstName = context.getString(R.string.empty_first_name);
        }

        if (TextUtils.isEmpty(lastName)){
            lastName = context.getString(R.string.empty_last_name);
        }

        // update UI
        contactFirstNameView.setText(firstName);
        contactLastNameView.setText(lastName);
        contactZipView.setText(String.valueOf(zip));
    }
}
