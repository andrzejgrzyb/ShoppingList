package pl.grzyb.andrzej.shoppinglist;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ShoppingListViewActivity extends AppCompatActivity {

    public static final String EXTRA_SHOPPING_LIST_NAME = "shoppingListName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list_view);

        // this is not working :(
      //  getActionBar().setDisplayHomeAsUpEnabled(true);

        String shoppingListName = getIntent().getStringExtra(EXTRA_SHOPPING_LIST_NAME);
        View contentShoppingListLayout = findViewById(R.id.content_shopping_list);
        TextView shoppingListNameTextView = (TextView) findViewById(R.id.shoppingListNameTextView);
        shoppingListNameTextView.setText(shoppingListName);
    }
}
