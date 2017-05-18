package com.node_coyote.placed.dataPackage;

/**
 * Created by node_coyote on 5/17/17.
 */

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contract for the contact storing app PeopleStore
 */
public final class PeopleStoreContract {

    // Empty constructor. Don't instantiate the contract class
    public PeopleStoreContract() {
    }

    /**
     * Content Authority for content provider
     */
    public static final String CONTENT_AUTHORITY = "com.node_coyote.placed";

    /**
     * Base uri is created for apps to contact the content provider
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Append path to base content uri to dig into contacts table
     */
    public static final String PATH_CONTACTS = "contacts";

    /**
     * This inner class holds constant values for the inventory database table
     */
    public static final class PeopleStoreEntry implements BaseColumns {

        /**
         * content uri to access contact data in the provider
         **/
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_CONTACTS);

        /**
         * MIME type for a list of items {@link #CONTENT_URI}
         */
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONTACTS;

        /**
         * MIME type for a single item {@link #CONTENT_URI}
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONTACTS;

        /**
         * name of database table for contacts
         **/
        public static final String TABLE_NAME = "inventory";

        /**
         * Type: INTEGER
         * Unique identifier for a contact (row) in the database
         */
        public static final String _ID = BaseColumns._ID;

        /**
         * Type: TEXT
         * The first name of the contact
         */
        public static final String COLUMN_FIRST_NAME = "first_name";

        /**
         * Type: TEXT
         * The last name of the contact
         */
        public static final String COLUMN_LAST_NAME = "last_name";

        /**
         * Type: INTEGER
         * The zip code of the contact
         */
        public static final String COLUMN_ZIP = "zip";

        /**
         * Type: TEXT
         * The phone number of the contact
         */
        public static final String COLUMN_PHONE_NUMBER = "number";

        /**
         * Type: TEXT
         * The date of birth of the contact
         */
        public static final String COLUMN_BIRTH = "birth";

        /**
         * Type: TEXT
         * The path to the contact image
         */
        public static final String COLUMN_CONTACT_IMAGE = "image";

    }
}
