package pl.grzyb.andrzej.shoppinglist;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import pl.grzyb.andrzej.shoppinglist.data.DbContract;
import pl.grzyb.andrzej.shoppinglist.data.DbHelper;
import pl.grzyb.andrzej.shoppinglist.data.DbUtilities;

public class ShoppingListViewActivity extends AppCompatActivity {

    public static final String EXTRA_SHOPPING_LIST_ID = "shoppingListId";
    private SimpleCursorAdapter cursorAdapter = null;
    private SQLiteDatabase db;
    private ListView itemsListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list_view);

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

        // populate Floating Action Button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(ShoppingListViewActivity.this, ItemEditActivity.class);
//                startActivity(intent);

                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Get reference to the ListView
        itemsListView = (ListView) findViewById(R.id.item_list_view);

        // Get Shopping List ID from Intent
        long shoppingListId = getIntent().getLongExtra(EXTRA_SHOPPING_LIST_ID, 0);

        // Get reference to readable DB
        DbHelper dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        // Query DB to get Cursor to Shopping List entry
        final Cursor shoppingListCursor = DbUtilities.getShoppingListCursor(db, shoppingListId);
        shoppingListCursor.moveToFirst();

        //Get Name and Description strings
        String shoppingListTitle = shoppingListCursor.getString(shoppingListCursor.getColumnIndex(
                DbContract.ShoppingListsEntry.COLUMN_NAME));
        String shoppingListDescription = shoppingListCursor.getString(shoppingListCursor.getColumnIndex(
                DbContract.ShoppingListsEntry.COLUMN_DESCRIPTION));
        // Set the header
        setTitle(shoppingListTitle);
        // Set description
        TextView shoppingListDescriptionTextView = (TextView) findViewById(R.id.shopping_list_description_text_view);
        shoppingListDescriptionTextView.setText(shoppingListDescription);

        // Query DB to get Cursor to list of all Items of the Shopping List
        final Cursor cursor = DbUtilities.getShoppingListItemsCursor(db, shoppingListId);

        // Define SimpleCursorAdapter
        cursorAdapter = new SimpleCursorAdapter(this,
                R.layout.listview_item_list,
                cursor,
                new String[] {DbContract.ItemsEntry.COLUMN_NAME, DbContract.ItemsEntry.COLUMN_QUANTITY,
                        DbContract.ItemsEntry.COLUMN_QUANTITY_UNIT, DbContract.ItemsEntry.COLUMN_CHECKED},
                new int[] {R.id.itemNameTextView, R.id.itemQuantityTextView,
                        R.id.itemQuantityUnitTextView, R.id.itemCheckedCheckBox},
                0);

        // set the Adapter and OnClickListener
        itemsListView.setAdapter(cursorAdapter);
        cursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

                if (columnIndex == cursor.getColumnIndex(DbContract.ItemsEntry.COLUMN_QUANTITY)) {
                    long quantity = cursor.getLong(columnIndex);
                    TextView textView = (TextView) view;
                    textView.setText(Long.toString(quantity));
                    return true;
                }
                else if (columnIndex == cursor.getColumnIndex(DbContract.ItemsEntry.COLUMN_CHECKED)) {
                    int checked = cursor.getInt(columnIndex);
                    CheckBox checkBox = (CheckBox) view;
                    if (checked == 1) {
                        checkBox.setChecked(true);
                    }
                    checkBox.setText("");
                    return true;
                }

                return false;
            }
        });
        View contentShoppingListLayout = findViewById(R.id.content_shopping_list_view);
       // shoppingListNameTextView.setText(shoppingListName);
//        setTitle(shoppingListName);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Close database
        db.close();

    }

    @Override
    public void onResume() {
        super.onResume();
//        cursorAdapter.notifyDataSetChanged();
    }
}
