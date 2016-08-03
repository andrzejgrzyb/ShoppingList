package pl.grzyb.andrzej.shoppinglist;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import pl.grzyb.andrzej.shoppinglist.data.DbContract;
import pl.grzyb.andrzej.shoppinglist.data.DbHelper;
import pl.grzyb.andrzej.shoppinglist.data.DbUtilities;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ListView shoppingListsListView;
    private SimpleCursorAdapter cursorAdapter = null;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_activity_toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.title_activity_main);

        // populate Floating Action Button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ShoppingListEditActivity.class);
                startActivity(intent);
            }
        });

        // populate Drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        // Get some fake ShoppingList data
//        long userId;
//        long shoppingListId;
//        userId = DbDummyData.insertDummyUser(this, "andgrzyb", "andrzej");
//        shoppingListId = DbDummyData.insertDummyShoppingList(this, "Biedronka", "zakupy spożywcze", userId);
//        DbDummyData.insertDummyItem(this, "mleko", 1.5, "L", shoppingListId, 0, 0, userId);
//        DbDummyData.insertDummyItem(this, "ser żółty", 300, "g", shoppingListId, 1, 0, userId);
//        DbDummyData.insertDummyItem(this, "bułki", 5, "szt.", shoppingListId, 2, 0, userId);
//        shoppingListId = DbDummyData.insertDummyShoppingList(this, "Castorama", "przy okazji jak się będzie", userId);
//        DbDummyData.insertDummyItem(this, "młotek", 1, "szt.", shoppingListId, 0, 0, userId);
//        DbDummyData.insertDummyItem(this, "kołki rozporowe 4x10mm", 8, "szt.", shoppingListId, 1, 0, userId);
//        DbDummyData.insertDummyItem(this, "gwoździe", 50, "szt.", shoppingListId, 2, 0, userId);
//        DbDummyData.insertDummyItem(this, "żarówka 100W", 1, "szt.", shoppingListId, 2, 0, userId);


        // Populate Shopping Lists list
        shoppingListsListView = (ListView) findViewById(R.id.shopping_lists_list_view);

        // Get reference to readable DB
        DbHelper dbHelper = new DbHelper(this);
        db = dbHelper.getReadableDatabase();
        // Query DB to get Cursor to list of all Shopping Lists
        final Cursor cursor = DbUtilities.getAllShoppingLists(db);

        // Define SimpleCursorAdapter
        cursorAdapter = new SimpleCursorAdapter(this,
                R.layout.listview_shoplists,
                cursor,
                new String[]{DbContract.ShoppingListsEntry.COLUMN_NAME, DbContract.ShoppingListsEntry.COLUMN_MODIFICATION_DATE,
                        // the 2 below are never  used, but I want to bind textviews with datra, so I need some "fake" columns ;)
                        DbContract.ShoppingListsEntry._ID, DbContract.ShoppingListsEntry._ID},
                new int[]{R.id.shoppingListNameTextView, R.id.shoppingListModificationDateTextView,
                        // the two view I'll populate with not cursor data, but data from external methods
                        R.id.itemsCountTextView, R.id.percentTextView},
                0);

        // set the Adapter and OnClickListener
        shoppingListsListView.setAdapter(cursorAdapter);
        cursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

                if (columnIndex == cursor.getColumnIndex(DbContract.ShoppingListsEntry.COLUMN_MODIFICATION_DATE)) {
                    long modificationDate = cursor.getLong(columnIndex);
                    TextView textView = (TextView) view;
                    textView.setText(DbUtilities.formatDate(getApplicationContext(), modificationDate));
                    return true;
                }
                // Here I actually modify items count
                else if (view.getId() == R.id.itemsCountTextView) {
                    TextView textView = (TextView) view;
                    int itemCount = DbUtilities.getShoppingListItemsCount(db, cursor.getLong(columnIndex));
                    textView.setText(getResources().getQuantityString(R.plurals.numberOfItemsInShoppingList, itemCount, itemCount));
                    return true;
                }
                else if (view.getId() == R.id.percentTextView) {
                    TextView textView = (TextView) view;
                    double percentCompleted = DbUtilities.getPercentageComplete(db, cursor.getLong(columnIndex));
                    String outputString = String.format(getResources().getString(R.string.percentCompleted), percentCompleted);
                    textView.setText(getResources().getString(R.string.percentCompleted, percentCompleted));
                    return true;
                }

                return false;
            }
        });
        shoppingListsListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

