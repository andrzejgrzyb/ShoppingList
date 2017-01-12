package pl.com.andrzejgrzyb.shoppinglist.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.math.RoundingMode;
import java.text.DateFormat;

import android.support.v7.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;

import pl.com.andrzejgrzyb.shoppinglist.R;
import pl.com.andrzejgrzyb.shoppinglist.googlesignin.GoogleConnection;

/**
 * Created by Andrzej on 24.06.2016.
 */
public class DbUtilities {
    private static final String TAG = "DbUtilities";
    private GoogleConnection googleConnection;
    public SQLiteDatabase db;
    private Context context;


    public DbUtilities(Context incomingContext, GoogleConnection incomingGoogleConnection) {
        googleConnection = incomingGoogleConnection;
        context = incomingContext;

        // Get reference to writable DB
        DbHelper dbHelper = new DbHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    public void closeDb() {
        db.close();
    }


    public long insertUser(String login, String name) {

        // Create ContentValues of what needs to be inserted
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.UsersEntry.COLUMN_ID_CLOUD, 0);
        contentValues.put(DbContract.UsersEntry.COLUMN_EMAIL, login);
        contentValues.put(DbContract.UsersEntry.COLUMN_NAME, name);

        // Insert ContentValues into the table and get row id back
        long userRowId;
        userRowId = db.insert(DbContract.UsersEntry.TABLE_NAME, null, contentValues);

        return userRowId;
    }

    public Cursor getAllShoppingLists() {
        Cursor cursor = db.query(DbContract.ShoppingListsEntry.TABLE_NAME,
                null,
                DbContract.ShoppingListsEntry.COLUMN_PERMITTED_USER_ID_CLOUD + "=?",
                new String[] {getCurrentUserIdCloud()},
                null,
                null,
                DbContract.ShoppingListsEntry.COLUMN_MODIFICATION_DATE + " DESC"      //sortBy
        );

        return cursor;
    }

    public Cursor getAllShoppingListsExceptOf(long excludedId) {
        Cursor cursor = db.query(DbContract.ShoppingListsEntry.TABLE_NAME,
                new String[] {DbContract.ShoppingListsEntry._ID, DbContract.ShoppingListsEntry.COLUMN_NAME},
                DbContract.ShoppingListsEntry._ID + "!=?",
                new String[] {String.valueOf(excludedId)},
                null,
                null,
                DbContract.ShoppingListsEntry.COLUMN_MODIFICATION_DATE + " DESC");

        return cursor;
    }

    public long insertShoppingList(long idCloud, String name, String description) {
        // Create ContentValues of what needs to be inserted
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_ID_CLOUD, idCloud);
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_NAME, name);
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_DESCRIPTION, description);
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_PERMITTED_USER_ID_CLOUD, getCurrentUserIdCloud());
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_OWNER_ID_CLOUD, getCurrentUserIdCloud());
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_MODIFICATION_DATE, getCurrentTime());
//        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_MODIFIED_BY_ID, getCurrentUserIdFromDB());
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_MODIFIED_BY_ID_CLOUD, getCurrentUserIdCloud());
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_HASHTAG, 0);


        // Insert ContentValues into the table and get row id back
        long shoppingListRowId;
        shoppingListRowId = db.insert(DbContract.ShoppingListsEntry.TABLE_NAME, null, contentValues);


        return shoppingListRowId;
    }

    public int updateShoppingList(long id, long idCloud, String name, String description) {
        // Form query
        String where = DbContract.ShoppingListsEntry._ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(id)};

        ContentValues contentValues = new ContentValues();
        if (idCloud != -1)
            contentValues.put(DbContract.ShoppingListsEntry.COLUMN_ID_CLOUD, idCloud);
        if (name != null) contentValues.put(DbContract.ShoppingListsEntry.COLUMN_NAME, name);
        if (description != null)
            contentValues.put(DbContract.ShoppingListsEntry.COLUMN_DESCRIPTION, description);
//        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_PERMITTED_USER_ID_CLOUD, ownerId);
//        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_OWNER_ID_CLOUD, ownerIdCloud);
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_MODIFICATION_DATE, getCurrentTime());
//        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_MODIFIED_BY_ID, getCurrentUserIdFromDB());
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_MODIFIED_BY_ID_CLOUD, getCurrentUserIdCloud());
//        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_HASHTAG, hashTag);

        int result = db.update(DbContract.ShoppingListsEntry.TABLE_NAME, contentValues, where, whereArgs);

        return result;
    }

    public long insertItem(long idCloud, String name, double quantity,
                           String quantityUnit, long listId, long listIdCloud) {
        // Create ContentValues of what needs to be inserted
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.ItemsEntry.COLUMN_ID_CLOUD, idCloud);
        contentValues.put(DbContract.ItemsEntry.COLUMN_NAME, name);
        contentValues.put(DbContract.ItemsEntry.COLUMN_QUANTITY, quantity);
        contentValues.put(DbContract.ItemsEntry.COLUMN_QUANTITY_UNIT, quantityUnit);
        contentValues.put(DbContract.ItemsEntry.COLUMN_LIST_ID, listId);
        contentValues.put(DbContract.ItemsEntry.COLUMN_LIST_ID_CLOUD, listIdCloud);
        int position = 0;
        Cursor shoppingListCursor = getShoppingListItemsCursor(listId);
        if (shoppingListCursor.moveToLast()) {
            position = 1+ shoppingListCursor.getInt(shoppingListCursor.getColumnIndex(DbContract.ItemsEntry.COLUMN_POSITION));
        }
        contentValues.put(DbContract.ItemsEntry.COLUMN_POSITION, position);
        contentValues.put(DbContract.ItemsEntry.COLUMN_CHECKED, 0);
        contentValues.put(DbContract.ItemsEntry.COLUMN_MODIFICATION_DATE, getCurrentTime());
