package pl.grzyb.andrzej.shoppinglist.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.math.RoundingMode;
import java.text.DateFormat;

import android.support.v7.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;

import pl.grzyb.andrzej.shoppinglist.R;

/**
 * Created by Andrzej on 24.06.2016.
 */
public class DbUtilities {


    public static long insertUser(Context mContext, String login, String name) {

        // Get reference to writable DB
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create ContentValues of what needs to be inserted
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.UsersEntry.COLUMN_ID_CLOUD, 0);
        contentValues.put(DbContract.UsersEntry.COLUMN_LOGIN, login);
        contentValues.put(DbContract.UsersEntry.COLUMN_NAME, name);

        // Insert ContentValues into the table and get row id back
        long userRowId;
        userRowId = db.insert(DbContract.UsersEntry.TABLE_NAME, null, contentValues);

        // Close DB
        db.close();

        return userRowId;
    }

    public static Cursor getAllShoppingLists(SQLiteDatabase db) {
        Cursor cursor = db.query(DbContract.ShoppingListsEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                DbContract.ShoppingListsEntry.COLUMN_MODIFICATION_DATE + " DESC"      //sortBy
        );

        return cursor;
    }

    public static Cursor getAllShoppingListsExceptOf(SQLiteDatabase db, long excludedId) {
        Cursor cursor = db.query(DbContract.ShoppingListsEntry.TABLE_NAME,
                new String[] {DbContract.ShoppingListsEntry._ID, DbContract.ShoppingListsEntry.COLUMN_NAME},
                DbContract.ShoppingListsEntry._ID + "!=?",
                new String[] {String.valueOf(excludedId)},
                null,
                null,
                DbContract.ShoppingListsEntry.COLUMN_MODIFICATION_DATE + " DESC");

        return cursor;
    }

    public static long insertShoppingList(Context mContext,
                                          long idCloud, String name, String description) {

        // Get reference to writable DB
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create ContentValues of what needs to be inserted
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_ID_CLOUD, idCloud);
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_NAME, name);
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_DESCRIPTION, description);
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_OWNER_ID, getCurrentUserIdFromDB(db));
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_OWNER_ID_CLOUD, getCurrentUserIdCloud(db));
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_MODIFICATION_DATE, getCurrentTime());
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_MODIFIED_BY_ID, getCurrentUserIdFromDB(db));
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_MODIFIED_BY_ID_CLOUD, getCurrentUserIdCloud(db));
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_HASHTAG, 0);


        // Insert ContentValues into the table and get row id back
        long shoppingListRowId;
        shoppingListRowId = db.insert(DbContract.ShoppingListsEntry.TABLE_NAME, null, contentValues);

        // Close DB
        db.close();

        return shoppingListRowId;
    }

    public static int updateShoppingList(Context mContext,
                                         long id, long idCloud, String name, String description) {
        // Get reference to writable DB
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Form query
        String where = DbContract.ShoppingListsEntry._ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(id)};

        ContentValues contentValues = new ContentValues();
        if (idCloud != -1)
            contentValues.put(DbContract.ShoppingListsEntry.COLUMN_ID_CLOUD, idCloud);
        if (name != null) contentValues.put(DbContract.ShoppingListsEntry.COLUMN_NAME, name);
        if (description != null)
            contentValues.put(DbContract.ShoppingListsEntry.COLUMN_DESCRIPTION, description);
