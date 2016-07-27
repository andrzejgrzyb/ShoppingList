package pl.grzyb.andrzej.shoppinglist;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
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

import java.util.Arrays;

import pl.grzyb.andrzej.shoppinglist.data.DbContract;
import pl.grzyb.andrzej.shoppinglist.data.DbHelper;
import pl.grzyb.andrzej.shoppinglist.data.DbUtilities;

public class ShoppingListViewActivity extends AppCompatActivity {

    public static final String EXTRA_SHOPPING_LIST_ID = "shoppingListId";
    private SimpleCursorAdapter cursorAdapter = null;
    private SQLiteDatabase db;
    private ListView itemsListView;
    private Cursor shoppingListItemsCursor;
    private long shoppingListId;
    private ShareActionProvider mShareActionProvider;
    private String mShareString;


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
        Cursor shoppingListCursor = DbUtilities.getShoppingListCursor(db, shoppingListId);
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

        shoppingListCursor.close();
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
        shoppingListItemsCursor = DbUtilities.getShoppingListItemsCursor(db, shoppingListId);

        // Define SimpleCursorAdapter
        cursorAdapter = new SimpleCursorAdapter(this,
                R.layout.listview_item_list,
                this.shoppingListItemsCursor,
                new String[]{DbContract.ItemsEntry.COLUMN_NAME, DbContract.ItemsEntry.COLUMN_QUANTITY,
                        DbContract.ItemsEntry.COLUMN_QUANTITY_UNIT, DbContract.ItemsEntry.COLUMN_CHECKED},
                new int[]{R.id.itemNameTextView, R.id.itemQuantityTextView,
                        R.id.itemQuantityUnitTextView, R.id.itemCheckedCheckBox},
                0);

        // set the Adapter and OnClickListener
        itemsListView.setAdapter(cursorAdapter);
        cursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            public boolean setViewValue(View view, final Cursor cursor, int columnIndex) {
                if (columnIndex == cursor.getColumnIndex(DbContract.ItemsEntry.COLUMN_QUANTITY)) {
                    double quantity = cursor.getDouble(columnIndex);
                    TextView textView = (TextView) view;
                    textView.setText(DbUtilities.formatQuantity((quantity)));
                    return true;
                } else if (columnIndex == cursor.getColumnIndex(DbContract.ItemsEntry.COLUMN_CHECKED)) {
                    int checked = cursor.getInt(columnIndex);
                    CheckBox checkBox = (CheckBox) view;
                    if (checked == 1) {
                        checkBox.setChecked(true);
                    } else checkBox.setChecked(false);
                    checkBox.setText("");
                    checkBox.setTag(cursor.getLong(cursor.getColumnIndex(DbContract.ItemsEntry._ID)));

                    // Set listener to save changes when user clicks the checkbox
                    checkBox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            long itemId = (Long) v.getTag();
                            DbUtilities.itemCheckBoxChange(getApplicationContext(), itemId, ((CheckBox) v).isChecked());
                            Toast.makeText(getApplicationContext(), String.valueOf(itemId), Toast.LENGTH_SHORT).show();
                        }
                    });
                    return true;
                } else if (columnIndex == cursor.getColumnIndex(DbContract.ItemsEntry.COLUMN_QUANTITY_UNIT)) {
                    // Get index of coded unit (prefixed one, e.g. #kg)
                    ((TextView) view).setText(getLocalisedQuantitUnit(cursor.getString(columnIndex)));

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

        // Create share button String
        mShareString = createShareString(cursorAdapter.getCursor());
    }

    public String getLocalisedQuantitUnit(String quantityUnit) {
        int unitId = Arrays.asList(getResources().getStringArray(R.array.quantity_units_codes_array)).indexOf(quantityUnit);
        if (unitId != -1)
          return getResources().getStringArray(R.array.quantity_units_array)[unitId];
        else return "";
    }

    private String createShareString(Cursor cursor) {
        StringBuilder shareString = new StringBuilder();
        if (cursor.moveToFirst()) {
            shareString.append(getTitle());
            shareString.append("\n");
            shareString.append(((TextView) findViewById(R.id.shopping_list_description_text_view)).getText());
            do {
                shareString.append("\n");
                shareString.append(cursor.getString(cursor.getColumnIndex(DbContract.ItemsEntry.COLUMN_NAME)));
                shareString.append(" ");
                shareString.append(DbUtilities.formatQuantity(
                        cursor.getDouble(cursor.getColumnIndex(DbContract.ItemsEntry.COLUMN_QUANTITY))));
                shareString.append(getLocalisedQuantitUnit(
                        cursor.getString(cursor.getColumnIndex(DbContract.ItemsEntry.COLUMN_QUANTITY_UNIT))));
            } while (cursor.moveToNext());
        }
        return shareString.toString();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.item_list_view) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

            // Get name of clicked Item
            shoppingListItemsCursor.moveToPosition(info.position);
            String itemName = shoppingListItemsCursor.getString(shoppingListItemsCursor.getColumnIndexOrThrow(DbContract.ItemsEntry.COLUMN_NAME));
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
        shoppingListItemsCursor.moveToPosition(info.position);
        // Get the _ID of clicked ShoppingList
        long itemId = shoppingListItemsCursor.getInt(shoppingListItemsCursor.getColumnIndexOrThrow(DbContract.ShoppingListsEntry._ID));
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
                shoppingListItemsCursor = DbUtilities.getShoppingListItemsCursor(db, shoppingListId);
                cursorAdapter.swapCursor(shoppingListItemsCursor).close();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.menu_shopping_list_view, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareIntent());
        }
        // Return true to display menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear_checked:
                DbUtilities.deleteCheckedItems(this, shoppingListId);
                shoppingListItemsCursor = DbUtilities.getShoppingListItemsCursor(db, shoppingListId);
                cursorAdapter.changeCursor(shoppingListItemsCursor);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }


    public Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                mShareString);
        return shareIntent;
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ShoppingListViewActivity.this, MainActivity.class);
        startActivity(intent);
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
