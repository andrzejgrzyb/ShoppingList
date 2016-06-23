package pl.grzyb.andrzej.shoppinglist.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;
import android.text.style.StrikethroughSpan;

/**
 * Created by Andrzej on 21.06.2016.
 */
public final class DbContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public DbContract() {
    }

    public static final String CONTENT_AUTHORITY = "pl.grzyb.andrzej.shoppinglist";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_ITEMS = "items";
    public static final String PATH_SHOPPING_LIST = "shoppingList";
//    public static final String PATH_BIND_LIST = "bindList";

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    /* Inner classes that define the table contents */
    /* Items in the shopping list, e.g. milk, cheese, ham, etc.  */
    public static final class ItemsEntry implements BaseColumns {
        public static final String TABLE_NAME = "items";
    //    public static final String COLUMN_ID = "id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_QUANTITY_UNIT = "quantityUnit";
        public static final String COLUMN_LIST_ID = "listId";
        public static final String COLUMN_POSITION = "position";
        public static final String COLUMN_CHECKED = "checked";
        public static final String COLUMN_MODIFICATION_DATE = "modificationDate";
        public static final String COLUMN_MODIFIED_BY = "modifiedBy"; // id of the user who did the last modification


        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_ITEMS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        public static final Uri buildItemUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /* Shopping Lists with owner, modifiers IDs, dates, etc. */
    public static final class ShoppingListEntry implements BaseColumns {
        public static final String TABLE_NAME = "shoppingList";
        //  public static final String COLUMN_ID = "id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_OWNER_ID = "ownerId";
        public static final String COLUMN_MODIFICATION_DATE = "modificationDate";
        public static final String COLUMN_MODIFIED_BY = "modifiedBy"; // id of the user who did the last modification

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SHOPPING_LIST).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SHOPPING_LIST;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SHOPPING_LIST;

        public static final Uri buildShoppingListUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

//    /* Table which bind Items to its Shopping List */
//    public static final class BindListEntry implements BaseColumns {
//        public static final String TABLE_NAME = "bindList";
//        public static final String COLUMN_ITEM_ID = "itemId";
//        public static final String COLUMN_LIST_ID = "listId";
//    }



}