//        contentValues.put(DbContract.ItemsEntry.COLUMN_MODIFIED_BY_ID, getCurrentUserIdFromDB());
        contentValues.put(DbContract.ItemsEntry.COLUMN_MODIFIED_BY_ID_CLOUD, getCurrentUserIdCloud());

        // Insert ContentValues into the table and get row id back
        long itemRowId;
        itemRowId = db.insert(DbContract.ItemsEntry.TABLE_NAME, null, contentValues);

        // Add info about the modification to the Shopping List, new date and user id
        updateShoppingList(listId, -1, null, null);

        return itemRowId;
    }

    public int updateItem(long id, String name,
                          double quantity, String quantityUnit) {
        // Form query
        String where = DbContract.ItemsEntry._ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(id)};

        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.ItemsEntry.COLUMN_NAME, name);
        contentValues.put(DbContract.ItemsEntry.COLUMN_QUANTITY, quantity);
        contentValues.put(DbContract.ItemsEntry.COLUMN_QUANTITY_UNIT, quantityUnit);
        contentValues.put(DbContract.ItemsEntry.COLUMN_MODIFICATION_DATE, getCurrentTime());
//        contentValues.put(DbContract.ItemsEntry.COLUMN_MODIFIED_BY_ID, getCurrentUserIdFromDB());
        contentValues.put(DbContract.ItemsEntry.COLUMN_MODIFIED_BY_ID_CLOUD, getCurrentUserIdCloud());
        // Uncheck item
        contentValues.put(DbContract.ItemsEntry.COLUMN_CHECKED, 0);

        int result = db.update(DbContract.ItemsEntry.TABLE_NAME, contentValues, where, whereArgs);

        // add info about the modification to the Shopping List, new date and user id
        updateShoppingList(getShoppingListIdBasedOnItemId(id), -1, null, null);

        return result;
    }

    // Returns Shopping List ID of a given Item based on ItemID
    public long getShoppingListIdBasedOnItemId(long itemId) {
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

    public int itemCheckBoxChange(long id, boolean checked) {
         // Form query
        String where = DbContract.ItemsEntry._ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(id)};

        ContentValues contentValues = new ContentValues();
        int checkedInteger;
        if (checked) checkedInteger = 1;
        else checkedInteger = 0;
        contentValues.put(DbContract.ItemsEntry.COLUMN_CHECKED, checkedInteger);

        int result = db.update(DbContract.ItemsEntry.TABLE_NAME, contentValues, where, whereArgs);
        return result;
    }

    public boolean deleteItem(long id) {
        String whereClause = DbContract.ItemsEntry._ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(id)};

        // Get shopping list ID to update modification date. Do it before deleting item
        long shoppingListId =  getShoppingListIdBasedOnItemId(id);

        // if number of rows affected > 0 -> result = true
        boolean result =
                db.delete(DbContract.ItemsEntry.TABLE_NAME, whereClause, whereArgs) > 0;

        // add info about the modification to the Shopping List, new date and user id
        updateShoppingList(shoppingListId, -1, null, null);

        return result;
    }

    public boolean deleteShoppingList(long id) {

        String whereClause = DbContract.ShoppingListsEntry._ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        // if number of rows affected > 0 -> result = true
        boolean result =
                db.delete(DbContract.ShoppingListsEntry.TABLE_NAME, whereClause, whereArgs) > 0;

        return result;
    }

    public Cursor getShoppingListItemsCursor(long shoppingListId) {
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

    public Cursor getShoppingListCursor(long shoppingListId) {
        Cursor cursor = db.query(DbContract.ShoppingListsEntry.TABLE_NAME,
                null, // columns
                DbContract.ShoppingListsEntry._ID + " = ?",
                new String[]{Long.toString(shoppingListId)},
                null,
                null,
                null);
        return cursor;
    }

    public long getCurrentUserIdFromDB() {
        return 1;
    }

    public String getCurrentUserIdCloud() {
        String userIdCloud;
        if (googleConnection.isSignedIn()) {
            userIdCloud = googleConnection.getId();
        }
        else userIdCloud = "0";
        Log.d(TAG, "getCurrentUserIdCloud(): " + userIdCloud);
        return userIdCloud;
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

    public boolean deleteCheckedItems(long shoppingListId) {

        String whereClause = DbContract.ItemsEntry.COLUMN_LIST_ID + "=? AND "
                + DbContract.ItemsEntry.COLUMN_CHECKED + "=?";
        String[] whereArgs = new String[]{String.valueOf(shoppingListId), "1"};
        // if number of rows affected > 0 -> result = true
        boolean result =
                db.delete(DbContract.ItemsEntry.TABLE_NAME, whereClause, whereArgs) > 0;

        // add info about the modification to the Shopping List, new date and user id
        updateShoppingList(shoppingListId, -1, null, null);


        return result;
    }

    public int getShoppingListItemsCount(long shoppingListId) {
        // get cursor with all items
        Cursor cursor = getShoppingListItemsCursor(shoppingListId);
        return cursor.getCount();
    }

    public double getPercentageComplete(long shoppingListId) {
        // Get total count of items in the list
        int totalCount = getShoppingListItemsCount(shoppingListId);
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

    public void changeItemPosition(Cursor cursor, long itemId, int startPosition, int endPosition) {
        // Change moved item's position
        updateItemPosition(itemId, endPosition);
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
            updateItemPosition(cursor.getLong(cursor.getColumnIndex(DbContract.ItemsEntry._ID)), i + positionChange);
            Log.d("changeItemPosition", cursor.getString(cursor.getColumnIndex(DbContract.ItemsEntry.COLUMN_NAME)) + " " + String.valueOf(i+positionChange));
        }
        // Just write the modification date and user ID to the Shopping List
        updateShoppingList(getShoppingListIdBasedOnItemId(itemId), -1, null, null);
    }

    public int updateItemPosition(long id, int newPosition) {
        // Form query
        String where = DbContract.ItemsEntry._ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(id)};

        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.ItemsEntry.COLUMN_POSITION, newPosition);
        contentValues.put(DbContract.ItemsEntry.COLUMN_MODIFICATION_DATE, getCurrentTime());
//        contentValues.put(DbContract.ItemsEntry.COLUMN_MODIFIED_BY_ID, getCurrentUserIdFromDB());
        contentValues.put(DbContract.ItemsEntry.COLUMN_MODIFIED_BY_ID_CLOUD, getCurrentUserIdCloud());


        int result = db.update(DbContract.ItemsEntry.TABLE_NAME, contentValues, where, whereArgs);
        return result;
    }

    public int moveItemToAnotherShoppingList(long itemId, long newShoppingListId) {
        long oldShoppingListId = getShoppingListIdBasedOnItemId(itemId);
        long newShoppingListIdCloud = getShoppingListIdCloud(newShoppingListId);

        // Form query
        String where = DbContract.ItemsEntry._ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(itemId)};

        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.ItemsEntry.COLUMN_POSITION, getShoppingListItemsCount(newShoppingListId));
        contentValues.put(DbContract.ItemsEntry.COLUMN_LIST_ID, newShoppingListId);
        contentValues.put(DbContract.ItemsEntry.COLUMN_LIST_ID_CLOUD, newShoppingListIdCloud);
        contentValues.put(DbContract.ItemsEntry.COLUMN_MODIFICATION_DATE, getCurrentTime());
