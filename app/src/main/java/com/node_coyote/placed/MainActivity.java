package com.node_coyote.placed;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.node_coyote.placed.dataPackage.PeopleStoreContract.PeopleStoreEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    /** Used as identifier for the contact loader data. int can be arbitrary **/
    private static final int CONTACT_LOADER = 42;

    /** Our list view adapter **/
    PeopleStoreCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PeopleStoreDetailActivity.class);
                startActivity(intent);
            }
        });

        // Find list view to populate with data
        ListView itemListView = (ListView) findViewById(R.id.list);

        // Show empty view when there aren't any inventory items
        View emptyView = findViewById(R.id.empty_view);
        itemListView.setEmptyView(emptyView);

        mCursorAdapter = new PeopleStoreCursorAdapter(this, null);
        itemListView.setAdapter(mCursorAdapter);

        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, PeopleStoreDetailActivity.class);

                Uri currentItemUri = ContentUris.withAppendedId(PeopleStoreEntry.CONTENT_URI, id);

                intent.setData(currentItemUri);

                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(CONTACT_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Defines projection specifying columns from table
        String[] projection = {
                PeopleStoreEntry._ID,
                PeopleStoreEntry.COLUMN_FIRST_NAME,
                PeopleStoreEntry.COLUMN_LAST_NAME,
                PeopleStoreEntry.COLUMN_ZIP
        };

        // Loader to execute Content Provider's query method on background thread
        return new CursorLoader(this,
                PeopleStoreEntry.CONTENT_URI,
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
