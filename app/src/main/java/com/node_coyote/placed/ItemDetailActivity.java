package com.node_coyote.placed;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.node_coyote.placed.dataPackage.PlacedContract.PlacedEntry;

/**
 * Created by node_coyote on 4/8/17.
 */

public class ItemDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    /** Identifier for item data loader **/
    private static final int EXISTING_ITEM_LOADER = 42;

    /** Content uri for an existing item in the inventory **/
    private Uri mCurrentItmeUri;

    /** EditText field for product name **/
    private EditText mNameEditText;

    /** EditText field for product quantity **/
    private EditText mQuantityEditText;

    /** EditText field for product price **/
    private EditText mPriceEditText;

    /** Let's use a boolean to keep track of whether or not a user has edited an item **/
    private boolean mItemHasChanged = false;

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mItemHasChanged = true;
            return false;
        }
    };

    protected void onCreate(Bundle savedInstancestate){
        super.onCreate(savedInstancestate);
        setContentView(R.layout.item_detail);

        // Are we editing an item or creating a new one?
        Intent intent = getIntent();
        Uri mCurrentItmeUri = intent.getData();

        // If there isn't an id, let's create a new item
        if (mCurrentItmeUri == null){
            setTitle(R.string.item_detail_activity_add_item);
        } else {
            setTitle(getString(R.string.item_detail_activity_edit_item));
        }

        mNameEditText = (EditText) findViewById(R.id.edit_product_name_text_view);
        mQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity_text_view);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price_text_view);

        mNameEditText.setOnTouchListener(mOnTouchListener);
        mQuantityEditText.setOnTouchListener(mOnTouchListener);
        mPriceEditText.setOnTouchListener(mOnTouchListener);

//        getActionBar().setDisplayHomeAsUpEnabled(true);
        Button saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveItem();
            }
        });
    }

    /**
     * Get user input from editor and save item to database
     */
    private void saveItem() {
        // Read from input fields then trip empty garbage
        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();

        // Check if this is a new item and if all fields are blank
        if (mCurrentItmeUri == null &&
                TextUtils.isEmpty(nameString) &&
                TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(priceString)){
            // Jump out early. No need to run any more operations
            return;
        }

        ContentValues values = new ContentValues();
        values.put(PlacedEntry.COLUMN_PRODUCT_NAME, nameString);

        // Let's set quantity to 0 by default, then check if the field is empty
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)){
            quantity = Integer.parseInt(quantityString);
        }
        values.put(PlacedEntry.COLUMN_PRODUCT_QUANTITY, quantity);

        // Let's set a price of 0.00, then check if the field is empty
        double price = 0.00;
        if (!TextUtils.isEmpty(priceString)){
            price = Double.parseDouble(priceString);
        }
        values.put(PlacedEntry.COLUMN_PRODUCT_PRICE, price);

        if (mCurrentItmeUri == null) {
            Uri newUri = getContentResolver().insert(PlacedEntry.CONTENT_URI, values);

            // Let's show a toast of whether or not the save was successful
            if (newUri == null){
                // If the new uri is empty, the save didn't happen
                Toast.makeText(this, getString(R.string.save_item_failed), Toast.LENGTH_SHORT).show();
            } else {
                // If the a new uri is returned, the save most likely happened.
                Toast.makeText(this, getString(R.string.save_item_winning), Toast.LENGTH_SHORT).show();
            }
        } else {
            // Or this item exists. so we should update the uri
            int rowsAffected = getContentResolver().update(mCurrentItmeUri, values, null, null);

            // If this update was successful or not, let's show a toast
            if (rowsAffected == 0){
                Toast.makeText(this, getString(R.string.update_item_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.update_item_winning), Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                PlacedEntry._ID,
                PlacedEntry.COLUMN_PRODUCT_NAME,
                PlacedEntry.COLUMN_PRODUCT_QUANTITY,
                PlacedEntry.COLUMN_PRODUCT_PRICE
        };

        return new CursorLoader(this,
                mCurrentItmeUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Let's not do this if we have an empty cursor or less than 1 row
        if (cursor == null && cursor.getCount() < 1){
            return;
        }

        if (cursor.moveToFirst()){
            // Find columns with item attributes
            int nameColumnIndex = cursor.getColumnIndex(PlacedEntry.COLUMN_PRODUCT_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(PlacedEntry.COLUMN_PRODUCT_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(PlacedEntry.COLUMN_PRODUCT_PRICE);

            // Get the values from the cursor
            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);

            // Update UI
            mNameEditText.setText(name);
            mQuantityEditText.setText(quantity);
            mPriceEditText.setText(String.valueOf(price));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Let's clear the loader if it's invalidated
        mNameEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");

    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null){
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // Let's check if anything has changed. If not, go ahead and go back
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // If so, let's pop up a dialog
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };

        // Show an unsaved changes dialog
        showUnsavedChangesDialog(discardButtonClickListener);
    }
}
