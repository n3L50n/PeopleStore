package com.node_coyote.placed.dataPackage;

/**
 * Created by node_coyote on 4/7/17.
 */

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contract for the inventory tracking app Placed
 */
public final class PlacedContract {

    // Empty constructor. Don't instantiate the contract class
    public PlacedContract() {
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
     * Append path to base content uri to dig into inventory table
     */
    public static final String PATH_INVENTORY = "inventory";

    /**
     * This inner class holds constant values for the inventory database table
     */
    public static final class PlacedEntry implements BaseColumns {

        /**
         * content uri to access inventory data in the provider
         **/
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        /**
         * MIME type for a list of items {@link #CONTENT_URI}
         */
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        /**
         * MIME type for a single item {@link #CONTENT_URI}
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        /**
         * name of database table for inventory items
         **/
        public static final String TABLE_NAME = "inventory";

        /**
         * Type: INTEGER
         * Unique identifier for an item (row) in the database
         */
        public static final String _ID = BaseColumns._ID;

        /**
         * Type: TEXT
         * The name of the product
         */
        public static final String COLUMN_PRODUCT_NAME = "name";

        /**
         * Type: INTEGER
         * The current quantity of the product
         */
        public static final String COLUMN_PRODUCT_QUANTITY = "quantity";

        /**
         * Type: REAL
         * The current price of the product
         */
        public static final String COLUMN_PRODUCT_PRICE = "price";

        /**
         * Type: TEXT
         * The image of the product
         */
        public static final String COLUMN_PRODUCT_IMAGE = "image";

    }
}
