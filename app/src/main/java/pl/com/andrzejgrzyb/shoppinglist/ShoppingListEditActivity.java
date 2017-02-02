package pl.com.andrzejgrzyb.shoppinglist;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import pl.com.andrzejgrzyb.shoppinglist.data.DbContract;
import pl.com.andrzejgrzyb.shoppinglist.data.DbHelper;
import pl.com.andrzejgrzyb.shoppinglist.data.DbUtilities;
import pl.com.andrzejgrzyb.shoppinglist.googlesignin.GoogleConnection;

public class ShoppingListEditActivity extends AppCompatActivity {
    public static final String EXTRA_SHOPPING_LIST_ID = "shoppingListId";
    // Flag to determine if it is an Editing Activity or Adding brand new List
    private boolean editFlag;
    private long shoppingListId;
    private EditText shoppingListNameEditText;
    private EditText shoppingListDescriptionEditText;
    private String oldName;
    private String oldDescription;

    // Google Sign-in
    private GoogleConnection googleConnection;

    // Database
    private DbUtilities dbUtilities;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.onCreate(this);
        setContentView(R.layout.activity_shopping_list_edit);
        // Get toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Add back arrow to the toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        // Get references to EditText views
        shoppingListNameEditText = (EditText) findViewById(R.id.shoppingListNameEditText);
        shoppingListDescriptionEditText = (EditText) findViewById(R.id.shoppingListDescriptionEditText);

        // Create new connection to Google API
        googleConnection = new GoogleConnection(this);
        // Get reference to DB
        dbUtilities = new DbUtilities(getApplicationContext(), googleConnection);


        // Get the intent
        Intent intent = getIntent();
        // Check if it came with List ID and set the flag
        editFlag = intent.hasExtra(EXTRA_SHOPPING_LIST_ID);

        if (editFlag) {
            // Get the list Id
            shoppingListId = intent.getLongExtra(EXTRA_SHOPPING_LIST_ID, 0);
            setTitle(R.string.title_activity_shopping_list_edit);

            Cursor cursor = dbUtilities.getDb().query(DbContract.ShoppingListsEntry.TABLE_NAME,
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
                Toast.makeText(this, R.string.error_no_record, Toast.LENGTH_SHORT);
            }
        }
        else {
            // We're adding a brand new Shopping List
            setTitle(R.string.title_activity_shopping_list_add);
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        googleConnection.connectSilently();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Close database
        dbUtilities.closeDb();
    }

    public void onClickAddShoppingList(View view) {
        // Get name from the EditText view and trim blank characters
        String name = shoppingListNameEditText.getText().toString().trim();

        if (name.length() < 3) {
            // Error: name should be at least 3 characters long
            Toast.makeText(ShoppingListEditActivity.this, R.string.error_name_too_short, Toast.LENGTH_SHORT).show();
        }
        else {
            // Get description and insert row into DB
            String description = shoppingListDescriptionEditText.getText().toString().trim();
            if (!editFlag) {
                long rowId = dbUtilities.insertShoppingList(
                        0,                  // don't know IdCloud yet, this should be updated by the sync class
                        name,
                        description);
                if (rowId != -1) {
                    // Got a rowId back -> row added to DB, make a Toast and go back to MainActivity
                    Toast.makeText(ShoppingListEditActivity.this, R.string.notify_shopping_list_added, Toast.LENGTH_SHORT).show();
                }
            }
            else if (!oldName.equals(name) || !oldDescription.equals(description)) {
                int result = dbUtilities.updateShoppingList(
                        shoppingListId,
                        0,
                        name,
                        description);
                if (result == 1) {
                    // 1 row updated, make a Toast and go back to MainActivity
                    Toast.makeText(ShoppingListEditActivity.this, R.string.notify_shopping_list_updated, Toast.LENGTH_SHORT).show();
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
