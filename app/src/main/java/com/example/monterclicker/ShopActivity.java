package com.example.monterclicker;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ShopActivity extends AppCompatActivity {

    private ListView shopListView;
    private TextView coinTextView;
    private TextView hungerTextView;
    private TextView thirstTextView;
    private SQLiteDatabase db;
    private int userCoins;
    private int hunger;
    private int thirst;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        shopListView = findViewById(R.id.shopListView);
        coinTextView = findViewById(R.id.coinTextView);
        hungerTextView = findViewById(R.id.hungerTextView);
        thirstTextView = findViewById(R.id.thirstTextView);

        DBHelper dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        loadUserData();
        loadShopItems();

        // Set item click listener for the shop items
        shopListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected item
                String selectedItem = (String) parent.getItemAtPosition(position);
                String[] parts = selectedItem.split(" - ");
                String itemName = parts[0];
                int itemCost = Integer.parseInt(parts[1].replaceAll("[\\D]", "")); // Extract cost from string

                // Check if the user has enough coins to buy the item
                if (userCoins >= itemCost) {
                    // Subtract item cost from user's coins
                    userCoins -= itemCost;

                    // Adjust hunger and thirst if necessary
                    if (itemName.equals("Food")) {
                        hunger += 20; // Example: Increase hunger by 20 when food is purchased
                        updateUserData(userCoins, hunger, thirst);
                        loadUserData(); // Refresh user data after updating hunger
                    } else if (itemName.equals("Water")) {
                        thirst += 20; // Example: Increase thirst by 20 when water is purchased
                        updateUserData(userCoins, hunger, thirst);
                        loadUserData(); // Refresh user data after updating thirst
                    }

                    // Display a toast message to indicate successful purchase
                    Toast.makeText(ShopActivity.this, "You bought " + itemName, Toast.LENGTH_SHORT).show();
                } else {
                    // Display a toast message indicating insufficient funds
                    Toast.makeText(ShopActivity.this, "Not enough coins to buy " + itemName, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadUserData() {
        Cursor cursor = db.rawQuery("SELECT coins, hunger, thirst FROM user WHERE id = 1", null);
        if (cursor.moveToFirst()) {
            userCoins = cursor.getInt(cursor.getColumnIndexOrThrow("coins"));
            hunger = cursor.getInt(cursor.getColumnIndexOrThrow("hunger"));
            thirst = cursor.getInt(cursor.getColumnIndexOrThrow("thirst"));
            coinTextView.setText("Coins: " + userCoins);
            hungerTextView.setText("Hunger: " + hunger);
            thirstTextView.setText("Thirst: " + thirst);
        }
        cursor.close();
    }

    private void loadShopItems() {
        Cursor cursor = db.rawQuery("SELECT * FROM items", null);
        List<String> shopItemList = new ArrayList<>();

        while (cursor.moveToNext()) {
            String itemName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            int itemCost = cursor.getInt(cursor.getColumnIndexOrThrow("cost"));
            shopItemList.add(itemName + " - " + itemCost + " gold");
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, shopItemList);
        shopListView.setAdapter(adapter);
    }

    private void updateUserData(int coins, int hunger, int thirst) {
        ContentValues values = new ContentValues();
        values.put("coins", coins);
        values.put("hunger", hunger);
        values.put("thirst", thirst);
        db.update("user", values, "id = 1", null);
    }
}
