package com.node_coyote.placed;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.node_coyote.placed.dataPackage.PlacedContract.PlacedEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    /** Used as identifier for the item loader data. int can be arbitrary **/
    private static final int ITEM_LOADER = 42;

    /** Our list view adapter **/
    PlacedCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ItemDetailActivity.class);
                startActivity(intent);
            }
        });

        // Find list view to populate with data
        ListView itemListView = (ListView) findViewById(R.id.list);

        // Show empty view when there aren't any inventory items
        View emptyView = findViewById(R.id.empty_view);
        itemListView.setEmptyView(emptyView);

        mCursorAdapter = new PlacedCursorAdapter(this, null);
        itemListView.setAdapter(mCursorAdapter);

        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ItemDetailActivity.class);

                Uri currentItemUri = ContentUris.withAppendedId(PlacedEntry.CONTENT_URI, id);

                intent.setData(currentItemUri);

                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(ITEM_LOADER, null, this);
    }

    private void insertItem(){
        ContentValues values = new ContentValues();
    }

    /** Be careful with this one. It deletes all the rows in the database. **/
    private void deleteAllItems(){
        int deletedRows = getContentResolver().delete(PlacedEntry.CONTENT_URI, null, null);
        Log.v("MainActivity", deletedRows + " rows deleted from inventory.");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Defines projection specifying columns from table
        String[] projection = {
                PlacedEntry._ID,
                PlacedEntry.COLUMN_PRODUCT_NAME,
                PlacedEntry.COLUMN_PRODUCT_QUANTITY,
                PlacedEntry.COLUMN_PRODUCT_PRICE
        };

        // Loader to execute Content Provider's query method on background thread
        return new CursorLoader(this,
                PlacedEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update cursor data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