//                cursor.moveToPosition(position);
                Cursor clickCursor = (Cursor) parent.getItemAtPosition(position);
                String shoppingListName = clickCursor.getString(clickCursor.getColumnIndexOrThrow(DbContract.ShoppingListsEntry.COLUMN_NAME));
                long shoppingListId = clickCursor.getInt(clickCursor.getColumnIndexOrThrow(DbContract.ShoppingListsEntry._ID));

                // make a Toast with the clicked item description (just to test it)
                Toast toast = Toast.makeText(getApplicationContext(), shoppingListName, Toast.LENGTH_SHORT);
                toast.show();

                Intent intent = new Intent(MainActivity.this, ShoppingListViewActivity.class);
                intent.putExtra(ShoppingListViewActivity.EXTRA_SHOPPING_LIST_ID, shoppingListId);
                startActivity(intent);
            }



        });

        // Add Context Menu to ListView
        registerForContextMenu(shoppingListsListView);
        // When the list is empty show a TextView with an information about that
        shoppingListsListView.setEmptyView((TextView) findViewById(R.id.empty_listview_textview));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId()==R.id.shopping_lists_list_view) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;

            // Get name of clicked ShoppingList
            final Cursor cursor = DbUtilities.getAllShoppingLists(db);
            cursor.moveToPosition(info.position);
            String shoppingListName = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.ShoppingListsEntry.COLUMN_NAME));
            // Set the name as a Context Menu header
            menu.setHeaderTitle(shoppingListName);

            // Show menu options
            String[] menuItems = getResources().getStringArray(R.array.context_menu_main_activity);
            for (int i = 0; i<menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
            // Close cursor
            cursor.close();
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // Move Cursor to the clicked Shopping List item in the ListView
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        final Cursor shoppingListsCursor = DbUtilities.getAllShoppingLists(db);
        shoppingListsCursor.moveToPosition(info.position);
        // Get the _ID of clicked ShoppingList
        long shoppingListId = shoppingListsCursor.getInt(shoppingListsCursor.getColumnIndexOrThrow(DbContract.ShoppingListsEntry._ID));
        // Get clicked menu option's ID (Edit/Delete/Share)
        int menuItemIndex = item.getItemId();



        switch (menuItemIndex) {
            case 0: // Edit
                Intent intent = new Intent(MainActivity.this, ShoppingListEditActivity.class);
                intent.putExtra(ShoppingListEditActivity.EXTRA_SHOPPING_LIST_ID, shoppingListId);
                startActivity(intent);
                break;
            case 1: // Delete
                DbUtilities.deleteShoppingList(this, shoppingListId);
                cursorAdapter.swapCursor(DbUtilities.getAllShoppingLists(db)).close();
                break;
            case 2: // Share
                // Get cursor to items of chosen shopping list
                Cursor itemsCursor = DbUtilities.getShoppingListItemsCursor(db, shoppingListId);
                // Get shopping list's name and description
                String name =        shoppingListsCursor.getString(shoppingListsCursor.getColumnIndexOrThrow(DbContract.ShoppingListsEntry.COLUMN_NAME));
                String description = shoppingListsCursor.getString(shoppingListsCursor.getColumnIndexOrThrow(DbContract.ShoppingListsEntry.COLUMN_DESCRIPTION));
                // Create a share string to be sent
                String shareString = DbUtilities.createShareString(this, itemsCursor, name, description);
                // Close the cursor
                itemsCursor.close();
                // Start the Activity
                startActivity(DbUtilities.createShareIntent(shareString));
                Toast.makeText(this, getResources().getStringArray(R.array.context_menu_main_activity)[menuItemIndex], Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, "WTF?!", Toast.LENGTH_SHORT);
                break;
        }
        // Close cursor
        shoppingListsCursor.close();
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            // In MainActivity BackButton hides the app
            moveTaskToBack(true);
            //super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
     //   getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
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
        cursorAdapter.notifyDataSetChanged();
    }
}
