package com.example.monterclicker;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
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
import androidx.core.content.res.ResourcesCompat;

public class MainActivity extends AppCompatActivity {

    private ImageView monsterImage;
    private ProgressBar monsterHealthBar;
    private TextView coinTextView;
    private TextView hungerTextView;
    private TextView thirstTextView;
    private TextView attackIndicatorTextView;
    private TextView damageIndicatorTextView;
    private int monsterHealth;
    private int currentMonster = 1;
    private int coins;
    private int hunger = 100;
    private int thirst = 100;
    private SQLiteDatabase db;
    private MediaPlayer bgMediaPlayer;
    private MediaPlayer dieMediaPlayer;
    private int baseMonsterHealth = 100;
    private int baseMonsterCoins = 10;
    private int weaponDamage = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        monsterImage = findViewById(R.id.monsterImage);
        monsterHealthBar = findViewById(R.id.monsterHealthBar);
        coinTextView = findViewById(R.id.coinTextView);
        hungerTextView = findViewById(R.id.hungerTextView);
        thirstTextView = findViewById(R.id.thirstTextView);
        attackIndicatorTextView = findViewById(R.id.attackIndicatorTextView);
        damageIndicatorTextView = findViewById(R.id.damageIndicatorTextView);

        ImageView shopButton = findViewById(R.id.shopButton);

        Typeface pixelFont = ResourcesCompat.getFont(this, R.font.pixelated_font);
        coinTextView.setTypeface(pixelFont);
        hungerTextView.setTypeface(pixelFont);
        thirstTextView.setTypeface(pixelFont);
        attackIndicatorTextView.setTypeface(pixelFont);
        damageIndicatorTextView.setTypeface(pixelFont);

        DBHelper dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        loadGame();
        updateMonsterStats();
        updateHungerThirst();

        bgMediaPlayer = MediaPlayer.create(this, R.raw.bgmusic);
        bgMediaPlayer.setLooping(true);
        bgMediaPlayer.start();

        dieMediaPlayer = MediaPlayer.create(this, R.raw.monsterdie);

        final Animation shrinkAnimation = AnimationUtils.loadAnimation(this, R.anim.shrink);

        monsterImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                monsterImage.startAnimation(shrinkAnimation);
                int damage = dbHelper.getUserDamage(); // Get damage from user
                monsterHealth -= damage;
                addCoins(10);
                if (monsterHealth <= 0) {
                    playMonsterDieSound();
                    handleProgression(); // Handle progression when monster is defeated
                }
                updateMonsterHealthBar();
                updateHungerThirst();
                updateAttackIndicator();
                updateDamageIndicator(damage);
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

    private void updateDamageIndicator(int damage) {
        damageIndicatorTextView.setText(String.valueOf(damage));
    }
    private void updateMonsterStats() {
        monsterHealth = calculateMonsterHealth();
        updateMonsterHealthBar();
    }



    @Override
    protected void onResume() {
        super.onResume();
        loadGame();
        if (bgMediaPlayer != null && !bgMediaPlayer.isPlaying()) {
            bgMediaPlayer.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bgMediaPlayer != null && bgMediaPlayer.isPlaying()) {
            bgMediaPlayer.pause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bgMediaPlayer != null && bgMediaPlayer.isPlaying()) {
            bgMediaPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    private void handleProgression() {
        spawnNextMonster();
        updateMonsterStats();
    }

    private void spawnNextMonster() {
        currentMonster++;
        if (currentMonster > 5) {
            currentMonster = 1;
        }
        int monsterResId = getResources().getIdentifier("monster" +
                currentMonster, "drawable", getPackageName());
        monsterImage.setImageResource(monsterResId);
        monsterHealth = calculateMonsterHealth();
        updateMonsterHealthBar();

    }

    private int calculateMonsterHealth() {
        return baseMonsterHealth * currentMonster * (weaponDamage / 10);
    }

    private int calculateMonsterCoins() {
        return baseMonsterCoins * currentMonster;
    }

    private void loadGame() {
        Cursor cursor = db.rawQuery("SELECT coins, hunger, thirst FROM user WHERE id = 1", null);
        if (cursor.moveToFirst()) {
            coins = cursor.getInt(cursor.getColumnIndexOrThrow("coins"));
            hunger = cursor.getInt(cursor.getColumnIndexOrThrow("hunger"));
            thirst = cursor.getInt(cursor.getColumnIndexOrThrow("thirst"));
            updateCoinText();
            updateHungerThirst();
        }
        cursor.close();
    }

    private void addCoins(int baseAmount) {
        int finalAmount = baseAmount;
        if (hunger == 0 || thirst == 0) {
            finalAmount = (int) (baseAmount * 0.10);
        }
        coins += finalAmount;
        coins += calculateMonsterCoins();
        ContentValues values = new ContentValues();
        values.put("coins", coins);
        db.update("user", values, "id = 1", null);
        updateCoinText();
    }

    private void updateCoinText() {
        coinTextView.setText(String.valueOf(coins));
    }

    private void updateMonsterHealthBar() {
        monsterHealthBar.setProgress(monsterHealth);
    }

    private void updateHungerThirst() {
        hunger -= 1;
        thirst -= 1;
        if (hunger <= 0) hunger = 0;
        if (thirst <= 0) thirst = 0;
        hungerTextView.setText(String.valueOf(hunger));
        thirstTextView.setText(String.valueOf(thirst));

        DBHelper dbHelper = new DBHelper(this);
        dbHelper.updateHungerThirst(hunger, thirst);
    }

    private void updateAttackIndicator() {
        if (hunger == 0 || thirst == 0) {
            attackIndicatorTextView.setText("Attack Weakened!");
            attackIndicatorTextView.setVisibility(View.VISIBLE);
        } else {
            attackIndicatorTextView.setVisibility(View.INVISIBLE);
        }
    }
}
