package com.example.monterclicker;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ImageView monsterImage;
    private ProgressBar monsterHealthBar;
    private TextView coinTextView;
    private TextView hungerTextView;
    private TextView thirstTextView;
    private int monsterHealth = 100; // Initial monster health
    private int currentMonster = 1;
    private int coins;
    private int hunger = 100; // Initial hunger level
    private int thirst = 100; // Initial thirst level
    private SQLiteDatabase db;
    private MediaPlayer bgMediaPlayer;
    private MediaPlayer dieMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        monsterImage = findViewById(R.id.monsterImage);
        monsterHealthBar = findViewById(R.id.monsterHealthBar);
        coinTextView = findViewById(R.id.coinTextView);
        hungerTextView = findViewById(R.id.hungerTextView);
        thirstTextView = findViewById(R.id.thirstTextView);

        ImageView shopButton = findViewById(R.id.shopButton);

        DBHelper dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        loadGame(); // Load game data including coins, hunger, and thirst
        updateMonsterHealthBar();
        updateHungerThirst();

        // Start playing background music
        bgMediaPlayer = MediaPlayer.create(this, R.raw.bgmusic);
        bgMediaPlayer.setLooping(true);
        bgMediaPlayer.start();

        // Initialize MediaPlayer for monster die sound
        dieMediaPlayer = MediaPlayer.create(this, R.raw.monsterdie);

        // Load the shrink animation
        final Animation shrinkAnimation = AnimationUtils.loadAnimation(this, R.anim.shrink);

        monsterImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hunger > 0 && thirst > 0) { // Check if hunger and thirst are greater than zero
                    monsterImage.startAnimation(shrinkAnimation); // Start the animation
                    monsterHealth -= 10; // Decrease health by 10 on each click
                    addCoins(1);
                    if (monsterHealth <= 0) {
                        playMonsterDieSound();
                        spawnNextMonster();
                        addCoins(20); // Example: add 10 coins for defeating a monster
                    }
                    updateMonsterHealthBar();
                    updateHungerThirst(); // Decrease hunger and thirst with each click
                } else {
                    // Inform the user that they cannot click the monster due to hunger or thirst being zero
                    Toast.makeText(MainActivity.this, "Cannot click the monster. Hunger or thirst is zero.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        shopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ShopActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload game data when returning to MainActivity
        loadGame();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release MediaPlayer resources
        if (bgMediaPlayer != null) {
            bgMediaPlayer.release();
            bgMediaPlayer = null;
        }
        if (dieMediaPlayer != null) {
            dieMediaPlayer.release();
            dieMediaPlayer = null;
        }
    }

    private void playMonsterDieSound() {
        if (dieMediaPlayer != null) {
            dieMediaPlayer.start();
        }
    }

    private void spawnNextMonster() {
        currentMonster++;
        if (currentMonster > 5) {
            currentMonster = 1;
        }
        int monsterResId = getResources().getIdentifier("monster" + currentMonster, "drawable", getPackageName());
        monsterImage.setImageResource(monsterResId);
        monsterHealth = 100 * currentMonster; // Example: reset monster health
        updateMonsterHealthBar(); // Update the monster health bar
        Toast.makeText(this, "New monster spawned!", Toast.LENGTH_SHORT).show();
    }

    private void loadGame() {
        Cursor cursor = db.rawQuery("SELECT coins, hunger, thirst FROM user WHERE id = 1", null);
        if (cursor.moveToFirst()) {
            coins = cursor.getInt(cursor.getColumnIndexOrThrow("coins"));
            hunger = cursor.getInt(cursor.getColumnIndexOrThrow("hunger"));
            thirst = cursor.getInt(cursor.getColumnIndexOrThrow("thirst"));
            updateCoinText();
            updateHungerThirst(); // Update hunger and thirst views when reloading game
        }
        cursor.close();
    }

    private void addCoins(int amount) {
        coins += amount;
        ContentValues values = new ContentValues();
        values.put("coins", coins);
        db.update("user", values, "id = 1", null);
        updateCoinText();
    }

    private void updateCoinText() {
        coinTextView.setText("Coins: " + coins);
    }

    private void updateMonsterHealthBar() {
        monsterHealthBar.setProgress(monsterHealth);
    }

    private void updateHungerThirst() {
        hunger -= 1;
        thirst -= 1;
        if (hunger <= 0) hunger = 0;
        if (thirst <= 0) thirst = 0;
        hungerTextView.setText("Hunger: " + hunger);
        thirstTextView.setText("Thirst: " + thirst);

        // Update hunger and thirst values in the database
        DBHelper dbHelper = new DBHelper(this);
        dbHelper.updateHungerThirst(hunger, thirst);
    }
}
