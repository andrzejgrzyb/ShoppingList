package pl.com.andrzejgrzyb.shoppinglist.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

/**
 * Created by Andrzej on 21.06.2016.
 */
public class TestDb  extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();


    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(DbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    /*
        Students: Uncomment this test once you've written the code to create the Location
        table.  Note that you will have to have chosen the same column names that I did in
        my solution for this test to compile, so if you haven't yet done that, this is
        a good time to change your column names to match mine.

        Note that this only tests that the Location table has the correct columns, since we
        give you the code for the weather table.  This test does not look at the
     */

    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(DbContract.ItemsEntry.TABLE_NAME);
        tableNameHashSet.add(DbContract.ShoppingListsEntry.TABLE_NAME);
        tableNameHashSet.add(DbContract.UsersEntry.TABLE_NAME);

        mContext.deleteDatabase(DbHelper.DATABASE_NAME);
        SQLiteDatabase db = new DbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain all the entry tables
        assertTrue("Error: Your database was created without both the location entry and weather entry tables",
                tableNameHashSet.isEmpty());

        // *********** Items table  *****************************************************************
        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + DbContract.ItemsEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> itemsColumnHashSet = new HashSet<String>();
        itemsColumnHashSet.add(DbContract.ItemsEntry._ID);
        itemsColumnHashSet.add(DbContract.ItemsEntry.COLUMN_ID_CLOUD);
        itemsColumnHashSet.add(DbContract.ItemsEntry.COLUMN_NAME);
        itemsColumnHashSet.add(DbContract.ItemsEntry.COLUMN_QUANTITY);
        itemsColumnHashSet.add(DbContract.ItemsEntry.COLUMN_QUANTITY_UNIT);
        itemsColumnHashSet.add(DbContract.ItemsEntry.COLUMN_LIST_ID);
        itemsColumnHashSet.add(DbContract.ItemsEntry.COLUMN_LIST_ID_CLOUD);
        itemsColumnHashSet.add(DbContract.ItemsEntry.COLUMN_POSITION);
        itemsColumnHashSet.add(DbContract.ItemsEntry.COLUMN_CHECKED);
        itemsColumnHashSet.add(DbContract.ItemsEntry.COLUMN_MODIFICATION_DATE);
        itemsColumnHashSet.add(DbContract.ItemsEntry.COLUMN_MODIFIED_BY_ID);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            itemsColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                itemsColumnHashSet.isEmpty());

        // **********  ShoppingList table ***************************************************************
        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + DbContract.ShoppingListsEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> shoppingListColumnHashSet = new HashSet<String>();
        shoppingListColumnHashSet.add(DbContract.ShoppingListsEntry._ID);
        shoppingListColumnHashSet.add(DbContract.ShoppingListsEntry.COLUMN_ID_CLOUD);
        shoppingListColumnHashSet.add(DbContract.ShoppingListsEntry.COLUMN_NAME);
        shoppingListColumnHashSet.add(DbContract.ShoppingListsEntry.COLUMN_DESCRIPTION);
        shoppingListColumnHashSet.add(DbContract.ShoppingListsEntry.COLUMN_OWNER_ID);
        shoppingListColumnHashSet.add(DbContract.ShoppingListsEntry.COLUMN_MODIFICATION_DATE);
        shoppingListColumnHashSet.add(DbContract.ShoppingListsEntry.COLUMN_MODIFIED_BY_ID);
        shoppingListColumnHashSet.add(DbContract.ShoppingListsEntry.COLUMN_HASHTAG);

        columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            shoppingListColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                shoppingListColumnHashSet.isEmpty());

        // **********  USERS table ***************************************************************
        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + DbContract.UsersEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> usersColumnHashSet = new HashSet<String>();
        usersColumnHashSet.add(DbContract.UsersEntry._ID);
        usersColumnHashSet.add(DbContract.UsersEntry.COLUMN_ID_CLOUD);
        usersColumnHashSet.add(DbContract.UsersEntry.COLUMN_LOGIN);
        usersColumnHashSet.add(DbContract.UsersEntry.COLUMN_NAME);

        columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            usersColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                usersColumnHashSet.isEmpty());


        db.close();
    }


    // Adds a user to UserTable and checks if data is being added and verifies added data
    public void testUsersTable() {
        // Get reference to writable database
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create ContentValues of what needs to be inserted
        ContentValues testValues = new ContentValues();
        testValues.put(DbContract.UsersEntry.COLUMN_ID_CLOUD, 0);
        testValues.put(DbContract.UsersEntry.COLUMN_LOGIN, "test_login");
        testValues.put(DbContract.UsersEntry.COLUMN_NAME, "andrzej");

        // Insert values into database and get a row ID back
        long userRowId;
        userRowId = db.insert(DbContract.UsersEntry.TABLE_NAME, null, testValues);

        // Verify if we got a row back
        assertTrue("Error: Don't have a row ID back.", userRowId != -1);

        // Query DB and receive a Cursor back
        Cursor cursor = db.query(
                DbContract.UsersEntry.TABLE_NAME,  // Table to query
                null,   // all columns
                null,   // Columns for the "where" clause
                null,   // Values for the "where" clause
                null,   // Columns to group by
                null,   // Columns to filter by row groups
                null   // sort order
        );

        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query
        assertTrue("Error: No Records returnd from Users query", cursor.moveToFirst());

        // Validate data in resulting cursor with textValues
        TestUtilities.validateCurrentRecord("Error: User Query Validation Failed.", cursor, testValues);

        // Move the cursor to demonstrate that there is only one record in the DB
        assertFalse("Error: More that one record returned for the User query", cursor.moveToNext());

        // Close Cursor na DB
        cursor.close();
        db.close();

        // Return the ID of the new User
       // return userRowId;
    }

    // Adds a ShoppingList to the Table and verifies if it was correctly added
    public void testShoppingListTable() {
        // Get reference to writable database
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create ContentValues of what needs to be inserted
        ContentValues testValues = new ContentValues();
        long testUser = 1;
        testValues.put(DbContract.ShoppingListsEntry.COLUMN_ID_CLOUD, 0);
        testValues.put(DbContract.ShoppingListsEntry.COLUMN_NAME, "Zakupy w Biedronce");
        testValues.put(DbContract.ShoppingListsEntry.COLUMN_DESCRIPTION, "Rzeczy, które trzeba kupić w Biedronce");
        testValues.put(DbContract.ShoppingListsEntry.COLUMN_OWNER_ID, testUser);
        testValues.put(DbContract.ShoppingListsEntry.COLUMN_OWNER_ID_CLOUD, 0);
        testValues.put(DbContract.ShoppingListsEntry.COLUMN_MODIFICATION_DATE, TestUtilities.TEST_DATE);
        testValues.put(DbContract.ShoppingListsEntry.COLUMN_MODIFIED_BY_ID, testUser);
        testValues.put(DbContract.ShoppingListsEntry.COLUMN_MODIFIED_BY_ID_CLOUD, 0);
        testValues.put(DbContract.ShoppingListsEntry.COLUMN_HASHTAG, "qwerty1123456");

        // Insert values into database and get a row ID back
        long shoppingListRowId;
        shoppingListRowId = db.insert(DbContract.ShoppingListsEntry.TABLE_NAME, null, testValues);

        // Verify if we got a row back
        assertTrue("Error: Don't have a row ID back.", shoppingListRowId != -1);

        // Query DB and receive a Cursor back
        Cursor cursor = db.query(
                DbContract.ShoppingListsEntry.TABLE_NAME,  // Table to query
                null,   // all columns
                null,   // Columns for the "where" clause
                null,   // Values for the "where" clause
                null,   // Columns to group by
                null,   // Columns to filter by row groups
                null   // sort order
        );

        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query
        assertTrue("Error: No Records returnd from Users query", cursor.moveToFirst());

        // Validate data in resulting cursor with textValues
        TestUtilities.validateCurrentRecord("Error: User Query Validation Failed.", cursor, testValues);

        // Move the cursor to demonstrate that there is only one record in the DB
        assertFalse("Error: More that one record returned for the User query", cursor.moveToNext());

        // Close Cursor na DB
        cursor.close();
        db.close();

   //     return shoppingListRowId;
    }

    // Adds a ShoppingList to the Table and verifies if it was correctly added
    public void testItemsTable() {
        // Get reference to writable database
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create ContentValues of what needs to be inserted
        ContentValues testValues = new ContentValues();
        long testUser = 1;
        long testShoppingList = 2;
        testValues.put(DbContract.ItemsEntry.COLUMN_ID_CLOUD, 0);
        testValues.put(DbContract.ItemsEntry.COLUMN_NAME, "mleko");
        testValues.put(DbContract.ItemsEntry.COLUMN_QUANTITY, 1.5);
        testValues.put(DbContract.ItemsEntry.COLUMN_QUANTITY_UNIT, "L");
        testValues.put(DbContract.ItemsEntry.COLUMN_LIST_ID, testShoppingList);
        testValues.put(DbContract.ItemsEntry.COLUMN_LIST_ID_CLOUD, 0);
        testValues.put(DbContract.ItemsEntry.COLUMN_POSITION, 1);
        testValues.put(DbContract.ItemsEntry.COLUMN_CHECKED, 0);
        testValues.put(DbContract.ItemsEntry.COLUMN_MODIFICATION_DATE, TestUtilities.TEST_DATE);
        testValues.put(DbContract.ItemsEntry.COLUMN_MODIFIED_BY_ID, testUser);
        testValues.put(DbContract.ItemsEntry.COLUMN_MODIFIED_BY_ID_CLOUD, 0);


        // Insert values into database and get a row ID back
        long itemRowId;
        itemRowId = db.insert(DbContract.ItemsEntry.TABLE_NAME, null, testValues);

        // Verify if we got a row back
        assertTrue("Error: Don't have a row ID back.", itemRowId != -1);

        // Query DB and receive a Cursor back
        Cursor cursor = db.query(
                DbContract.ItemsEntry.TABLE_NAME,  // Table to query
                null,   // all columns
                null,   // Columns for the "where" clause
                null,   // Values for the "where" clause
                null,   // Columns to group by
                null,   // Columns to filter by row groups
                null   // sort order
        );

        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query
        assertTrue("Error: No Records returned from Users query", cursor.moveToFirst());

        // Validate data in resulting cursor with textValues
        TestUtilities.validateCurrentRecord("Error: User Query Validation Failed.", cursor, testValues);

        // Move the cursor to demonstrate that there is only one record in the DB
        assertFalse("Error: More that one record returned for the User query", cursor.moveToNext());

        // Close Cursor na DB
        cursor.close();
        db.close();

       // return itemRowId;
    }
    /*
        Students:  Here is where you will build code to test that we can insert and query the
        location database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can uncomment out the "createNorthPoleLocationValues" function.  You can
        also make use of the ValidateCurrentRecord function from within TestUtilities.
    */