//        contentValues.put(DbContract.ItemsEntry.COLUMN_MODIFIED_BY_ID, getCurrentUserIdFromDB());
        contentValues.put(DbContract.ItemsEntry.COLUMN_MODIFIED_BY_ID_CLOUD, getCurrentUserIdCloud());

        int result = db.update(DbContract.ItemsEntry.TABLE_NAME, contentValues, where, whereArgs);

        // add info about the modification to Shopping Lists, new date and user id
        updateShoppingList(oldShoppingListId, -1, null, null);
        updateShoppingList(newShoppingListId, -1, null, null);

        return result;
    }

    public long getShoppingListIdCloud(long shoppingListId) {
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

    public String createShareString(Cursor cursor, String name, String description) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean includeTitle = sharedPreferences.getBoolean(context.getResources().getString(R.string.pref_key_share_list_title), true);
        boolean includeDesc = sharedPreferences.getBoolean(context.getResources().getString(R.string.pref_key_share_list_desc), true);
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
                shareString.append(getLocalisedQuantitUnit(
                        cursor.getString(cursor.getColumnIndex(DbContract.ItemsEntry.COLUMN_QUANTITY_UNIT))));
            } while (cursor.moveToNext());
        }
        return shareString.toString();
    }
    public String getLocalisedQuantitUnit(String quantityUnit) {
        int unitId = Arrays.asList(context.getResources().getStringArray(R.array.quantity_units_codes_array)).indexOf(quantityUnit);
        if (unitId != -1)
            return context.getResources().getStringArray(R.array.quantity_units_array)[unitId];
        else return "";

    }

}
