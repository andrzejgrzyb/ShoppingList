package pl.grzyb.andrzej.shoppinglist;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import pl.grzyb.andrzej.shoppinglist.data.DbContract;
import pl.grzyb.andrzej.shoppinglist.data.DbHelper;
import pl.grzyb.andrzej.shoppinglist.data.DbUtilities;

public class ShoppingListViewActivity extends AppCompatActivity {

    public static final String EXTRA_SHOPPING_LIST_ID = "shoppingListId";
    private SimpleCursorAdapter cursorAdapter = null;
    private SQLiteDatabase db;
    private ListView itemsListView;
    private Cursor shoppingListCursor;
    private long shoppingListId;


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

        // Get Shopping List ID from Intent
        shoppingListId = getIntent().getLongExtra(EXTRA_SHOPPING_LIST_ID, 0);

        // Get reference to the ListView
        itemsListView = (ListView) findViewById(R.id.item_list_view);

        // Get reference to readable DB
        DbHelper dbHelper = new DbHelper(this);
        db = dbHelper.getReadableDatabase();

        // Query DB to get Cursor to Shopping List entry
        shoppingListCursor = DbUtilities.getShoppingListCursor(db, shoppingListId);
        shoppingListCursor.moveToFirst();

        //Get Name and Description strings
        String shoppingListName = shoppingListCursor.getString(shoppingListCursor.getColumnIndex(
                DbContract.ShoppingListsEntry.COLUMN_NAME));
        String shoppingListDescription = shoppingListCursor.getString(shoppingListCursor.getColumnIndex(
                DbContract.ShoppingListsEntry.COLUMN_DESCRIPTION));
        // Set the header
        setTitle(shoppingListName);
        // Set description
        TextView shoppingListDescriptionTextView = (TextView) findViewById(R.id.shopping_list_description_text_view);
        shoppingListDescriptionTextView.setText(shoppingListDescription);

        // Get shopping list Cloud ID
        final long shoppingListIdCloud = shoppingListCursor.getLong(shoppingListCursor.getColumnIndex(
                DbContract.ShoppingListsEntry.COLUMN_ID_CLOUD));

        // populate Floating Action Button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShoppingListViewActivity.this, ItemEditActivity.class);
                intent.putExtra(ItemEditActivity.EXTRA_SHOPPING_LIST_ID, shoppingListId);
                intent.putExtra(ItemEditActivity.EXTRA_SHOPPING_LIST_ID_CLOUD, shoppingListIdCloud);
                startActivity(intent);
            }
        });

        // Query DB to get Cursor to list of all Items of the Shopping List
        shoppingListCursor = DbUtilities.getShoppingListItemsCursor(db, shoppingListId);

        // Define SimpleCursorAdapter
        cursorAdapter = new SimpleCursorAdapter(this,
                R.layout.listview_item_list,
                this.shoppingListCursor,
                new String[]{DbContract.ItemsEntry.COLUMN_NAME, DbContract.ItemsEntry.COLUMN_QUANTITY,
                        DbContract.ItemsEntry.COLUMN_QUANTITY_UNIT, DbContract.ItemsEntry.COLUMN_CHECKED},
                new int[]{R.id.itemNameTextView, R.id.itemQuantityTextView,
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
                } else if (columnIndex == cursor.getColumnIndex(DbContract.ItemsEntry.COLUMN_CHECKED)) {
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

        itemsListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "click", Toast.LENGTH_SHORT).show();
            }
        });

        // Add Context Menu to ListView
        this.registerForContextMenu(itemsListView);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.item_list_view) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

            // Get name of clicked Item
            shoppingListCursor.moveToPosition(info.position);
            String itemName = shoppingListCursor.getString(shoppingListCursor.getColumnIndexOrThrow(DbContract.ItemsEntry.COLUMN_NAME));
            // Set the name as a Context Menu header
            menu.setHeaderTitle(itemName);

            // Show menu options
            String[] menuItems = getResources().getStringArray(R.array.context_menu_shopping_list_view_activity);
            for (int i = 0; i<menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
            // Close shoppingListCursor
//            shoppingListCursor.close();
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // Move Cursor to the clicked Shopping List item in the ListView
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        shoppingListCursor.moveToPosition(info.position);
        // Get the _ID of clicked ShoppingList
        long itemId = shoppingListCursor.getInt(shoppingListCursor.getColumnIndexOrThrow(DbContract.ShoppingListsEntry._ID));
        // Get clicked menu option's ID (Edit/Delete/Share)
        int menuItemIndex = item.getItemId();

        // Close shoppingListCursor
//        shoppingListCursor.close();

        switch (menuItemIndex) {
            case 0: // Edit
                Intent intent = new Intent(ShoppingListViewActivity.this, ItemEditActivity.class);
                intent.putExtra(ItemEditActivity.EXTRA_ITEM_ID, itemId);
                startActivity(intent);
                break;
            case 1: // Delete
                DbUtilities.deleteItem(this, itemId);
                cursorAdapter.swapCursor(DbUtilities.getShoppingListItemsCursor(db, shoppingListId));
                break;
//            case 2: // Share
//                Toast.makeText(this, getResources().getStringArray(R.array.context_menu_main_activity)[menuItemIndex], Toast.LENGTH_SHORT);
//                break;
            default:
                Toast.makeText(this, "WTF?!", Toast.LENGTH_SHORT);
                break;
        }

        return true;
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
