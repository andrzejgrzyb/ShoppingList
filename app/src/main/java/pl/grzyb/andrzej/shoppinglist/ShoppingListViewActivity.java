package pl.grzyb.andrzej.shoppinglist;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;


import pl.grzyb.andrzej.shoppinglist.DragNDropList.DragNDropCursorAdapter;
import pl.grzyb.andrzej.shoppinglist.DragNDropList.DragNDropListView;
import pl.grzyb.andrzej.shoppinglist.data.DbContract;
import pl.grzyb.andrzej.shoppinglist.data.DbHelper;
import pl.grzyb.andrzej.shoppinglist.data.DbUtilities;

public class ShoppingListViewActivity extends AppCompatActivity {

    public static final String EXTRA_SHOPPING_LIST_ID = "shoppingListId";
    private DragNDropCursorAdapter cursorAdapter = null;
    private SQLiteDatabase db;
    private DragNDropListView itemsListView;
    private Cursor shoppingListItemsCursor;
    private long shoppingListId;
    private ShareActionProvider mShareActionProvider;


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
        itemsListView = (DragNDropListView) findViewById(R.id.item_list_view);

        // Get reference to readable DB
        DbHelper dbHelper = new DbHelper(this);
        db = dbHelper.getReadableDatabase();

        // Query DB to get Cursor to Shopping List entry
        final Cursor shoppingListCursor = DbUtilities.getShoppingListCursor(db, shoppingListId);
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
        cursorAdapter = new DragNDropCursorAdapter(this,
                R.layout.listview_item_list,
                this.shoppingListItemsCursor,
                new String[]{DbContract.ItemsEntry.COLUMN_NAME, DbContract.ItemsEntry.COLUMN_QUANTITY,
                        DbContract.ItemsEntry.COLUMN_QUANTITY_UNIT, DbContract.ItemsEntry.COLUMN_CHECKED},
                new int[]{R.id.itemNameTextView, R.id.itemQuantityTextView,
                        R.id.itemQuantityUnitTextView, R.id.itemCheckedCheckBox},
                R.id.handler);