//        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_OWNER_ID, ownerId);
//        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_OWNER_ID_CLOUD, ownerIdCloud);
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_MODIFICATION_DATE, getCurrentTime());
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_MODIFIED_BY_ID, getCurrentUserIdFromDB(db));
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_MODIFIED_BY_ID_CLOUD, getCurrentUserIdCloud(db));
//        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_HASHTAG, hashTag);

        int result = db.update(DbContract.ShoppingListsEntry.TABLE_NAME, contentValues, where, whereArgs);
        // Close DB
        db.close();
        return result;
    }

    public static long insertItem(Context mContext,
                                  long idCloud, String name, double quantity,
                                  String quantityUnit, long listId, long listIdCloud) {

        // Get reference to writable DB
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create ContentValues of what needs to be inserted
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.ItemsEntry.COLUMN_ID_CLOUD, idCloud);
        contentValues.put(DbContract.ItemsEntry.COLUMN_NAME, name);
        contentValues.put(DbContract.ItemsEntry.COLUMN_QUANTITY, quantity);
        contentValues.put(DbContract.ItemsEntry.COLUMN_QUANTITY_UNIT, quantityUnit);
        contentValues.put(DbContract.ItemsEntry.COLUMN_LIST_ID, listId);
        contentValues.put(DbContract.ItemsEntry.COLUMN_LIST_ID_CLOUD, listIdCloud);
        contentValues.put(DbContract.ItemsEntry.COLUMN_POSITION, getShoppingListItemsCount(db, listId));
        contentValues.put(DbContract.ItemsEntry.COLUMN_CHECKED, 0);
        contentValues.put(DbContract.ItemsEntry.COLUMN_MODIFICATION_DATE, getCurrentTime());
        contentValues.put(DbContract.ItemsEntry.COLUMN_MODIFIED_BY_ID, getCurrentUserIdFromDB(db));
        contentValues.put(DbContract.ItemsEntry.COLUMN_MODIFIED_BY_ID_CLOUD, getCurrentUserIdCloud(db));

        // Insert ContentValues into the table and get row id back
        long itemRowId;
        itemRowId = db.insert(DbContract.ItemsEntry.TABLE_NAME, null, contentValues);

        // Add info about the modification to the Shopping List, new date and user id
        updateShoppingList(mContext, listId, -1, null, null);

        // Close DB
        db.close();
        return itemRowId;
    }

    public static int updateItem(Context mContext,
                                 long id, String name,
                                 double quantity, String quantityUnit) {
        // Get reference to writable DB
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Form query
        String where = DbContract.ItemsEntry._ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(id)};

        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.ItemsEntry.COLUMN_NAME, name);
        contentValues.put(DbContract.ItemsEntry.COLUMN_QUANTITY, quantity);
        contentValues.put(DbContract.ItemsEntry.COLUMN_QUANTITY_UNIT, quantityUnit);
        contentValues.put(DbContract.ItemsEntry.COLUMN_MODIFICATION_DATE, getCurrentTime());
        contentValues.put(DbContract.ItemsEntry.COLUMN_MODIFIED_BY_ID, getCurrentUserIdFromDB(db));
        contentValues.put(DbContract.ItemsEntry.COLUMN_MODIFIED_BY_ID_CLOUD, getCurrentUserIdCloud(db));
        // Uncheck item
        contentValues.put(DbContract.ItemsEntry.COLUMN_CHECKED, 0);

        int result = db.update(DbContract.ItemsEntry.TABLE_NAME, contentValues, where, whereArgs);

        // add info about the modification to the Shopping List, new date and user id
        updateShoppingList(mContext, getShoppingListIdBasedOnItemId(db, id), -1, null, null);

        // Close DB
        db.close();
        return result;
    }

    // Returns Shopping List ID of a given Item based on ItemID
    public static long getShoppingListIdBasedOnItemId(SQLiteDatabase db, long itemId) {
        // Form query
        String where = DbContract.ItemsEntry._ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(itemId)};

        Cursor cursor = db.query(DbContract.ItemsEntry.TABLE_NAME,
                new String[]{DbContract.ItemsEntry.COLUMN_LIST_ID},
                where,
                whereArgs,
                null,
                null,
                null);

        long result;
        if (cursor.moveToFirst()) {
            result = cursor.getLong(cursor.getColumnIndex(DbContract.ItemsEntry.COLUMN_LIST_ID));
        } else result = -1;
        cursor.close();
        return result;
    }

    public static int itemCheckBoxChange(Context mContext, long id, boolean checked) {
        // Get reference to writable DB
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Form query
        String where = DbContract.ItemsEntry._ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(id)};

        ContentValues contentValues = new ContentValues();
        int checkedInteger;
        if (checked) checkedInteger = 1;
        else checkedInteger = 0;
        contentValues.put(DbContract.ItemsEntry.COLUMN_CHECKED, checkedInteger);

        int result = db.update(DbContract.ItemsEntry.TABLE_NAME, contentValues, where, whereArgs);
        db.close();
        return result;
    }

    public static boolean deleteItem(Context mContext, long id) {
        // Get reference to writable DB
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String whereClause = DbContract.ItemsEntry._ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(id)};

        // Get shopping list ID to update modification date. Do it before deleting item
        long shoppingListId =  getShoppingListIdBasedOnItemId(db, id);

        // if number of rows affected > 0 -> result = true
        boolean result =
                db.delete(DbContract.ItemsEntry.TABLE_NAME, whereClause, whereArgs) > 0;

        // add info about the modification to the Shopping List, new date and user id
        updateShoppingList(mContext, shoppingListId, -1, null, null);

        //Close DB
        db.close();
        return result;
    }

    public static boolean deleteShoppingList(Context mContext, long id) {
        // Get reference to writable DB
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String whereClause = DbContract.ShoppingListsEntry._ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        // if number of rows affected > 0 -> result = true
        boolean result =
                db.delete(DbContract.ShoppingListsEntry.TABLE_NAME, whereClause, whereArgs) > 0;

        // Close DB
        db.close();
        return result;
    }

    public static Cursor getShoppingListItemsCursor(SQLiteDatabase db, long shoppingListId) {
        Cursor cursor = db.query(DbContract.ItemsEntry.TABLE_NAME,
                null,
                DbContract.ItemsEntry.COLUMN_LIST_ID + " = ?",
                new String[]{Long.toString(shoppingListId)},
                null,
                null,
                DbContract.ItemsEntry.COLUMN_POSITION,
                null);
        return cursor;
    }

    public static Cursor getShoppingListCursor(SQLiteDatabase db, long shoppingListId) {
        Cursor cursor = db.query(DbContract.ShoppingListsEntry.TABLE_NAME,
                null, // columns
                DbContract.ShoppingListsEntry._ID + " = ?",
                new String[]{Long.toString(shoppingListId)},
                null,
                null,
                null);
        return cursor;
    }

    public static long getCurrentUserIdFromDB(SQLiteDatabase db) {
        return 1;
    }

    public static long getCurrentUserIdCloud(SQLiteDatabase db) {
        return 0;
    }

    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }

    // returns a string with formatted date "x minutes ago"
    public static String formatDate(Context applicationContext, long milliesSinceEpoch) {
        long currentMilliesSinceEpoch = System.currentTimeMillis();
        Date inputDate = new Date(milliesSinceEpoch);
        long difference = currentMilliesSinceEpoch - milliesSinceEpoch;
        // avoid returning x seconds, write "just now" instead
        if (difference < 60 * 1000) {
            return applicationContext.getResources().getString(R.string.text_less_than_minute_ago);
        }
        // if more than a minute but not more than a week passed since update,
        // write "x minutes ago", "x days ago"
        else if (difference < 60 * 60 * 24 * 7 * 1000) {
            String output = DateUtils.getRelativeTimeSpanString(milliesSinceEpoch, currentMilliesSinceEpoch, 0).toString();
            return output;
        } else {
            // if more than a week, just return the date
            return DateFormat.getDateInstance().format(inputDate);
        }
    }

    public static String formatQuantity(double quantity) {
       /*  return a String with formatted quantity
        * no decimal point if quantity is an integer
        * add decimal point if quantity is double */

        DecimalFormat df = new DecimalFormat("#.###");
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(quantity).replace(',','.');
    }

    public static boolean deleteCheckedItems(Context mContext, long shoppingListId) {
        // Get reference to writable DB
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String whereClause = DbContract.ItemsEntry.COLUMN_LIST_ID + "=? AND "
                + DbContract.ItemsEntry.COLUMN_CHECKED + "=?";
        String[] whereArgs = new String[]{String.valueOf(shoppingListId), "1"};
        // if number of rows affected > 0 -> result = true
        boolean result =
                db.delete(DbContract.ItemsEntry.TABLE_NAME, whereClause, whereArgs) > 0;

        // add info about the modification to the Shopping List, new date and user id
        updateShoppingList(mContext, shoppingListId, -1, null, null);

        // Close DB
        db.close();

        return result;
    }

    public static int getShoppingListItemsCount(SQLiteDatabase db, long shoppingListId) {
        // get cursor with all items
        Cursor cursor = getShoppingListItemsCursor(db, shoppingListId);
        return cursor.getCount();
    }

    public static double getPercentageComplete(SQLiteDatabase db, long shoppingListId) {
        // Get total count of items in the list
        int totalCount = getShoppingListItemsCount(db, shoppingListId);
        if (totalCount != 0) {
            // Query for a cursor with checked items
            Cursor cursor = db.query(DbContract.ItemsEntry.TABLE_NAME,
                    null,
                    DbContract.ItemsEntry.COLUMN_LIST_ID + "=? AND "
                            + DbContract.ItemsEntry.COLUMN_CHECKED + "=?",
                    new String[]{Long.toString(shoppingListId), "1"},
                    null,
                    null,
                    null,
                    null);
            // Count checked items
            int checkedCount = cursor.getCount();
            // Return value in percent (0-100%)
            return checkedCount * 100 / totalCount ;
        }
        else {
            return 0;
        }

    }

    public static void changeItemPosition(Context mContext, Cursor cursor, long itemId, int startPosition, int endPosition) {
        // Get reference to writable DB
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Change moved item's position
        updateItemPosition(db, itemId, endPosition);
        cursor.moveToPosition(startPosition);
        Log.d("changeItemPosition", cursor.getString(cursor.getColumnIndex(DbContract.ItemsEntry.COLUMN_NAME)) + " " + String.valueOf(endPosition));

        // Variables used in FOR loop depending on whether item went up or down
        int positionChange;  // +1 or -1
        int i_start;
        int i_end;

        // Change positions of items between start position and end position
        if (startPosition < endPosition) {
            // Moved down
            positionChange = -1;
            i_start = startPosition + 1;
            i_end = endPosition;
        } else {
            // Moded item up
            positionChange = 1;
            i_start = endPosition;
            i_end = startPosition - 1;
        }
        for (int i = i_start; i <= i_end; i++) {
            cursor.moveToPosition(i);
            updateItemPosition(db, cursor.getLong(cursor.getColumnIndex(DbContract.ItemsEntry._ID)), i + positionChange);
            Log.d("changeItemPosition", cursor.getString(cursor.getColumnIndex(DbContract.ItemsEntry.COLUMN_NAME)) + " " + String.valueOf(i+positionChange));
        }
        // Just write the modification date and user ID to the Shopping List
        updateShoppingList(mContext, getShoppingListIdBasedOnItemId(db, itemId), -1, null, null);
        // Close DB
        db.close();
    }

    public static int updateItemPosition(SQLiteDatabase db,
                                          long id, int newPosition) {
        // Form query
        String where = DbContract.ItemsEntry._ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(id)};

        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.ItemsEntry.COLUMN_POSITION, newPosition);
        contentValues.put(DbContract.ItemsEntry.COLUMN_MODIFICATION_DATE, getCurrentTime());
        contentValues.put(DbContract.ItemsEntry.COLUMN_MODIFIED_BY_ID, getCurrentUserIdFromDB(db));
        contentValues.put(DbContract.ItemsEntry.COLUMN_MODIFIED_BY_ID_CLOUD, getCurrentUserIdCloud(db));


        int result = db.update(DbContract.ItemsEntry.TABLE_NAME, contentValues, where, whereArgs);
        return result;
    }

    public static int moveItemToAnotherShoppingList(Context mContext, long itemId, long newShoppingListId) {
        // Get reference to writable DB
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long oldShoppingListId = getShoppingListIdBasedOnItemId(db, itemId);
        long newShoppingListIdCloud = getShoppingListIdCloud(db, newShoppingListId);

        // Form query
        String where = DbContract.ItemsEntry._ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(itemId)};

        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.ItemsEntry.COLUMN_POSITION, getShoppingListItemsCount(db, newShoppingListId));
        contentValues.put(DbContract.ItemsEntry.COLUMN_LIST_ID, newShoppingListId);
        contentValues.put(DbContract.ItemsEntry.COLUMN_LIST_ID_CLOUD, newShoppingListIdCloud);
        contentValues.put(DbContract.ItemsEntry.COLUMN_MODIFICATION_DATE, getCurrentTime());
        contentValues.put(DbContract.ItemsEntry.COLUMN_MODIFIED_BY_ID, getCurrentUserIdFromDB(db));
        contentValues.put(DbContract.ItemsEntry.COLUMN_MODIFIED_BY_ID_CLOUD, getCurrentUserIdCloud(db));

        int result = db.update(DbContract.ItemsEntry.TABLE_NAME, contentValues, where, whereArgs);

        // add info about the modification to Shopping Lists, new date and user id
        updateShoppingList(mContext, oldShoppingListId, -1, null, null);
        updateShoppingList(mContext, newShoppingListId, -1, null, null);
        // Close DB
        db.close();

        return result;
    }

    public static long getShoppingListIdCloud(SQLiteDatabase db, long shoppingListId) {
        // Form query
        String where = DbContract.ShoppingListsEntry._ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(shoppingListId)};

        Cursor cursor = db.query(DbContract.ShoppingListsEntry.TABLE_NAME,
                new String[]{DbContract.ShoppingListsEntry.COLUMN_ID_CLOUD},
                where,
                whereArgs,
                null,
                null,
                null);
        long idCloud = 0;
        if (cursor.moveToFirst()) {
            idCloud = cursor.getLong(cursor.getColumnIndex(DbContract.ShoppingListsEntry.COLUMN_ID_CLOUD));
        }
        cursor.close();
        return idCloud;
    }

    public static Intent createShareIntent(String shareString) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                shareString);
        return shareIntent;
    }

    public static String createShareString(Context mContext, Cursor cursor, String name, String description) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean includeTitle = sharedPreferences.getBoolean(mContext.getResources().getString(R.string.pref_key_share_list_title), true);
        boolean includeDesc = sharedPreferences.getBoolean(mContext.getResources().getString(R.string.pref_key_share_list_desc), true);
        StringBuilder shareString = new StringBuilder();
        if (cursor.moveToFirst()) {
            if (includeTitle) {
                shareString.append(name);
                shareString.append("\n");
            }
            // if there's a description, add it
            if (!description.isEmpty() && includeDesc) {
                shareString.append(description);
                shareString.append("\n");
            }
            do {
                if (shareString.length() != 0) {
                    // add blank line if there something above the item list
                    shareString.append("\n");
                }
                shareString.append(cursor.getString(cursor.getColumnIndex(DbContract.ItemsEntry.COLUMN_NAME)));
                shareString.append(" ");
                shareString.append(DbUtilities.formatQuantity(
                        cursor.getDouble(cursor.getColumnIndex(DbContract.ItemsEntry.COLUMN_QUANTITY))));
                shareString.append(getLocalisedQuantitUnit(mContext,
                        cursor.getString(cursor.getColumnIndex(DbContract.ItemsEntry.COLUMN_QUANTITY_UNIT))));
            } while (cursor.moveToNext());
        }
        return shareString.toString();
    }
    public static String getLocalisedQuantitUnit(Context mContext, String quantityUnit) {
        int unitId = Arrays.asList(mContext.getResources().getStringArray(R.array.quantity_units_codes_array)).indexOf(quantityUnit);
        if (unitId != -1)
            return mContext.getResources().getStringArray(R.array.quantity_units_array)[unitId];
        else return "";

    }

}
