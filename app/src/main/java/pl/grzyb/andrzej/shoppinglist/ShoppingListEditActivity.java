package pl.grzyb.andrzej.shoppinglist;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import pl.grzyb.andrzej.shoppinglist.data.DbContract;
import pl.grzyb.andrzej.shoppinglist.data.DbHelper;
import pl.grzyb.andrzej.shoppinglist.data.DbUtilities;

public class ShoppingListEditActivity extends AppCompatActivity {
    public static final String EXTRA_SHOPPING_LIST_ID = "shoppingListId";
    // Flag to determine if it is an Editing Activity or Adding brand new List
    private boolean editFlag;
    private int shoppingListId;
    private EditText shoppingListNameEditText;
    private EditText shoppingListDescriptionEditText;
    private String oldName;
    private String oldDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list_edit);
        // Get references to EditText views
        shoppingListNameEditText = (EditText) findViewById(R.id.shoppingListNameEditText);
        shoppingListDescriptionEditText = (EditText) findViewById(R.id.shoppingListDescriptionEditText);


        // Get the intent
        Intent intent = getIntent();
        // Check if it came with List ID and set the flag
        editFlag = intent.hasExtra(EXTRA_SHOPPING_LIST_ID);

        if (editFlag) {
            // Get the list Id
            shoppingListId = intent.getIntExtra(EXTRA_SHOPPING_LIST_ID, 0);
            setTitle(R.string.title_activity_shopping_list_edit);

            // Get reference to readable DB
            DbHelper dbHelper = new DbHelper(this);
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            Cursor cursor = db.query(DbContract.ShoppingListsEntry.TABLE_NAME,
                    new String[]{DbContract.ShoppingListsEntry.COLUMN_NAME, DbContract.ShoppingListsEntry.COLUMN_DESCRIPTION}, // columns
                    DbContract.ShoppingListsEntry._ID + " = ?",
                    new String[]{String.valueOf(shoppingListId)},
                    null,
                    null,
                    null );
            if (cursor.moveToFirst()) {
                // Get Name and Description from DB
                oldName = cursor.getString(cursor.getColumnIndex(DbContract.ShoppingListsEntry.COLUMN_NAME));
                oldDescription = cursor.getString(cursor.getColumnIndex(DbContract.ShoppingListsEntry.COLUMN_DESCRIPTION));
                // Put Name and Description into EditText views
                shoppingListNameEditText.setText(oldName);
                shoppingListDescriptionEditText.setText(oldDescription);
                cursor.close();
            }
            else {
                Toast.makeText(this, R.string.errorNoRecord, Toast.LENGTH_SHORT);
            }

            db.close();
        }
        else {
            // We're adding a brand new Shopping List
            setTitle(R.string.title_activity_shopping_list_add);
        }

    }

    public void onClickAddShoppingList(View view) {
        // Get name from the EditText view and trim blank characters
        String name = shoppingListNameEditText.getText().toString().trim();

        if (name.length() < 3) {
            // Error: name should be at least 3 characters long
            Toast.makeText(ShoppingListEditActivity.this, R.string.errorNameTooShort, Toast.LENGTH_SHORT).show();
        }
        else {
            // Get description and insert row into DB
            String description = shoppingListDescriptionEditText.getText().toString().trim();
            if (!editFlag) {
                long rowId = DbUtilities.insertShoppingList(this,
                        0,                  // don't know IdCloud yet, this should be updated by the sync class
                        name,
                        description,
                        DbUtilities.getCurrentUserIdFromDB(this),
                        DbUtilities.getCurrentUserIdCloud(this),
                        DbUtilities.getCurrentTime(),
                        DbUtilities.getCurrentUserIdFromDB(this),
                        DbUtilities.getCurrentUserIdCloud(this),
                        0);
                if (rowId != -1) {
                    // Got a rowId back -> row added to DB, make a Toast and go back to MainActivity
                    Toast.makeText(ShoppingListEditActivity.this, R.string.notifyShoppingListAdded, Toast.LENGTH_SHORT).show();
                }
            }
            else if (!oldName.equals(name) || !oldDescription.equals(description)) {
                int result = DbUtilities.updateShoppingList(this,
                        shoppingListId,
                        0,
                        name,
                        description,
                        DbUtilities.getCurrentTime(),
                        DbUtilities.getCurrentUserIdFromDB(this),
                        DbUtilities.getCurrentUserIdCloud(this));
                if (result == 1) {
                    // 1 row updated, make a Toast and go back to MainActivity
                    Toast.makeText(ShoppingListEditActivity.this, R.string.notifyShoppingListUpdated, Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(ShoppingListEditActivity.this, "Nothing changed", Toast.LENGTH_SHORT).show();

            }

            Intent intent = new Intent(ShoppingListEditActivity.this, MainActivity.class);
            startActivity(intent);

        }
    }
}
