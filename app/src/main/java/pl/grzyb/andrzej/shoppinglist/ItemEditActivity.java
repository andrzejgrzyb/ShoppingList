package pl.grzyb.andrzej.shoppinglist;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import pl.grzyb.andrzej.shoppinglist.data.DbContract;
import pl.grzyb.andrzej.shoppinglist.data.DbHelper;
import pl.grzyb.andrzej.shoppinglist.data.DbUtilities;

public class ItemEditActivity extends AppCompatActivity {
    public static final String EXTRA_SHOPPING_LIST_ID = "shoppingListId";
    public static final String EXTRA_SHOPPING_LIST_ID_CLOUD = "shoppingListIdCloud";
    public static final String EXTRA_ITEM_ID = "itemId";
    // Flag to determine if it is an Editing Activity or Adding brand new Item
    private boolean editFlag;
    private long shoppingListId;
    private long shoppingListIdCloud;
    private long itemId;
    private long itemIdCloud;
    private EditText itemNameEditText;
    private EditText itemQuantityEditText;
    private String oldName;
    private double oldQuantity;
    private String oldQuantityUnit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_edit);
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
        itemNameEditText = (EditText) findViewById(R.id.item_name_edit_text);
        itemQuantityEditText = (EditText) findViewById(R.id.item_quantity_edit_text);

        // Get the intent
        Intent intent = getIntent();
        // Check if it came with List ID and set the flag
        editFlag = intent.hasExtra(EXTRA_ITEM_ID);

        // Get List ID
        shoppingListId = intent.getLongExtra(EXTRA_SHOPPING_LIST_ID, 0);
        shoppingListIdCloud = intent.getLongExtra(EXTRA_SHOPPING_LIST_ID_CLOUD, 0);

        if (editFlag) {
            // Get the list Id
            itemId = intent.getLongExtra(EXTRA_ITEM_ID, 0);
            setTitle(R.string.title_activity_item_edit);

            // Get reference to readable DB
            DbHelper dbHelper = new DbHelper(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            Cursor cursor = db.query(DbContract.ItemsEntry.TABLE_NAME,
                    null, // columns
                    DbContract.ItemsEntry._ID + " = ?",
                    new String[]{String.valueOf(itemId)},
                    null,
                    null,
                    null );
            if (cursor.moveToFirst()) {
                // Get Name and Description from DB
                shoppingListId = cursor.getLong(cursor.getColumnIndex(DbContract.ItemsEntry.COLUMN_LIST_ID));
                shoppingListIdCloud = cursor.getLong(cursor.getColumnIndex(DbContract.ItemsEntry.COLUMN_LIST_ID_CLOUD));
                itemIdCloud = cursor.getLong(cursor.getColumnIndex(DbContract.ItemsEntry.COLUMN_ID_CLOUD));
                oldName = cursor.getString(cursor.getColumnIndex(DbContract.ItemsEntry.COLUMN_NAME));
                oldQuantity = cursor.getDouble(cursor.getColumnIndex(DbContract.ItemsEntry.COLUMN_QUANTITY));
                oldQuantityUnit = cursor.getString(cursor.getColumnIndex(DbContract.ItemsEntry.COLUMN_QUANTITY_UNIT));

                // Put Name and Description into EditText views
                itemNameEditText.setText(oldName);
                itemQuantityEditText.setText(String.valueOf(oldQuantity));
                // itemQuantityUnit ???
                cursor.close();
            }
            else {
                Toast.makeText(this, R.string.error_no_record, Toast.LENGTH_SHORT);
            }

            db.close();
        }
        else {
            // We're adding a brand new Shopping List
            setTitle(R.string.title_activity_item_add);
        }
    }

    public void onClickSaveButton(View view) {
        // Get name and quantity from the EditText views and trim blank characters
        String name = itemNameEditText.getText().toString().trim();
        double quantity = Long.valueOf(itemQuantityEditText.getText().toString());
        String quantityUnit = "szt."; //TODO: zrobić pobieranie Unitu


        if (name.length() < 3) {
            // Error: name should be at least 3 characters long
            Toast.makeText(ItemEditActivity.this, R.string.error_name_too_short, Toast.LENGTH_SHORT).show();
        }
        else {
            // Insert row into DB
            if (!editFlag) {
                long rowId = DbUtilities.insertItem(this,
                        0,                  // don't know IdCloud yet, this should be updated by the sync class
                        name,
                        quantity,
                        quantityUnit,
                        shoppingListId,
                        shoppingListIdCloud,
                        0,  // checked
                        0,  // position
                        DbUtilities.getCurrentTime(),
                        DbUtilities.getCurrentUserIdFromDB(this),
                        DbUtilities.getCurrentUserIdCloud(this));
                if (rowId != -1) {
                    // Got a rowId back -> row added to DB, make a Toast and go back to MainActivity
                    Toast.makeText(ItemEditActivity.this, R.string.notify_item_added, Toast.LENGTH_SHORT).show();
                }
            }
            else if (!oldName.equals(name) || oldQuantity != quantity || !oldQuantityUnit.equals(quantityUnit)) {
                int result = DbUtilities.updateItem(this,
                        itemId,
                        itemIdCloud,
                        name,
                        quantity,
                        quantityUnit,
                        DbUtilities.getCurrentTime(),
                        DbUtilities.getCurrentUserIdFromDB(this),
                        DbUtilities.getCurrentUserIdCloud(this));
                if (result == 1) {
                    // 1 row updated, make a Toast and go back to MainActivity
                    Toast.makeText(ItemEditActivity.this, R.string.notify_item_updated, Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(ItemEditActivity.this, "Nothing changed", Toast.LENGTH_SHORT).show();

            }

            Intent intent = new Intent(ItemEditActivity.this, ShoppingListViewActivity.class);
            intent.putExtra(ShoppingListViewActivity.EXTRA_SHOPPING_LIST_ID, shoppingListId);
            startActivity(intent);

        }
    }

}
