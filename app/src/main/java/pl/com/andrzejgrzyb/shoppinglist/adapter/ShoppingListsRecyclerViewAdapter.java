package pl.com.andrzejgrzyb.shoppinglist.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import pl.com.andrzejgrzyb.shoppinglist.R;
import pl.com.andrzejgrzyb.shoppinglist.ShoppingListViewActivity;

public class ShoppingListsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int EMPTY_VIEW = -1;

    private List<ShoppingListHolderItem> mDataset;
    private int longClickPosition;

    public ShoppingListsRecyclerViewAdapter(List<ShoppingListHolderItem> myDataset) {
        this.mDataset = myDataset;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == EMPTY_VIEW) {
            return new EmptyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_empty, parent, false));
        } else {
            return new ShoppingListsRecyclerViewAdapter.ShoppingListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_shoplist, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ShoppingListViewHolder) {
            ((ShoppingListViewHolder) holder).bind(mDataset.get(position), position);
            holder.itemView.setTag(position);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        // if no shopping lists return one to make place for empty_view
        return mDataset.size() > 0 ? mDataset.size() : 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (mDataset.size() == 0) {
            return EMPTY_VIEW;
        }
        return super.getItemViewType(position);
    }

    public void setmDataset(List<ShoppingListHolderItem> mDataset) {
        this.mDataset = mDataset;
    }

    public int getLongClickPosition() {
        return longClickPosition;
    }

    public class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class ShoppingListViewHolder extends RecyclerView.ViewHolder {
        private final Context context;

        @BindView(R.id.shoppingListNameTextView)
        TextView shoppingListNameTextView;
        @BindView(R.id.percentTextView)
        TextView percentTextView;
        @BindView(R.id.itemsCountTextView)
        TextView itemsCountTextView;
        @BindView(R.id.shoppingListModificationDateTextView)
        TextView modificationDateTextView;
        int position;

        public ShoppingListViewHolder(View v) {
            super(v);
            context = v.getContext();
            ButterKnife.bind(this, v);
        }

        public void bind(ShoppingListHolderItem shoppingList, int position) {
            shoppingListNameTextView.setText(shoppingList.getName());
            percentTextView.setText(shoppingList.getPercentCompleted());
            itemsCountTextView.setText(shoppingList.getItemsCount());
            modificationDateTextView.setText(shoppingList.getModificationDate());
            itemView.setLongClickable(true);
            this.position = position;
        }

        @OnClick
        public void onClick() {
            long shoppingListId = mDataset.get(getAdapterPosition()).getId();
            Intent intent = new Intent(context, ShoppingListViewActivity.class);
            intent.putExtra(ShoppingListViewActivity.EXTRA_SHOPPING_LIST_ID, shoppingListId);
            context.startActivity(intent);
        }

        @OnLongClick
        public boolean onLongClick() {
            longClickPosition = position;
            return false;
        }
    }
}
