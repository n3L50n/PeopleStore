package com.node_coyote.placed;

import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

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
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

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
}
