package pl.com.andrzejgrzyb.shoppinglist.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Andrzej on 24.06.2016.
 */
public class DbDummyData {

    static final long TEST_DATE = 1419033600L;  // December 20th, 2014

    public static long insertDummyUser(Context mContext, String login, String name) {

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

    public static long insertDummyShoppingList(Context mContext, String name, String description, long ownerId) {

        // Get reference to writable DB
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create ContentValues of what needs to be inserted
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_ID_CLOUD, 0);
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_NAME, name);
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_DESCRIPTION, description);
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_OWNER_ID, ownerId);
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_OWNER_ID_CLOUD, 0);
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_MODIFICATION_DATE, TEST_DATE);
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_MODIFIED_BY_ID, ownerId);
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_MODIFIED_BY_ID_CLOUD, 0);
        contentValues.put(DbContract.ShoppingListsEntry.COLUMN_HASHTAG, "xxx123xxx");


        // Insert ContentValues into the table and get row id back
        long shoppingListRowId;
        shoppingListRowId = db.insert(DbContract.ShoppingListsEntry.TABLE_NAME, null, contentValues);

        // Close DB
        db.close();

        return shoppingListRowId;
    }
    public static long insertDummyItem(Context mContext, String name, double quantity, String quantityUnit, long listId, long position, int checked,  long ownerId) {

        // Get reference to writable DB
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create ContentValues of what needs to be inserted
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.ItemsEntry.COLUMN_ID_CLOUD, 0);
        contentValues.put(DbContract.ItemsEntry.COLUMN_NAME, name);
        contentValues.put(DbContract.ItemsEntry.COLUMN_QUANTITY, quantity);
        contentValues.put(DbContract.ItemsEntry.COLUMN_QUANTITY_UNIT, quantityUnit);
        contentValues.put(DbContract.ItemsEntry.COLUMN_LIST_ID, listId);
        contentValues.put(DbContract.ItemsEntry.COLUMN_LIST_ID_CLOUD, 0);
        contentValues.put(DbContract.ItemsEntry.COLUMN_POSITION, position);
        contentValues.put(DbContract.ItemsEntry.COLUMN_CHECKED, checked);
        contentValues.put(DbContract.ItemsEntry.COLUMN_MODIFICATION_DATE, TEST_DATE);
        contentValues.put(DbContract.ItemsEntry.COLUMN_MODIFIED_BY_ID, ownerId);
        contentValues.put(DbContract.ItemsEntry.COLUMN_MODIFIED_BY_ID_CLOUD, 0);


        // Insert ContentValues into the table and get row id back
        long itemRowId;
        itemRowId = db.insert(DbContract.ItemsEntry.TABLE_NAME, null, contentValues);

        // Close DB
        db.close();

        return itemRowId;
    }
}
