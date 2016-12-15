package pl.com.andrzejgrzyb.shoppinglist.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by Andrzej on 21.06.2016.
 */
public class DbProvider  extends ContentProvider {
    // The URI Matcher used by this Content Provider
//    private static final UriMatcher sUriMatcher = buildUriMatcher(); //TODO napisać UriMatcher
    private DbHelper mDbHelper;
    // content://pl.com.andrzej.shoppinglist/users
    static final int USERS = 100;
    // content://pl.com.andrzej.shoppinglist/shoppinglists
    static final int SHOPPING_LISTS = 200;
    // content://pl.com.andrzej.shoppinglist/shoppinglists/[shopping_list_id]/
    static final int SHOPPING_LISTS_WITH_ITEMS = 201;
    // content://pl.com.andrzej.shoppinglist/items
    static final int ITEMS = 300;


    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
