package com.node_coyote.placed;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

/**
 * Created by node_coyote on 4/8/17.
 */

public class PlacedCursorAdapter extends CursorAdapter {

    public PlacedCursorAdapter(Context context, Cursor c) {
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
    public void bindView(View view, Context context, Cursor cursor) {

    }
}