//    public void testLocationTable() {
//
//
//        insertLocation();
//
//
//
//
//    }
//
//    /*
//        Students:  Here is where you will build code to test that we can insert and query the
//        database.  We've done a lot of work for you.  You'll want to look in TestUtilities
//        where you can use the "createWeatherValues" function.  You can
//        also make use of the validateCurrentRecord function from within TestUtilities.
//     */
//    public void testWeatherTable() {
//        // First insert the location, and then use the locationRowId to insert
//        // the weather. Make sure to cover as many failure cases as you can.
//        long locationRowId = insertLocation();
//        // Instead of rewriting all of the code we've already written in testLocationTable
//        // we can move this code to insertLocation and then call insertLocation from both
//        // tests. Why move it? We need the code to return the ID of the inserted location
//        // and our testLocationTable can only return void because it's a test.
//
//        // First step: Get reference to writable database
//        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//        // Create ContentValues of what you want to insert
//        // (you can use the createWeatherValues TestUtilities function if you wish)
//        ContentValues testValues = new ContentValues();
//        testValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationRowId);
//        testValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, 1419033600L);
//        testValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, 1.1);
//        testValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, 1.2);
//        testValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, 1.3);
//        testValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, 75);
//        testValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, 65);
//        testValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
//        testValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, 5.5);
//        testValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, 321);
//        // Insert ContentValues into database and get a row ID back
//        long weatherRowId = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, testValues);
//        // Query the database and receive a Cursor back
//        Cursor cursor = db.query(
//                WeatherContract.WeatherEntry.TABLE_NAME,
//                null, // leaving "columns" null just returns all the columns.
//                null, // cols for "where" clause
//                null, // values for "where" clause
//                null, // columns to group by
//                null, // columns to filter by row groups
//                null  // sort order
//        );
//
//        // Move the cursor to a valid database row
//        assertTrue("Error: No records returnd from the location query", cursor.moveToFirst());
//        // Validate data in resulting Cursor with the original ContentValues
//        // (you can use the validateCurrentRecord function in TestUtilities to validate the
//        // query if you like)
//        TestUtilities.validateCurrentRecord("Error: Location Query Validation Failed", cursor, testValues);
//        // Finally, close the cursor and database
//        cursor.close();
//        db.close();
//    }

