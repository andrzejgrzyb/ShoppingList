package pl.com.andrzejgrzyb.shoppinglist.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Andrzej on 21.06.2016.
 */
public class DbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "ShoppingList.db";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {

        //TODO: uzupełnić onCreate w DbHelper
        final String SQL_CREATE_ITEMS_TABLE = "CREATE TABLE " + DbContract.ItemsEntry.TABLE_NAME + " (" +
                // Why AutoIncrement here, and not above?
                // Unique keys will be auto-generated in either case.  But for weather
                // forecasting, it's reasonable to assume the user will want information
                // for a certain date and all dates *following*, so the forecast data
                // should be sorted accordingly.
                DbContract.ItemsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DbContract.ItemsEntry.COLUMN_ID_CLOUD   + " INTEGER, " +
                DbContract.ItemsEntry.COLUMN_NAME       + " TEXT NOT NULL, " +
                DbContract.ItemsEntry.COLUMN_QUANTITY   + " REAL NOT NULL, " +
                DbContract.ItemsEntry.COLUMN_QUANTITY_UNIT  + " TEXT, " +
                DbContract.ItemsEntry.COLUMN_LIST_ID        + " INTEGER NOT NULL, " +
                DbContract.ItemsEntry.COLUMN_LIST_ID_CLOUD  + " INTEGER NOT NULL, " +
                DbContract.ItemsEntry.COLUMN_POSITION       + " INTEGER NOT NULL, " +
                DbContract.ItemsEntry.COLUMN_CHECKED        + " INTEGER NOT NULL, " +
                DbContract.ItemsEntry.COLUMN_MODIFICATION_DATE  + " INTEGER NOT NULL, " +
//                DbContract.ItemsEntry.COLUMN_MODIFIED_BY_ID + " INTEGER NOT NULL, " +
                DbContract.ItemsEntry.COLUMN_MODIFIED_BY_ID_CLOUD + " TEXT NOT NULL, " +


                // Set up the ListId column as a foreign key to ShoppingList table.
                " FOREIGN KEY (" + DbContract.ItemsEntry.COLUMN_LIST_ID + ") REFERENCES " +
                DbContract.ShoppingListsEntry.TABLE_NAME + " (" + DbContract.ShoppingListsEntry._ID + ") " +
                ");";

        db.execSQL(SQL_CREATE_ITEMS_TABLE);

        final String SQL_CREATE_SHOPPING_LIST_TABLE = "CREATE TABLE " + DbContract.ShoppingListsEntry.TABLE_NAME + " (" +
                DbContract.ShoppingListsEntry._ID                + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DbContract.ShoppingListsEntry.COLUMN_ID_CLOUD    + " INTEGER NOT NULL, " +
                DbContract.ShoppingListsEntry.COLUMN_NAME        + " TEXT NOT NULL, " +
                DbContract.ShoppingListsEntry.COLUMN_DESCRIPTION + " TEXT, " +
                DbContract.ShoppingListsEntry.COLUMN_PERMITTED_USER_ID_CLOUD + " TEXT NOT NULL, " +
                DbContract.ShoppingListsEntry.COLUMN_OWNER_ID_CLOUD    + " TEXT NOT NULL, " +
                DbContract.ShoppingListsEntry.COLUMN_MODIFICATION_DATE   + " INTEGER NOT NULL, " +
//                DbContract.ShoppingListsEntry.COLUMN_MODIFIED_BY_ID + " INTEGER NOT NULL, " +
                DbContract.ShoppingListsEntry.COLUMN_MODIFIED_BY_ID_CLOUD + " TEXT NOT NULL, " +
                DbContract.ShoppingListsEntry.COLUMN_HASHTAG             + " TEXT NOT NULL " +

               // Set up the OwnerId column as a foreign key to Users table.
//                " FOREIGN KEY (" + DbContract.ShoppingListsEntry.COLUMN_PERMITTED_USER_ID_CLOUD + ") REFERENCES " +
//                DbContract.UsersEntry.TABLE_NAME + " (" + DbContract.UsersEntry._ID + "), " +

                // Set up the ModifiedBy column as a foreign key to Users table.
//               " FOREIGN KEY (" + DbContract.ShoppingListsEntry.COLUMN_MODIFIED_BY_ID + ") REFERENCES " +
//                DbContract.UsersEntry.TABLE_NAME + " (" + DbContract.UsersEntry._ID + ") " +
                ");";
        db.execSQL(SQL_CREATE_SHOPPING_LIST_TABLE);

        final String SQL_CREATE_USERS_TABLE = "CREATE TABLE " + DbContract.UsersEntry.TABLE_NAME + " (" +
                DbContract.UsersEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DbContract.UsersEntry.COLUMN_ID_CLOUD + " INTEGER NOT NULL, " +
                DbContract.UsersEntry.COLUMN_EMAIL + " TEXT NOT NULL, " +
                DbContract.UsersEntry.COLUMN_NAME     + " TEXT NOT NULL " +

                ");";
        db.execSQL(SQL_CREATE_USERS_TABLE);



   /*     // To assure the application have just one weather entry per day
        // per location, it's created a UNIQUE constraint with REPLACE strategy
        " UNIQUE (" + WeatherContract.WeatherEntry.COLUMN_DATE + ", " +
                WeatherContract.WeatherEntry.COLUMN_LOC_KEY + ") ON CONFLICT REPLACE);";*/
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database should be only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.ItemsEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.ShoppingListsEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.UsersEntry.TABLE_NAME);
//        db.execSQL("DROP TABLE IF EXISTS " + DbContract.BindListEntry.TABLE_NAME);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
