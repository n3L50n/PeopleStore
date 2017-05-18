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
import android.support.v4.app.NavUtils;
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

import com.node_coyote.placed.dataPackage.PeopleStoreContract.PeopleStoreEntry;

import java.io.FileDescriptor;
import java.io.IOException;

/**
 * Created by node_coyote on 4/8/17.
 */

public class PeopleStoreDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the image directory opener intent
     */
    static final int CHOOSE_IMAGE_REQUEST = 0;

    /**
     * Identifier for item data loader
     **/
    private static final int EXISTING_CONTACT_LOADER = 42;

    /**
     * Content uri for an existing item in the inventory
     **/
    private Uri mCurrentItemUri;

    /**
     * EditText field for first name
     **/
    private EditText mFirstNameEditText;

    /**
     * EditText field for last name
     **/
    private EditText mLastNameEditText;

    /**
     * EditText field for zip
     **/
    private EditText mZipEditText;

    /**
     * EditText field for phone number
     **/
    private EditText mNumberEditText;

    /**
     * EditText field for birth date
     **/
    private EditText mBirthEditText;

    /**
     * A button to take a photo and an ImageView to show a chosen photo
     */
    private ImageButton mInventoryImageButton;

    /**
     * Variable to help saveContact method determine if fields have been filled out
     */
    private boolean mSaveHasBeenPushed = false;

    /**
     * Variable to store the path to a photo saved
     */
    String mCurrentPhotoPath;

    /**
     * Let's use a boolean to keep track of whether or not a user has edited a contact
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
        mFirstNameEditText = (EditText) findViewById(R.id.edit_contact_first_name_text_view);
        mLastNameEditText = (EditText) findViewById(R.id.edit_contact_last_name_text_view);
        mZipEditText = (EditText) findViewById(R.id.edit_zip_text_view);
        mNumberEditText = (EditText) findViewById(R.id.edit_number_text_view);
        mBirthEditText = (EditText) findViewById(R.id.edit_birth_text_view);
        mInventoryImageButton = (ImageButton) findViewById(R.id.product_image_view);

        // If there isn't an id, let's create a new item
        if (mCurrentItemUri == null) {
            setTitle(R.string.item_detail_activity_add_item);
            deleteButton.setVisibility(View.GONE);

        } else {
            setTitle(getString(R.string.item_detail_activity_edit_item));
            getLoaderManager().initLoader(EXISTING_CONTACT_LOADER, null, this);
        }


        mFirstNameEditText.setOnTouchListener(mOnTouchListener);
        mLastNameEditText.setOnTouchListener(mOnTouchListener);
        mZipEditText.setOnTouchListener(mOnTouchListener);
        mNumberEditText.setOnTouchListener(mOnTouchListener);
        mBirthEditText.setOnTouchListener(mOnTouchListener);

        Button saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveContact();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });


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
                mCurrentPhotoPath = path.toString();
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
     * Get user input from editor and save contact to database
     */
    private void saveContact() {

        // Create checks for each required field. I do not require an image.
        boolean firstNameEntered = false;
        boolean lastNameEntered = false;
        boolean zipEntered = false;
        boolean numberEntered = false;
        boolean birthEntered = false;

        // Read from input fields then trim empty garbage
        String firstNameString = mFirstNameEditText.getText().toString().trim();
        String lastNameString = mLastNameEditText.getText().toString().trim();
        String zipString = mZipEditText.getText().toString().trim();
        String numberString = mNumberEditText.getText().toString().trim();
        String birthString = mBirthEditText.getText().toString().trim();

        // Check if this is a new contact and if all fields are blank
        if (mCurrentItemUri == null &&
                TextUtils.isEmpty(firstNameString) &&
                TextUtils.isEmpty(lastNameString) &&
                TextUtils.isEmpty(zipString) &&
                TextUtils.isEmpty(numberString) &&
                TextUtils.isEmpty(birthString)) {
            // Jump out early. No need to run any more operations
            return;
        }

        ContentValues values = new ContentValues();

        // If something is in the first name field, it can be saved
        if (!TextUtils.isEmpty(firstNameString)) {
            firstNameEntered = true;
        }

        values.put(PeopleStoreEntry.COLUMN_FIRST_NAME, firstNameString);

        // If something is in the last name field, it can be saved
        if (!TextUtils.isEmpty(lastNameString)) {
            lastNameEntered = true;
        }

        values.put(PeopleStoreEntry.COLUMN_LAST_NAME, lastNameString);

        // Let's set zip to 10101 by default, then check if the field is empty
        // If something is in the zip field, it can be saved
        int zip = 10101;
        if (!TextUtils.isEmpty(zipString)) {
            zip = Integer.parseInt(zipString);
            zipEntered = true;
        }
        values.put(PeopleStoreEntry.COLUMN_ZIP, zipString);

        // If something is in the phone number field, it can be saved
        if (!TextUtils.isEmpty(numberString)) {
            numberEntered = true;
        }

        values.put(PeopleStoreEntry.COLUMN_PHONE_NUMBER, numberString);

        // If something is in the birth date field, it can be saved
        if (!TextUtils.isEmpty(birthString)) {
            birthEntered = true;
        }

        values.put(PeopleStoreEntry.COLUMN_BIRTH, birthString);


        values.put(PeopleStoreEntry.COLUMN_CONTACT_IMAGE, mCurrentPhotoPath);

        // If all the 5 fields first name, last name, zip, number, and birth have something in them, proceed
        if (firstNameEntered && lastNameEntered && zipEntered && numberEntered && birthEntered) {

            // A save can now happen
            mSaveHasBeenPushed = true;

            // Check if this is an add or an update
            if (mCurrentItemUri == null) {
                Uri newUri = getContentResolver().insert(PeopleStoreEntry.CONTENT_URI, values);

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
            //  If all the 5 fields first name, last name, zip, number, and birth do not have something and a save is attempted, show unsaved dialog
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
                PeopleStoreEntry._ID,
                PeopleStoreEntry.COLUMN_FIRST_NAME,
                PeopleStoreEntry.COLUMN_LAST_NAME,
                PeopleStoreEntry.COLUMN_ZIP,
                PeopleStoreEntry.COLUMN_PHONE_NUMBER,
                PeopleStoreEntry.COLUMN_BIRTH,
                PeopleStoreEntry.COLUMN_CONTACT_IMAGE
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
            int firstNameColumnIndex = cursor.getColumnIndex(PeopleStoreEntry.COLUMN_FIRST_NAME);
            int lastNameColumnIndex = cursor.getColumnIndex(PeopleStoreEntry.COLUMN_LAST_NAME);
            int zipColumnIndex = cursor.getColumnIndex(PeopleStoreEntry.COLUMN_ZIP);
            int numberColumnIndex = cursor.getColumnIndex(PeopleStoreEntry.COLUMN_PHONE_NUMBER);
            int birthColumnIndex = cursor.getColumnIndex(PeopleStoreEntry.COLUMN_BIRTH);
            int imageColumnIndex = cursor.getColumnIndex(PeopleStoreEntry.COLUMN_CONTACT_IMAGE);

            // Get the values from the cursor
            String firstName = cursor.getString(firstNameColumnIndex);
            String lastName = cursor.getString(lastNameColumnIndex);
            int zip = cursor.getInt(zipColumnIndex);
            String number = cursor.getString(numberColumnIndex);
            String birth = cursor.getString(birthColumnIndex);
            String image = cursor.getString(imageColumnIndex);

            if (image != null) {
                Uri imageUri = Uri.parse(image);
                mInventoryImageButton.setImageBitmap(getBitmapFromUri(imageUri));
            }

            // Update UI
            mFirstNameEditText.setText(firstName);
            mLastNameEditText.setText(lastName);
            mZipEditText.setText(String.valueOf(zip));
            mNumberEditText.setText(number);
            mBirthEditText.setText(String.valueOf(birth));

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Let's clear the loader if it's invalidated
        mFirstNameEditText.setText("");
        mLastNameEditText.setText("");
        mZipEditText.setText("");
        mNumberEditText.setText("");
        mBirthEditText.setText("");
        mCurrentPhotoPath = "";
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
     *
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
                    NavUtils.navigateUpFromSameTask(PeopleStoreDetailActivity.this);
                    return true;
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
                    return true;
                }

        }
        return super.onOptionsItemSelected(menuItem);
    }
}