//
//    /*
//        Students: This is a helper method for the testWeatherTable quiz. You can move your
//        code from testLocationTable to here so that you can call this code from both
//        testWeatherTable and testLocationTable.
//     */
//    public long insertLocation() {
//        // First step: Get reference to writable database
//        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//        // Create ContentValues of what you want to insert
//        ContentValues testValues = new ContentValues();
//        testValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, getContext().getString(pl.com.andrzej.sunshine.app.R.string.pref_location_default));
//        testValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, "Warsaw");
//        testValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, 52.2298);
//        testValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, 21.0118);
//        // (you can use the createNorthPoleLocationValues if you wish)
//        //    testValues = TestUtilities.createNorthPoleLocationValues();
//        // Insert ContentValues into database and get a row ID back
//        long locationRowId;
//        locationRowId = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null, testValues);
//
//        assertTrue(locationRowId != -1);
//        // Query the database and receive a Cursor back
//        Cursor cursor = db.query(
//                WeatherContract.LocationEntry.TABLE_NAME, //table name
//                null,                                       //select all columns
//                null,                                   // column for the "where clause
//                null,                                   // column to group by
//                null,
//                null,
//                null);
//
//        // Move the cursor to a valid database row
//        assertTrue("Error: No records returnd from the location query", cursor.moveToFirst());
//        // Validate data in resulting Cursor with the original ContentValues
//        // (you can use the validateCurrentRecord function in TestUtilities to validate the
//        // query if you like)
//        TestUtilities.validateCurrentRecord("Error: Location Query Validation Failed", cursor, testValues);
//
//        // Move the cursor to demonstarate that there is only one record in the database
//        assertFalse("Error: More than one record returnd from location query", cursor.moveToNext());
//        // Finally, close the cursor and database
//        cursor.close();
//        db.close();
//
//        return locationRowId;
//    }
}
