package com.node_coyote.placed;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.node_coyote.placed.dataPackage.PlacedContract.PlacedEntry;

import java.io.FileDescriptor;
import java.io.IOException;


/**
 * Created by node_coyote on 4/8/17.
 */

public class ItemDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    /**
     * Identifier for the image directory opener intent
     */
    static final int CHOOSE_IMAGE_REQUEST = 0;

    /**
     * Identifier for item data loader
     **/
    private static final int EXISTING_ITEM_LOADER = 42;

    /**
     * Content uri for an existing item in the inventory
     **/
    private Uri mCurrentItemUri;

    /**
     * EditText field for product name
     **/
    private EditText mNameEditText;

    /**
     * EditText field for product quantity
     **/
    private EditText mQuantityEditText;

    /**
     * EditText field for product price
     **/
    private EditText mPriceEditText;

    private ImageButton mInventoryImageButton;
    private EditText mImageText;

    /**
     * Let's use a boolean to keep track of whether or not a user has edited an item
     **/
    private boolean mItemHasChanged = false;

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mItemHasChanged = true;
            return false;
        }
    };

    protected void onCreate(Bundle savedInstancestate) {
        super.onCreate(savedInstancestate);
        setContentView(R.layout.item_detail);

        // Are we editing an item or creating a new one?
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();
        Button deleteButton = (Button) findViewById(R.id.delete_button);
        mNameEditText = (EditText) findViewById(R.id.edit_product_name_text_view);
        mQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity_text_view);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price_text_view);
        Button subtractOne = (Button) findViewById(R.id.subtract_one_button);
        Button addMore = (Button) findViewById(R.id.add_one_button);
        mInventoryImageButton = (ImageButton) findViewById(R.id.product_image_view);
        mImageText = (EditText) findViewById(R.id.image_text);

        // If there isn't an id, let's create a new item
        if (mCurrentItemUri == null) {
            setTitle(R.string.item_detail_activity_add_item);
            deleteButton.setVisibility(View.GONE);
            subtractOne.setVisibility(View.GONE);
            addMore.setVisibility(View.GONE);

        } else {
            setTitle(getString(R.string.item_detail_activity_edit_item));
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }


        mNameEditText.setOnTouchListener(mOnTouchListener);
        mQuantityEditText.setOnTouchListener(mOnTouchListener);
        mPriceEditText.setOnTouchListener(mOnTouchListener);
        mImageText.setOnTouchListener(mOnTouchListener);

        Button saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveItem();
            }
        });

        Button orderMoreButton = (Button) findViewById(R.id.order_more_button);
        orderMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderMore(new String[]{"number42@gmailcom"}, "I'd like to order more.");
                Log.v("Clicked", "Fore real");
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });


        // Decrease inventory Button
        subtractOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pull out a new quantity to pass around
                int quantity = Integer.parseInt(mQuantityEditText.getText().toString());

                // Check if the field is empty
                if (TextUtils.isEmpty(String.valueOf(quantity))) {
                    quantity = 0;
                }

                if (quantity > 0) {
                    quantity--;
                    String newQuantity = Integer.toString(quantity);
                    ContentValues values = new ContentValues();
                    values.put(PlacedEntry.COLUMN_PRODUCT_QUANTITY, quantity);

                    int rows = getContentResolver().update(mCurrentItemUri, values, null, null);
                    if (rows != 0) {
                        mQuantityEditText.setText(newQuantity);
                    }
                }
            }
        });

        // Increase inventory Button
        addMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ContentValues values = new ContentValues();

                // Pull out a new quantity to pass around
                int quantity = Integer.parseInt(mQuantityEditText.getText().toString());

                // Check if the field is empty
                if (TextUtils.isEmpty(String.valueOf(quantity))) {
                    quantity = 0;
                }

                // Make sure we're not in the negative, then increase by 1
                if (quantity >= 0) {
                    quantity++;
                }

                // Send the quantity integer back to string format
                String newQuantity = Integer.toString(quantity);

                // Pass along the new value
                values.put(PlacedEntry.COLUMN_PRODUCT_QUANTITY, newQuantity);

                int rows = getContentResolver().update(mCurrentItemUri, values, null, null);
                if (rows != 0) {
                    mQuantityEditText.setText(newQuantity);
                }

            }
        });

        // Touching the button will open the image directory
        mInventoryImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                if (Build.VERSION.SDK_INT < 19) {
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                } else {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                }
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Choose Picture"), CHOOSE_IMAGE_REQUEST);
            }
        });
    }

    // looked at Google documentation and Carlos' https://github.com/crlsndrsjmnz/MyFileProviderExample
    // for help implementing photo storage process
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHOOSE_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                Uri path = data.getData();
                Log.v("PATH", path.toString());
                mImageText.setText(path.toString());
                mInventoryImageButton.setImageBitmap(getBitmapFromUri(path));
            }
        }
    }

    // looked at Google documentation and Carlos' https://github.com/crlsndrsjmnz/MyFileProviderExample
    // for help implementing photo storage process
    private Bitmap getBitmapFromUri(Uri uri) {

        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return bitmap;
        } catch (Exception e) {
            Log.e("bitMapException", "Failed to load bitmap", e);
            return null;
        } finally {
            try {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("ioException", "Error closing ParcelFile Descriptor", e);
            }
        }
    }

    /**
     * Get user input from editor and save item to database
     */
    private void saveItem() {
        // Read from input fields then trim empty garbage
        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String imageString = mImageText.getText().toString().trim();

        // Check if this is a new item and if all fields are blank
        if (mCurrentItemUri == null &&
                TextUtils.isEmpty(nameString) &&
                TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(imageString)) {
            // Jump out early. No need to run any more operations
            return;
        }

        ContentValues values = new ContentValues();

        values.put(PlacedEntry.COLUMN_PRODUCT_NAME, nameString);

        // Let's set quantity to 0 by default, then check if the field is empty
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(PlacedEntry.COLUMN_PRODUCT_QUANTITY, quantity);

        // Let's set a price of 0.00, then check if the field is empty
        double price = 0.00;
        if (!TextUtils.isEmpty(priceString)) {
            price = Double.parseDouble(priceString);
        }
        values.put(PlacedEntry.COLUMN_PRODUCT_PRICE, price);

        if (mCurrentItemUri == null) {
            Uri newUri = getContentResolver().insert(PlacedEntry.CONTENT_URI, values);

            // Let's show a toast of whether or not the save was successful
            if (newUri == null) {
                // If the new uri is empty, the save didn't happen
                Toast.makeText(this, getString(R.string.save_item_failed), Toast.LENGTH_SHORT).show();
            } else {
                // If the a new uri is returned, the save most likely happened.
                Toast.makeText(this, getString(R.string.save_item_winning), Toast.LENGTH_SHORT).show();
            }
        } else {
            // Or this item exists. so we should update the uri
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

            // If this update was successful or not, let's show a toast
            if (rowsAffected == 0) {
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
                PlacedEntry.COLUMN_PRODUCT_PRICE,
                PlacedEntry.COLUMN_PRODUCT_IMAGE
        };

        return new CursorLoader(this,
                mCurrentItemUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Let's not do this if we have an empty cursor or less than 1 row
        if (cursor == null && cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            // Find columns with item attributes
            int nameColumnIndex = cursor.getColumnIndex(PlacedEntry.COLUMN_PRODUCT_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(PlacedEntry.COLUMN_PRODUCT_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(PlacedEntry.COLUMN_PRODUCT_PRICE);
            int imageColumnIndex = cursor.getColumnIndex(PlacedEntry.COLUMN_PRODUCT_IMAGE);

            // Get the values from the cursor
            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            String image = cursor.getString(imageColumnIndex);

            // Update UI
            mNameEditText.setText(name);
            mQuantityEditText.setText(String.valueOf(quantity));
            mPriceEditText.setText(String.valueOf(price));
            mImageText.setText(image);

            // Thank you Udacity forums
            if (mImageText != null) {
                Uri path = Uri.parse(mImageText.getText().toString());
                mInventoryImageButton.setImageBitmap(getBitmapFromUri(path));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Let's clear the loader if it's invalidated
        mNameEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
        mImageText.setText("");

    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
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

    private void orderMore(String[] addresses, String subject) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setData(Uri.parse("mailto:"));
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(intent, "Send email"));
        }

    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_message);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteItem() {
        if (mCurrentItemUri != null) {
            int deletedRows = getContentResolver().delete(mCurrentItemUri, null, null);

            if (deletedRows == 0) {
                Toast.makeText(this, getString(R.string.delete_item_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.delete_item_winning), Toast.LENGTH_LONG).show();
            }
        }

        // Close Activity
        finish();
    }
}
