package pl.com.andrzejgrzyb.shoppinglist.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.TextView;

import pl.com.andrzejgrzyb.shoppinglist.R;
import pl.com.andrzejgrzyb.shoppinglist.data.DbContract;
import pl.com.andrzejgrzyb.shoppinglist.data.DbUtilities;

public class ShopingListsAdapter extends SimpleCursorAdapter {

    private DbUtilities dbUtilities;

    public ShopingListsAdapter(Context context, int layout, DbUtilities dbUtilities) {
        super(context,
                layout,
                dbUtilities.getAllShoppingLists(),
                new String[]{DbContract.ShoppingListsEntry.COLUMN_NAME, DbContract.ShoppingListsEntry.COLUMN_MODIFICATION_DATE,
                        // the 2 below are never  used, but I want to bind textviews with dara, so I need some "fake" columns ;)
                        DbContract.ShoppingListsEntry._ID, DbContract.ShoppingListsEntry._ID},
                new int[]{R.id.shoppingListNameTextView, R.id.shoppingListModificationDateTextView,
                        // the two view I'll populate with not cursor data, but data from external methods
                        R.id.itemsCountTextView, R.id.percentTextView},
                0);
        this.dbUtilities = dbUtilities;
        setViewBinder(new ViewBinderImpl());
    }

    final class ViewBinderImpl implements ViewBinder {

        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            if (columnIndex == cursor.getColumnIndex(DbContract.ShoppingListsEntry.COLUMN_MODIFICATION_DATE)) {
                long modificationDate = cursor.getLong(columnIndex);
                TextView textView = (TextView) view;
                textView.setText(DbUtilities.formatDate(mContext, modificationDate));
                return true;
            }
            // Here I actually modify items count
            else if (view.getId() == R.id.itemsCountTextView) {
                TextView textView = (TextView) view;
                int itemCount = dbUtilities.getShoppingListItemsCount(cursor.getLong(columnIndex));
                textView.setText(mContext.getResources().getQuantityString(R.plurals.numberOfItemsInShoppingList, itemCount, itemCount));
                return true;
            } else if (view.getId() == R.id.percentTextView) {
                TextView textView = (TextView) view;
                double percentCompleted = dbUtilities.getPercentageComplete(cursor.getLong(columnIndex));
                textView.setText(mContext.getResources().getString(R.string.percentCompleted, percentCompleted));
                return true;
            }
            return false;

        }
    }
}
