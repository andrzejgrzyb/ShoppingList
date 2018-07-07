package pl.com.andrzejgrzyb.shoppinglist;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import pl.com.andrzejgrzyb.shoppinglist.adapter.ShopingListsAdapter;
import pl.com.andrzejgrzyb.shoppinglist.data.DbContract;
import pl.com.andrzejgrzyb.shoppinglist.data.DbUtilities;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SimpleCursorAdapter cursorAdapter;
    private DbUtilities dbUtilities;
    private final String TAG = this.getClass().getSimpleName();


    @BindView(R.id.main_activity_toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.nav_view)
    NavigationView mNavigationView;
    @BindView(R.id.shopping_lists_list_view)
    ListView shoppingListsListView;
    @BindView(R.id.empty_listview_textview)
    TextView emptyListTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.onCreate(this);
        dbUtilities = new DbUtilities(getApplicationContext());

        setTitle(R.string.title_activity_main);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView.setNavigationItemSelectedListener(this);

        cursorAdapter = new ShopingListsAdapter(this, R.layout.listview_shoplists, dbUtilities);
        shoppingListsListView.setAdapter(cursorAdapter);

        // Add Context Menu to ListView
        registerForContextMenu(shoppingListsListView);
        // When the list is empty show a TextView with an information about that
        shoppingListsListView.setEmptyView(emptyListTextView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.shopping_lists_list_view) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

            // Get name of clicked ShoppingList
            final Cursor cursor = dbUtilities.getAllShoppingLists();
            cursor.moveToPosition(info.position);
            String shoppingListName = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.ShoppingListsEntry.COLUMN_NAME));
            // Set the name as a Context Menu header
            menu.setHeaderTitle(shoppingListName);

            // Show menu options
            String[] menuItems = getResources().getStringArray(R.array.context_menu_main_activity);
            for (int i = 0; i < menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
            // Close cursor
            cursor.close();
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // Move Cursor to the clicked Shopping List item in the ListView
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final Cursor shoppingListsCursor = dbUtilities.getAllShoppingLists();
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
                dbUtilities.deleteShoppingList(shoppingListId);
                cursorAdapter.swapCursor(dbUtilities.getAllShoppingLists()).close();
                break;
            case 2: // Share
                // Get cursor to items of chosen shopping list
                Cursor itemsCursor = dbUtilities.getShoppingListItemsCursor(shoppingListId);
                // Get shopping list's name and description
                String name = shoppingListsCursor.getString(shoppingListsCursor.getColumnIndexOrThrow(DbContract.ShoppingListsEntry.COLUMN_NAME));
                String description = shoppingListsCursor.getString(shoppingListsCursor.getColumnIndexOrThrow(DbContract.ShoppingListsEntry.COLUMN_DESCRIPTION));
                // Create a share string to be sent
                String shareString = dbUtilities.createShareString(itemsCursor, name, description);
                // Close the cursor
                itemsCursor.close();
                // Start the Activity
                startActivity(dbUtilities.createShareIntent(shareString));
                break;
            default:
                Toast.makeText(this, "WTF?!", Toast.LENGTH_SHORT).show();
                break;
        }
        // Close cursor
        shoppingListsCursor.close();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            // In MainActivity BackButton hides the app
            moveTaskToBack(true);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {
            String shareString = getResources().getString(R.string.share_app_string, getPackageName());
            startActivity(DbUtilities.createShareIntent(shareString));
        } else if (id == R.id.nav_contact) {
            openContactEmailIntent();
        } else if (id == R.id.nav_rate) {
            openAppRating(this);
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // Close database
        dbUtilities.closeDb();
        Log.i(TAG, "onDestroy()");
    }

    @Override
    public void onResume() {
        super.onResume();
        cursorAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void openContactEmailIntent() {
        // Build email address and subject strings
        String emailAddress = getResources().getString(R.string.contact_email_address);
        String subject = getResources().getString(R.string.app_name);
        // Build email body text
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n\n\n\n");
        stringBuilder.append(getResources().getString(R.string.contact_email_text));
        stringBuilder.append("\nVersion: " + BuildConfig.VERSION_NAME);
        stringBuilder.append("\nLanguage: " + LocaleHelper.getLanguage(this));
        stringBuilder.append("\nSERIAL: " + Build.SERIAL);
        stringBuilder.append("\nMODEL: " + Build.MODEL);
        stringBuilder.append("\nID: " + Build.ID);
        stringBuilder.append("\nManufacturer: " + Build.MANUFACTURER);
        stringBuilder.append("\nbrand: " + Build.BRAND);
        stringBuilder.append("\ntype: " + Build.TYPE);
        stringBuilder.append("\nuser: " + Build.USER);
        stringBuilder.append("\nBASE: " + Build.VERSION_CODES.BASE);
        stringBuilder.append("\nINCREMENTAL " + Build.VERSION.INCREMENTAL);
        stringBuilder.append("\nSDK  " + Build.VERSION.SDK);
        stringBuilder.append("\nBOARD: " + Build.BOARD);
        stringBuilder.append("\nBRAND " + Build.BRAND);
        stringBuilder.append("\nHOST " + Build.HOST);
        stringBuilder.append("\nFINGERPRINT: " + Build.FINGERPRINT);
        stringBuilder.append("\nVersion Code: " + Build.VERSION.RELEASE);
        String text = stringBuilder.toString();

        Uri uri = Uri.parse("mailto:" + emailAddress)
                .buildUpon()
                .appendQueryParameter("subject", subject)
                .appendQueryParameter("body", text)
                .build();
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public static void openAppRating(Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + context.getPackageName())));
        } catch (android.content.ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
        }
    }

    @OnClick(R.id.fab)
    public void onFabClick(View view) {
        Intent intent = new Intent(MainActivity.this, ShoppingListEditActivity.class);
        startActivity(intent);
    }

    @OnItemClick(R.id.shopping_lists_list_view)
    public void onShoppingListsItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor clickCursor = (Cursor) parent.getItemAtPosition(position);
        long shoppingListId = clickCursor.getInt(clickCursor.getColumnIndexOrThrow(DbContract.ShoppingListsEntry._ID));

        Intent intent = new Intent(MainActivity.this, ShoppingListViewActivity.class);
        intent.putExtra(ShoppingListViewActivity.EXTRA_SHOPPING_LIST_ID, shoppingListId);
        startActivity(intent);
    }
}

