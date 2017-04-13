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
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.node_coyote.placed.dataPackage.PlacedContract;
import com.node_coyote.placed.dataPackage.PlacedContract.PlacedEntry;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by node_coyote on 4/8/17.
 */

public class ItemDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = ItemDetailActivity.class.getSimpleName();

    /**
     * Identifier for the image directory opener intent
     */
    static final int CHOOSE_IMAGE_REQUEST = 0;

    /**
     * Identifier for the camera intent
     */
    static final int REQUEST_IMAGE_CAPTURE = 1;

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

    /**
     * A button to take a photo and an ImageView to show a chosen photo
     */
    private ImageButton mInventoryImageButton;

    /**
     * A button to choose an existing photo
     */
    private Button mChooseExistingButton;

    /**
     * Variable to help saveItem method determine if fields have been filled out
     */
    private boolean mSaveHasBeenPushed = false;

    /**
     * Variable to store the path to a photo saved
     */
    String mCurrentPhotoPath;

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
        mChooseExistingButton = (Button) findViewById(R.id.choose_existing_photo);


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
                orderMore(new String[]{String.valueOf(R.string.email_address)}, String.valueOf(R.string.email_text));
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

                values.put(PlacedEntry.COLUMN_PRODUCT_PRICE, Double.parseDouble(mPriceEditText.getText().toString()));

                int rows = getContentResolver().update(mCurrentItemUri, values, null, null);
                if (rows != 0) {
                    mQuantityEditText.setText(newQuantity);
                }

            }
        });

        // Touching the button will open the image directory
        mChooseExistingButton.setOnClickListener(new View.OnClickListener() {
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

        mInventoryImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeInventoryPhoto();

            }
        });
    }

    private void takeInventoryPhoto(){
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (pictureIntent.resolveActivity(getPackageManager()) != null) {

            // Create the File for where the photo should go
            File photo = null;
            try {
                photo = createImageFile();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error while saving photo", e);
            }
            if (photo != null){
                Uri photoUri = FileProvider.getUriForFile(this, PlacedContract.CONTENT_AUTHORITY, photo);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(pictureIntent, REQUEST_IMAGE_CAPTURE);
                setPicture();
            }
        }
    }

    private File createImageFile() throws IOException {

        // Create a collision-resistant name for the image file
        String timeStamp = new SimpleDateFormat("yyyymmdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        // Get the file storage available to all apps. Deleted if user deletes app
        File storageFileDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, // This is the prefix
                ".jpg",          // We'll save it as a jpg
                storageFileDirectory // Store it here
        );
        // set the image path. we can use it with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();

        // Save the photo path to the data base
        ContentValues values = new ContentValues();
        values.put(PlacedEntry.COLUMN_PRODUCT_IMAGE, mCurrentPhotoPath);

        return image;
    }

    private void addGalleryPic(){
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    // looked at Google documentation and Carlos' https://github.com/crlsndrsjmnz/MyFileProviderExample
    // for help implementing photo storage process
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHOOSE_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                Uri path = data.getData();
                Log.v("PATH", path.toString());
                mCurrentPhotoPath = path.toString();
                setPicture();
                addGalleryPic();
            }
        }
    }

    private void setPicture(){
        // get dimensions of View
        int targetWidth = mInventoryImageButton.getWidth();
        int targetHeight = mInventoryImageButton.getHeight();

        // get dimensions of bitmap
        BitmapFactory.Options bitMapOptions = new BitmapFactory.Options();
        bitMapOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bitMapOptions);
        int photoWidth = bitMapOptions.outWidth;
        int photoHeight = bitMapOptions.outHeight;

        // determine image scale down
        int scaleFactor = Math.min(photoWidth/targetWidth, photoHeight/targetHeight);

        // decode image file into a Bitmap sized to fill the view
        bitMapOptions.inJustDecodeBounds = false;
        bitMapOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bitMapOptions);
        mInventoryImageButton.setImageBitmap(bitmap);
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

        // Create checks for each required field. I do not require an image.
        boolean nameEntered = false;
        boolean quantityEntered = false;
        boolean priceEntered = false;

        // Read from input fields then trim empty garbage
        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();

        // Check if this is a new item and if all fields are blank
        if (mCurrentItemUri == null &&
                TextUtils.isEmpty(nameString) &&
                TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(priceString)) {
            // Jump out early. No need to run any more operations
            return;
        }

        ContentValues values = new ContentValues();

        // If something is in the name field, it can be saved
        if (!TextUtils.isEmpty(nameString)){
            nameEntered = true;
        }

        values.put(PlacedEntry.COLUMN_PRODUCT_NAME, nameString);

        // Let's set quantity to 0 by default, then check if the field is empty
        // If something is in the quantity field, it can be saved
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
            quantityEntered = true;
        }
        values.put(PlacedEntry.COLUMN_PRODUCT_QUANTITY, quantity);

        // Let's set a price of 0.00, then check if the field is empty
        // if something is in the price field, it can be saved
        double price = 0.00;
        if (!TextUtils.isEmpty(priceString)) {
            price = Double.parseDouble(priceString);
            priceEntered = true;
        }

        values.put(PlacedEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(PlacedEntry.COLUMN_PRODUCT_IMAGE, mCurrentPhotoPath);

        // If all the 3 fields name, quantity, and price have something in them, proceed
        if (nameEntered && quantityEntered && priceEntered) {

            // A save can now happen
            mSaveHasBeenPushed = true;

            // Check if this is an add or an update
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
        } else {
            //  If all the 3 fields name, quantity, and price do not have something and a save is attempted, show unsaved dialog
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

            Uri imageUri =  Uri.parse(image);
            mInventoryImageButton.setImageBitmap(getBitmapFromUri(imageUri));

            // Update UI
            mNameEditText.setText(name);
            mQuantityEditText.setText(String.valueOf(quantity));
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
        if (!mSaveHasBeenPushed) {
            // If so, let's pop up a dialog
            DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            };

            // Show an unsaved changes dialog
            showUnsavedChangesDialog(discardButtonClickListener);
        } else {
            super.onBackPressed();
        }

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

    /**
     * Use these 3 methods to handle the Up button save case.
     * I'd like to prevent users from exiting with unsaved changes.
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                if (!mItemHasChanged) {
                        NavUtils.navigateUpFromSameTask(ItemDetailActivity.this);
                        return true;
                }
                if (!mSaveHasBeenPushed){
                    // If so, let's pop up a dialog
                    DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    };

                    // Show an unsaved changes dialog
                    showUnsavedChangesDialog(discardButtonClickListener);
                    return true;
                }

        }
        return super.onOptionsItemSelected(menuItem);
    }
}
