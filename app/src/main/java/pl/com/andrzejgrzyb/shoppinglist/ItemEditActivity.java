package pl.com.andrzejgrzyb.shoppinglist;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import java.util.Arrays;

import pl.com.andrzejgrzyb.shoppinglist.data.DbContract;
import pl.com.andrzejgrzyb.shoppinglist.data.DbHelper;
import pl.com.andrzejgrzyb.shoppinglist.data.DbUtilities;
import pl.com.andrzejgrzyb.shoppinglist.data.RepeatListener;

public class ItemEditActivity extends AppCompatActivity {
    public static final String EXTRA_SHOPPING_LIST_ID = "shoppingListId";
    public static final String EXTRA_SHOPPING_LIST_ID_CLOUD = "shoppingListIdCloud";
    public static final String EXTRA_ITEM_ID = "itemId";
    // Flag to determine if it is an Editing Activity or Adding brand new Item
    private boolean editFlag;
    private long shoppingListId;
    private long shoppingListIdCloud;
    private String itemQuantityUnit;
    private long itemId;
    private long itemIdCloud;
    private EditText itemNameEditText;
    private EditText itemQuantityEditText;
    private RadioGroup itemQuantityUnitRadioGroup;
    private String oldName;
    private double oldQuantity;
    private String oldQuantityUnit;

    // Google Sign-in
//    private GoogleConnection googleConnection;

    // Database
    private DbUtilities dbUtilities;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.onCreate(this);
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
        itemQuantityUnitRadioGroup = (RadioGroup) findViewById(R.id.quantity_units_flow_radio_group);

        // Get references to + and - buttons
        Button plusButton = (Button) findViewById(R.id.quantity_plus_button);
        Button minusButton = (Button) findViewById(R.id.quantity_minus_button);

        // Set touch listeners to plus/minus Buttons
        plusButton.setOnTouchListener(new RepeatListener(400, 100, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double quantity;
                try {
                    quantity = Double.parseDouble(itemQuantityEditText.getText().toString());
                }
                catch(NumberFormatException nfe) {
                    quantity = 0;
                }

                itemQuantityEditText.setText(DbUtilities.formatQuantity(incrementQuantity(quantity)));
//                Toast.makeText(getApplicationContext(), String.valueOf(interval), Toast.LENGTH_SHORT).show();
            }
        }));
        minusButton.setOnTouchListener(new RepeatListener(400, 100, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double quantity;
                try {
                    quantity = Double.parseDouble(itemQuantityEditText.getText().toString());
                }
                catch(NumberFormatException nfe) {
                    quantity = 0;
                }

                itemQuantityEditText.setText(DbUtilities.formatQuantity(decrementQuantity(quantity)));
            }
        }));

        // Create new connection to Google API
//        googleConnection = new GoogleConnection(this);
        // Get reference to DB
        dbUtilities = new DbUtilities(getApplicationContext());