        // set the Adapter and OnClickListener
        itemsListView.setDragNDropAdapter(cursorAdapter);
        // set drag n drop listener
        itemsListView.setOnItemDragNDropListener(new DragNDropListView.OnItemDragNDropListener() {
            @Override
            // Called when the item begins dragging.
            public void onItemDrag(DragNDropListView parent, View view, int position, long id) {

            }

            @Override
            // Called after the item is dropped in different place (actually moved)
            //
            public void onItemDrop(DragNDropListView parent, View view, int startPosition, int endPosition, long id) {
                // First, get cursor from adapter
                Cursor cursor = (Cursor) itemsListView.getAdapter().getItem(startPosition);
                DbUtilities.changeItemPosition(getApplicationContext(), cursor, id, startPosition, endPosition);

                shoppingListItemsCursor = DbUtilities.getShoppingListItemsCursor(db, shoppingListId);
                cursorAdapter.swapCursor(shoppingListItemsCursor).close();

                Log.d("MOVE", String.valueOf(id) + ": " +
                        String.valueOf(startPosition) + " -> " + String.valueOf(endPosition));
            }
        });
        cursorAdapter.setViewBinder(new DragNDropCursorAdapter.ViewBinder() {
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
                            refreshListView();
//                            Toast.makeText(getApplicationContext(), String.valueOf(itemId), Toast.LENGTH_SHORT).show();
                        }
                    });
                    return true;
                } else if (columnIndex == cursor.getColumnIndex(DbContract.ItemsEntry.COLUMN_QUANTITY_UNIT)) {
                    // Get index of coded unit (prefixed one, e.g. #kg)
                    ((TextView) view).setText(DbUtilities.getLocalisedQuantitUnit(getApplicationContext(), cursor.getString(columnIndex)));

                    return true;
                }
                return false;
            }
        });

        // Add Context Menu to ListView
        this.registerForContextMenu(itemsListView);

        // When the list is empty show a TextView with an information about that
        itemsListView.setEmptyView((TextView) findViewById(R.id.empty_listview_textview));
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

            // Inflate menu
            getMenuInflater().inflate(R.menu.context_menu_shopping_list_view, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // Move Cursor to the clicked Shopping List item in the ListView
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        shoppingListItemsCursor.moveToPosition(info.position);
        // Get the _ID of clicked ShoppingList
        final long itemId = shoppingListItemsCursor.getInt(shoppingListItemsCursor.getColumnIndexOrThrow(DbContract.ShoppingListsEntry._ID));
        // Get clicked menu option's ID (Edit/Delete/Share)
        int menuItemIndex = item.getItemId();

        switch (menuItemIndex) {
            case R.id.edit: // Edit
                Intent intent = new Intent(ShoppingListViewActivity.this, ItemEditActivity.class);
                intent.putExtra(ItemEditActivity.EXTRA_ITEM_ID, itemId);
                startActivity(intent);
                break;
            case R.id.move: // Move to another list
                // Instantiate an AlertDialog.Builder with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                //  Chain together various setter methods to set the dialog characteristics
                builder.setTitle(R.string.move_to_another_list);
                // Get current shopping list ID to not include it on the "move to..." list
                long currentShoppingListId = DbUtilities.getShoppingListIdBasedOnItemId(db, itemId);
                // Get a cursor with all lists
                final Cursor allShoppingListsCursor = DbUtilities.getAllShoppingListsExceptOf(db, currentShoppingListId);

                // Set cursor adapter if there is another shopping list
                if (allShoppingListsCursor.moveToFirst()) {
                    // Set cursor to the dialog builder, onClickListener and column name to be shown in the dialog as list
                    builder.setCursor(allShoppingListsCursor, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Get clicked shopping list ID
                                    allShoppingListsCursor.moveToPosition(which);
                                    long newShoppingListId = allShoppingListsCursor.getLong(allShoppingListsCursor.getColumnIndex(DbContract.ShoppingListsEntry._ID));
                                    // Call a static method to move item to chosen shopping list
                                    int result = DbUtilities.moveItemToAnotherShoppingList(getApplicationContext(), itemId, newShoppingListId);
                                    if (result > 0) {
                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.moved_to) + " " +
                                                        allShoppingListsCursor.getString(allShoppingListsCursor.getColumnIndex(DbContract.ShoppingListsEntry.COLUMN_NAME)),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                    refreshListView();
                                }
                            },
                            DbContract.ShoppingListsEntry.COLUMN_NAME
                    );
                }
                // If there is no other shopping lists, add info about that.
                else {
                    builder.setMessage(R.string.no_other_shopping_lists);
                }

                //  Get the AlertDialog from create() and show it
                AlertDialog dialog = builder.create();
                dialog.show();

                break;
            case R.id.delete: // Delete
                DbUtilities.deleteItem(this, itemId);
                refreshListView();
                break;
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

        // Create share button String
        String description = ((TextView) findViewById(R.id.shopping_list_description_text_view)).getText().toString();
        String shareString = DbUtilities.createShareString(this, cursorAdapter.getCursor(), getTitle().toString(), description);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(DbUtilities.createShareIntent(shareString));
        }
        // Return true to display menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear_checked:
                DbUtilities.deleteCheckedItems(this, shoppingListId);
                refreshListView();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
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
        // Close cursor and database
        shoppingListItemsCursor.close();
        db.close();

    }

    @Override
    public void onResume() {
        super.onResume();
//        cursorAdapter.notifyDataSetChanged();
    }

    public void refreshListView() {
        shoppingListItemsCursor = DbUtilities.getShoppingListItemsCursor(db, shoppingListId);
        cursorAdapter.swapCursor(shoppingListItemsCursor).close();
    }

}
