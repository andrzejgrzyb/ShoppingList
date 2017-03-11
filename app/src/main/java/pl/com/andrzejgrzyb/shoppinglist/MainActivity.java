package pl.com.andrzejgrzyb.shoppinglist;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;



import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import pl.com.andrzejgrzyb.shoppinglist.data.DbContract;
import pl.com.andrzejgrzyb.shoppinglist.data.DbUtilities;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener { //,Observer

    private ListView shoppingListsListView;
    private SimpleCursorAdapter cursorAdapter = null;
//    private GoogleConnection googleConnection;
    private DbUtilities dbUtilities;
    private final String TAG = this.getClass().getSimpleName();
    private static final int RC_SIGN_IN = 9001;

    private NavigationView mNavigationView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.onCreate(this);

        setTitle(R.string.title_activity_main);

        setContentView(R.layout.activity_main);

        // Get toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_activity_toolbar);
        setSupportActionBar(toolbar);

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

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        // Populate Shopping Lists list
        shoppingListsListView = (ListView) findViewById(R.id.shopping_lists_list_view);

        // Create GoogleConnection object
//        googleConnection = new GoogleConnection(this);
//        googleConnection.addObserver(this);
//        googleConnection.connectSilently();
        // Get reference to DB
//        dbUtilities = new DbUtilities(getApplicationContext(), googleConnection);
        dbUtilities = new DbUtilities(getApplicationContext());

        // Query DB to get Cursor to list of all Shopping Lists
        final Cursor cursor = dbUtilities.getAllShoppingLists();

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
                    int itemCount = dbUtilities.getShoppingListItemsCount(cursor.getLong(columnIndex));
                    textView.setText(getResources().getQuantityString(R.plurals.numberOfItemsInShoppingList, itemCount, itemCount));
                    return true;
                }
                else if (view.getId() == R.id.percentTextView) {
                    TextView textView = (TextView) view;
                    double percentCompleted = dbUtilities.getPercentageComplete(cursor.getLong(columnIndex));
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
                Cursor clickCursor = (Cursor) parent.getItemAtPosition(position);
                long shoppingListId = clickCursor.getInt(clickCursor.getColumnIndexOrThrow(DbContract.ShoppingListsEntry._ID));

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
            final Cursor cursor = dbUtilities.getAllShoppingLists();
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
                String name =        shoppingListsCursor.getString(shoppingListsCursor.getColumnIndexOrThrow(DbContract.ShoppingListsEntry.COLUMN_NAME));
                String description = shoppingListsCursor.getString(shoppingListsCursor.getColumnIndexOrThrow(DbContract.ShoppingListsEntry.COLUMN_DESCRIPTION));
                // Create a share string to be sent
                String shareString = dbUtilities.createShareString(itemsCursor, name, description);
                // Close the cursor
                itemsCursor.close();
                // Start the Activity
                startActivity(dbUtilities.createShareIntent(shareString));
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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//        if (id == R.id.action_sync) {
//            Log.d(TAG, "getShoppingListsAndItemsInJSONString: \n" +
//                    dbUtilities.getShoppingListsAndItemsInJSONString());
//        }
        // REMEMBER to uncomment the button in menu/main.xml !!!

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

//        if (id == R.id.nav_camera) {
//            // Handle the camera action
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else
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
//        } else if (id == R.id.nav_signout) {
//            googleConnection.disconnect();
//        } else if (id == R.id.nav_signin) {
//            googleConnection.connect();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        // Close database
        dbUtilities.closeDb();
        Log.i(TAG, "onDestroy()");
//        googleConnection.deleteObserver(this);
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
        stringBuilder.append("\nFINGERPRINT: "+Build.FINGERPRINT);
        stringBuilder.append("\nVersion Code: " + Build.VERSION.RELEASE);
        String text = stringBuilder.toString();

        Uri uri = Uri.parse("mailto:" + emailAddress)
                .buildUpon()
                .appendQueryParameter("subject", subject)
                .appendQueryParameter("body", text)
                .build();
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
//            intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] {emailAddress});
//            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
//            intent.putExtra(Intent.EXTRA_TEXT, text);
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

//    @Override
//    public void update(Observable observable, Object data) {
//        if (observable != googleConnection) {
//            return;
//        }
//
//        switch ((State) data) {
//            case CREATED:
//   //             dialog.dismiss();
//                onSignedOutUI();
//                break;
//            case OPENING:
//     //           dialog.show();
//                break;
//            case OPENED:
//    //            dialog.dismiss();
//                // Update the user interface to reflect that the user is signed in.
//                onSignedInUI();
//
//                // We are signed in!
//                // Retrieve some profile information to personalize our app for the user.
//
//                break;
//            case CLOSED:
//   //             dialog.dismiss();
//                onSignedOutUI();
//                break;
//        }
//    }
//    private void onSignedOutUI() {
//        Log.d(TAG, "onSignOutUI");
//        // Bring back default text (app name) and app icon in Nav Drawer header
//        TextView usernameTextView = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.nav_user_name_textview);
//        usernameTextView.setText(R.string.app_name);
//        ImageView navIcon = (ImageView) mNavigationView.getHeaderView(0).findViewById(R.id.nav_imageview);
//        navIcon.setImageResource(R.mipmap.ic_launcher);
//
//        // Show and hide Sign In/Out buttons
//        mNavigationView.getMenu().findItem(R.id.nav_signin).setVisible(true);
//        mNavigationView.getMenu().findItem(R.id.nav_signout).setVisible(false);
//
//        if (dbUtilities != null)
//            cursorAdapter.swapCursor(dbUtilities.getAllShoppingLists()).close();
//    }
//    private void onSignedInUI() {
//        Log.d(TAG, "onSignedInUI");
//        // Show user's name in Nav Drawer header
//        TextView usernameTextView = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.nav_user_name_textview);
//
//        usernameTextView.setText(googleConnection.getName());
//        // Place profile picture in Nav Drawer header
//        ImageView navIcon = (ImageView) mNavigationView.getHeaderView(0).findViewById(R.id.nav_imageview);
//        String urlString = googleConnection.getPhotoUrlString();
//        Log.d(TAG, "Image URL: " + urlString);
//        new DownloadImageTask(navIcon).execute(urlString);
//
//        // Show and hide Sign In/Out buttons
//        mNavigationView.getMenu().findItem(R.id.nav_signin).setVisible(false);
//        mNavigationView.getMenu().findItem(R.id.nav_signout).setVisible(true);
//
//        if (dbUtilities != null)
//         cursorAdapter.swapCursor(dbUtilities.getAllShoppingLists()).close();
//
//    }

//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        //    super.onActivityResult(requestCode, resultCode, data);
//
//        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
//        Log.d(TAG, "onActivityResult");
//        if (requestCode == RC_SIGN_IN) {
//            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
//            googleConnection.handleSignInResult(result);
//        }
//    }

//    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
//        ImageView bmImage;
//
//        public DownloadImageTask(ImageView bmImage) {
//            this.bmImage = bmImage;
//        }
//
//        protected Bitmap doInBackground(String... urls) {
//            String urldisplay = urls[0];
//            Bitmap mIcon11 = null;
//            try {
//                InputStream in = new java.net.URL(urldisplay).openStream();
//                mIcon11 = BitmapFactory.decodeStream(in);
//            } catch (Exception e) {
//               // Log.e("Error", e.getMessage());
//               // e.printStackTrace();
//            }
//            return mIcon11;
//        }
//
//        protected void onPostExecute(Bitmap result) {
//            bmImage.setImageBitmap(result);
//        }
//    }
}