//        dbUtilities = new DbUtilities(getApplicationContext(), googleConnection);


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

            Cursor cursor = dbUtilities.getDb().query(DbContract.ItemsEntry.TABLE_NAME,
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
                itemQuantityEditText.setText(DbUtilities.formatQuantity(oldQuantity));

                // get unit index in array
                int unitId = Arrays.asList(getResources().getStringArray(R.array.quantity_units_codes_array)).indexOf(oldQuantityUnit);
                // Get text that should be on the checked RadioButton
                if (unitId != -1) {
                    String unitTextLocal = getResources().getStringArray(R.array.quantity_units_array)[unitId];
                    for (int i = 0; i < itemQuantityUnitRadioGroup.getChildCount(); i++) {
                        View view = itemQuantityUnitRadioGroup.getChildAt(i);
                        if (view instanceof RadioButton) {
                            if (((RadioButton) view).getText().equals(unitTextLocal)) {
                                ((RadioButton) view).setChecked(true);
                                break;
                            }
                        }
                    }
                }

                cursor.close();
            }
            else {
                Toast.makeText(this, R.string.error_no_record, Toast.LENGTH_SHORT);
            }
        }
        else {
            // We're adding a brand new Item
            setTitle(R.string.title_activity_item_add);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
//        googleConnection.connectSilently();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Close database
        dbUtilities.closeDb();
    }
    public void onClickSaveButton(View view) {
        // Get name and quantity from the EditText views and trim blank characters
        String name = itemNameEditText.getText().toString().trim();
        // Get quantity, if empty, set zero
        double quantity;
        boolean quantityParseException = false;
        try {
            quantity = Double.parseDouble(itemQuantityEditText.getText().toString().replace(',', '.'));
        }
        catch(NumberFormatException nfe) {
            quantity = 0;
            quantityParseException = true;
        }

        // Get value from units RadioGroup
        int checkedRadioButtonId = itemQuantityUnitRadioGroup.getCheckedRadioButtonId();
        String quantityUnit;
        if (checkedRadioButtonId != -1) {
            RadioButton checkedRadioButton = (RadioButton) findViewById(checkedRadioButtonId);
            // Get string-array with localised units
            String[] unitArray = getResources().getStringArray(R.array.quantity_units_array);
            // get index of checked unit in array
            int unitId = Arrays.asList(unitArray).indexOf(checkedRadioButton.getText());
            // Get string-array with unit codes. Those with hash prefixes
            String[] unitCodesArray = getResources().getStringArray(R.array.quantity_units_codes_array);
            // Write coded unit (with hash prefix) to string
            quantityUnit = unitCodesArray[unitId];
//            Toast.makeText(ItemEditActivity.this, quantityUnit, Toast.LENGTH_SHORT).show();
        }
        else  // if not checked, select "pcs"
            quantityUnit = getResources().getStringArray(R.array.quantity_units_codes_array)[0];


        if (name.length() < 3) {
            // Error: name should be at least 3 characters long
            Toast.makeText(ItemEditActivity.this, R.string.error_name_too_short, Toast.LENGTH_SHORT).show();
        }
        else if (quantityParseException) {
            // Error: not a double
            Toast.makeText(ItemEditActivity.this, R.string.error_cant_parse_number, Toast.LENGTH_SHORT).show();
        }
        else {
            // Insert row into DB
            if (!editFlag) {
                long rowId = dbUtilities.insertItem(
                        0,                  // don't know IdCloud yet, this should be updated by the sync class
                        name,
                        quantity,
                        quantityUnit,
                        shoppingListId,
                        shoppingListIdCloud);
                if (rowId != -1) {
                    // Got a rowId back -> row added to DB, make a Toast and go back to MainActivity
                    Toast.makeText(ItemEditActivity.this, R.string.notify_item_added, Toast.LENGTH_SHORT).show();
                }
            }
            else if (!oldName.equals(name) || oldQuantity != quantity || !oldQuantityUnit.equals(quantityUnit)) {
                int result = dbUtilities.updateItem(
                        itemId,
                        name,
                        quantity,
                        quantityUnit);
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
    // static methods to increment and decrement quantity in EditTextView by clicking + and -
    // with defined intervals
    public static double incrementQuantity(double quantity) {
        if (quantity < 0) {
            return 0;
        }
        else if (quantity < 20) {
            return Math.floor(quantity) + 1;
        }
        else if (quantity < 50) {
            return Math.floor(quantity/5) *5 + 5;
        }
        else if (quantity < 100) {
            return Math.floor(quantity/10) *10 + 10;
        }
        else {
            return Math.floor(quantity/100) *100 + 100;
        }
    }
    public static double decrementQuantity(double quantity) {
        if (quantity <= 0) {
            return 0;
        }
        else if (quantity <= 20) {
            return Math.ceil(quantity) - 1;
        }
        else if (quantity <= 50) {
            return Math.ceil(quantity/5) *5 - 5;
        }
        else if (quantity <= 100) {
            return Math.ceil(quantity/10) *10 - 10;
        }
        else {
            return Math.ceil(quantity/100) *100 - 100;
        }
    }



}
