package pl.grzyb.andrzej.shoppinglist.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.text.DateFormat;

import android.text.format.DateUtils;

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
                new String[]{DbContract.ShoppingListsEntry._ID, DbContract.ShoppingListsEntry.COLUMN_NAME, DbContract.ShoppingListsEntry.COLUMN_MODIFICATION_DATE},
                null,
                null,
                null,
                null,
                DbContract.ShoppingListsEntry.COLUMN_MODIFICATION_DATE + " DESC"      //sortBy
                );

        return cursor;
    }

    public static long insertShoppingList(Context mContext,
                                          long idCloud, String name, String description, long ownerId,
                                          long ownerIdCloud, long modificationDate, long modifiedById,
                                          long modifiedByIdCloud, long hashTag) {

        // Get reference to writable DB
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create ContentValues of what needs to be inserted
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_ID_CLOUD, idCloud);
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_NAME, name);
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_DESCRIPTION, description);
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_OWNER_ID, ownerId);
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_OWNER_ID_CLOUD, ownerIdCloud);
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_MODIFICATION_DATE, modificationDate);
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_MODIFIED_BY_ID, modifiedById);
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_MODIFIED_BY_ID_CLOUD, modifiedByIdCloud);
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_HASHTAG, hashTag);


        // Insert ContentValues into the table and get row id back
        long shoppingListRowId;
        shoppingListRowId = db.insert(DbContract.ShoppingListsEntry.TABLE_NAME, null, contentValues);

        // Close DB
        db.close();

        return shoppingListRowId;
    }
    public static int updateShoppingList(Context mContext,
                                         long id, long idCloud, String name, String description,
                                         long modificationDate, long modifiedById, long modifiedByIdCloud) {
        // Get reference to writable DB
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Form query
        String where = DbContract.ShoppingListsEntry._ID + "=?";
        String[] whereArgs = new String[] {String.valueOf(id)};

        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_ID_CLOUD, idCloud);
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_NAME, name);
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_DESCRIPTION, description);
//        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_OWNER_ID, ownerId);
//        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_OWNER_ID_CLOUD, ownerIdCloud);
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_MODIFICATION_DATE, modificationDate);
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_MODIFIED_BY_ID, modifiedById);
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_MODIFIED_BY_ID_CLOUD, modifiedByIdCloud);
//        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_HASHTAG, hashTag);

        int result = db.update(DbContract.ShoppingListsEntry.TABLE_NAME, contentValues, where, whereArgs);
        // Close DB
        db.close();
        return result;
    }
    public static long insertItem(Context mContext,
                                  long idCloud, String name, double quantity,
                                  String quantityUnit, long listId, long listIdCloud,
                                  long position, int checked, long modificationDate,
                                  long modifiedById, long modifiedByIdCloud) {

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
        contentValues.put(DbContract.ItemsEntry.COLUMN_POSITION, position);
        contentValues.put(DbContract.ItemsEntry.COLUMN_CHECKED, checked);
        contentValues.put(DbContract.ItemsEntry.COLUMN_MODIFICATION_DATE, modificationDate);
        contentValues.put(DbContract.ItemsEntry.COLUMN_MODIFIED_BY_ID, modifiedById);
        contentValues.put(DbContract.ItemsEntry.COLUMN_MODIFIED_BY_ID_CLOUD, modifiedByIdCloud);


        // Insert ContentValues into the table and get row id back
        long itemRowId;
        itemRowId = db.insert(DbContract.ItemsEntry.TABLE_NAME, null, contentValues);

        // Close DB
        db.close();

        return itemRowId;
    }

    public static int updateItem(Context mContext,
                                 long id, long idCloud, String name,
                                 double quantity, String quantityUnit,
                                 long modificationDate, long modifiedById, long modifiedByIdCloud) {
        // Get reference to writable DB
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Form query
        String where = DbContract.ItemsEntry._ID + "=?";
        String[] whereArgs = new String[] {String.valueOf(id)};

        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.ItemsEntry.COLUMN_ID_CLOUD, idCloud);
        contentValues.put(DbContract.ItemsEntry.COLUMN_NAME, name);
        contentValues.put(DbContract.ItemsEntry.COLUMN_QUANTITY, quantity);
        contentValues.put(DbContract.ItemsEntry.COLUMN_QUANTITY_UNIT, quantityUnit);
        contentValues.put(DbContract.ItemsEntry.COLUMN_MODIFICATION_DATE, modificationDate);
        contentValues.put(DbContract.ItemsEntry.COLUMN_MODIFIED_BY_ID, modifiedById);
        contentValues.put(DbContract.ItemsEntry.COLUMN_MODIFIED_BY_ID_CLOUD, modifiedByIdCloud);

        int result = db.update(DbContract.ItemsEntry.TABLE_NAME, contentValues, where, whereArgs);
        // Close DB
        db.close();
        return result;
    }

    public static boolean deleteItem(Context mContext, long id) {
        // Get reference to writable DB
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String whereClause = DbContract.ItemsEntry._ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        // if number of rows affected > 0 -> result = true
        boolean result =
                db.delete(DbContract.ItemsEntry.TABLE_NAME, whereClause, whereArgs) > 0;
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
        db.close();
        return result;
    }

    public static Cursor getShoppingListItemsCursor(SQLiteDatabase db, long shoppingListId) {
        Cursor cursor = db.query(DbContract.ItemsEntry.TABLE_NAME,
                null,
                DbContract.ItemsEntry.COLUMN_LIST_ID + " = ?",
                new String[] {Long.toString(shoppingListId)},
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
                null );
        return cursor;
    }

    public static long getCurrentUserIdFromDB(Context mContext) {
        return 1;
    }

    public static long getCurrentUserIdCloud(Context mContext) {
        return 0;
    }

    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }

    public static String formatDate(Context applicationContext, long milliesSinceEpoch) {
        long currentMilliesSinceEpoch = System.currentTimeMillis();
        Date inputDate = new Date(milliesSinceEpoch);
//return String.valueOf(dateInMillies);
        long difference = currentMilliesSinceEpoch - milliesSinceEpoch;
        if (difference < 60*1000) {
            return applicationContext.getResources().getString(R.string.text_less_than_minute_ago);
        }
        else if (difference < 60*60*24*7 * 1000) {
            String output = DateUtils.getRelativeTimeSpanString (milliesSinceEpoch, currentMilliesSinceEpoch, 0).toString();
            return output;
            //TODO: fix the issue that adapter doesn't update when time passes
        } else {
            return DateFormat.getDateInstance().format(inputDate);
        }
    }
}
