package com.example.monterclicker;

import android.content.ContentValues;
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
    private TextView damageIndicatorTextView; // Ensure this is properly declared in your layout XML

    private SQLiteDatabase db;
    private int userCoins;
    private int hunger;
    private int thirst;
    private int damage;

    private DBHelper dbHelper;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        shopListView = findViewById(R.id.shopListView);
        coinTextView = findViewById(R.id.coinTextView);
        hungerTextView = findViewById(R.id.hungerTextView);
        thirstTextView = findViewById(R.id.thirstTextView);
        damageIndicatorTextView = findViewById(R.id.damageIndicatorTextView); // Make sure this is properly declared in your layout XML

        dbHelper = new DBHelper(this);
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

                // Handle the purchase of the selected item
                handleItemPurchase(itemName, itemCost);
            }
        });
    }

    private void loadUserData() {
        Cursor cursor = db.rawQuery("SELECT coins, hunger, thirst, damage FROM user WHERE id = 1", null);
        if (cursor.moveToFirst()) {
            userCoins = cursor.getInt(cursor.getColumnIndexOrThrow("coins"));
            hunger = cursor.getInt(cursor.getColumnIndexOrThrow("hunger"));
            thirst = cursor.getInt(cursor.getColumnIndexOrThrow("thirst"));
            damage = cursor.getInt(cursor.getColumnIndexOrThrow("damage"));
            coinTextView.setText("Gold: " + userCoins);
            hungerTextView.setText("Hunger: " + hunger);
            thirstTextView.setText("Thirst: " + thirst);
            damageIndicatorTextView.setText("Damage: " + damage);
        }
        cursor.close();
    }

    private void loadShopItems() {
        Cursor cursor = db.rawQuery("SELECT * FROM items", null);
        List<String> shopItemList = new ArrayList<>();

        while (cursor.moveToNext()) {
            String itemName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            int itemCost = cursor.getInt(cursor.getColumnIndexOrThrow("cost"));

            // If the item is a sword and its cost needs to be updated based on user damage
            if (itemName.equals("Sword")) {
                // Get the current user damage
                int userDamage = dbHelper.getUserDamage();

                // Calculate the new cost based on user damage
                // Example: Increase the cost by 100 gold for every 10 points of user damage
                itemCost += (userDamage / 10) * 5000;
            }

            // Create description based on item name
            String itemDetails = itemName + " - " + itemCost + " gold";

            // Append description to item name
            shopItemList.add(itemDetails);
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item_shop, shopItemList);
        shopListView.setAdapter(adapter);
    }




    private void handleItemPurchase(String itemName, int itemCost) {
        // Check if the user has enough coins to buy the item
        if (userCoins >= itemCost) {
            // Subtract item cost from user's coins
            userCoins -= itemCost;

            // Adjust hunger and thirst if necessary
            if (itemName.equals("Food")) {
                hunger += 20; // Example: Increase hunger by 20 when food is purchased
            } else if (itemName.equals("Water")) {
                thirst += 20; // Example: Increase thirst by 20 when water is purchased
            } else if (itemName.equals("Sword")) {
                // Increase damage when a sword is purchased
                damage += 10; // Example: Increase damage by 10 when sword is purchased
            }

            // Update user data in the database and UI
            updateUserData(userCoins, hunger, thirst, damage);
            loadUserData(); // Refresh user data after updating hunger, thirst, or coins
            loadShopItems(); // Refresh shop items after purchase

            // Display a toast message to indicate successful purchase

        } else {
            // Display a toast message indicating insufficient funds

        }
    }


    private void updateUserData(int coins, int hunger, int thirst, int damage) {
        ContentValues values = new ContentValues();
        values.put("coins", coins);
        values.put("hunger", hunger);
        values.put("thirst", thirst);
        values.put("damage", damage);
        db.update("user", values, "id = 1", null);
    }
}

