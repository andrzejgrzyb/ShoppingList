package pl.com.andrzejgrzyb.shoppinglist.activity;

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
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.com.andrzejgrzyb.shoppinglist.BuildConfig;
import pl.com.andrzejgrzyb.shoppinglist.LocaleHelper;
import pl.com.andrzejgrzyb.shoppinglist.R;
import pl.com.andrzejgrzyb.shoppinglist.adapter.ShoppingListHolderItem;
import pl.com.andrzejgrzyb.shoppinglist.adapter.ShoppingListsRecyclerViewAdapter;
import pl.com.andrzejgrzyb.shoppinglist.data.DbContract;
import pl.com.andrzejgrzyb.shoppinglist.data.DbUtilities;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ShoppingListsRecyclerViewAdapter recyclerViewAdapter;

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
    @BindView(R.id.shopping_lists_recycler_view)
    RecyclerView shoppingListsRecyclerView;

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

        recyclerViewAdapter = new ShoppingListsRecyclerViewAdapter(getShoppingListHolderItems());
        shoppingListsRecyclerView.setHasFixedSize(true);
        shoppingListsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        shoppingListsRecyclerView.setAdapter(recyclerViewAdapter);
        registerForContextMenu(shoppingListsRecyclerView);
    }

    @NonNull
    private List<ShoppingListHolderItem> getShoppingListHolderItems() {
        Cursor shoppingListsCursor = dbUtilities.getAllShoppingLists();
        List<ShoppingListHolderItem> shoppingListHolderItemList = new ArrayList<>();
        while (shoppingListsCursor.moveToNext()) {
            long shoppingListId = shoppingListsCursor.getLong(shoppingListsCursor.getColumnIndex(DbContract.ShoppingListsEntry._ID));
            int itemsCount = dbUtilities.getShoppingListItemsCount(shoppingListId);

            shoppingListHolderItemList.add(new ShoppingListHolderItem(
                    shoppingListId,
                    shoppingListsCursor.getString(shoppingListsCursor.getColumnIndex(DbContract.ShoppingListsEntry.COLUMN_NAME)),
                    shoppingListsCursor.getString(shoppingListsCursor.getColumnIndex(DbContract.ShoppingListsEntry.COLUMN_DESCRIPTION)),
                    DbUtilities.formatDate(this, shoppingListsCursor.getLong(shoppingListsCursor.getColumnIndex(DbContract.ShoppingListsEntry.COLUMN_MODIFICATION_DATE))),
                    getResources().getQuantityString(R.plurals.numberOfItemsInShoppingList, itemsCount, itemsCount),
                    getResources().getString(R.string.percentCompleted, dbUtilities.getPercentageComplete(shoppingListId))));
        }
        return shoppingListHolderItemList;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        String[] menuItems = getResources().getStringArray(R.array.context_menu_main_activity);
        for (int i = 0; i < menuItems.length; i++) {
            menu.add(recyclerViewAdapter.getLongClickPosition(), i, i, menuItems[i]);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // Move Cursor to the clicked Shopping List item in the ListView
        final Cursor shoppingListsCursor = dbUtilities.getAllShoppingLists();
        shoppingListsCursor.moveToPosition(item.getGroupId());
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
                recyclerViewAdapter.setmDataset(getShoppingListHolderItems());
                recyclerViewAdapter.notifyDataSetChanged();
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
                startActivity(DbUtilities.createShareIntent(shareString));
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
        recyclerViewAdapter.notifyDataSetChanged();
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
}

