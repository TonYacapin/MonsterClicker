package com.example.monterclicker;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ImageView monsterImage;
    private ProgressBar monsterHealthBar;
    private TextView coinTextView;
    private TextView hungerTextView;
    private TextView thirstTextView;
    private TextView attackIndicatorTextView;
    private TextView damageIndicatorTextView;
    private TextView eventTextView;
    private int monsterHealth;
    private int currentMonster = 1;
    private int coins;
    private int hunger = 100;
    private int thirst = 100;
    private SQLiteDatabase db;
    private MediaPlayer bgMediaPlayer;
    private MediaPlayer dieMediaPlayer;
    private int baseMonsterHealth = 50;
    private int baseMonsterCoins = 100;
    private int weaponDamage = 10;
    private Random random = new Random();

    private boolean isShopActivityVisible = false;

    DBHelper dbHelper = new DBHelper(this);

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
        eventTextView = findViewById(R.id.eventTextView); // New TextView for random events

        ImageView shopButton = findViewById(R.id.shopButton);

        findViewById(R.id.background).setBackgroundResource(R.drawable.background);

        Typeface pixelFont = ResourcesCompat.getFont(this, R.font.pixelated_font);
        coinTextView.setTypeface(pixelFont);
        hungerTextView.setTypeface(pixelFont);
        thirstTextView.setTypeface(pixelFont);
        attackIndicatorTextView.setTypeface(pixelFont);
        damageIndicatorTextView.setTypeface(pixelFont);
        eventTextView.setTypeface(pixelFont); // Set typeface for the new event TextView

        db = dbHelper.getWritableDatabase();

        loadGame();
        updateMonsterStats();
        updateHungerThirst();
        initializeDamageIndicator();

        bgMediaPlayer = MediaPlayer.create(this, R.raw.bgmusic);
        bgMediaPlayer.setLooping(true);
        bgMediaPlayer.start();

        dieMediaPlayer = MediaPlayer.create(this, R.raw.monsterdie);

        final Animation shrinkAnimation = AnimationUtils.loadAnimation(this, R.anim.shrink);

        monsterImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                monsterImage.startAnimation(shrinkAnimation);
                int damage = dbHelper.getUserDamage();
                boolean canCrit = hunger > 0 && thirst > 0;
                if (hunger == 0 || thirst == 0) {
                    damage /= 2;
                }

                // Critical strike chance
                if (canCrit) {
                    boolean isCritical = random.nextInt(100) < 10; // 10% chance for a critical hit
                    if (isCritical) {
                        damage *= 5;
                        damageIndicatorTextView.setText("Critical Hit! " + damage);
                        ImageView particleImageView = findViewById(R.id.particleImageView);
                        particleImageView.setVisibility(View.VISIBLE);
                        Animation particleAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.particle_anim);
                        particleImageView.startAnimation(particleAnimation);
                    } else {
                        damageIndicatorTextView.setText(String.valueOf(damage));
                    }
                } else {
                    damageIndicatorTextView.setText(String.valueOf(damage));
                }

                int damageResistance = random.nextInt(5) + 1;
                monsterHealth -= (damage / damageResistance);

                if (monsterHealth <= 0) {
                    ImageView particleImageView = findViewById(R.id.particleImageView);
                    particleImageView.setVisibility(View.VISIBLE);
                    Animation particleAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.particle_anim);
                    particleImageView.startAnimation(particleAnimation);
                    playMonsterDieSound();
                    int coinsToAdd = calculateMonsterCoins();
                    addCoins(coinsToAdd);
                    handleProgression();
                } else {
                    addCoins(1);
                }
                updateMonsterHealthBar();
                updateHungerThirst();
                updateAttackIndicator();

                // Check for random events
                int randomEventChance = random.nextInt(100);
                if (randomEventChance < 1) { // 5% chance for an event
                    if (monsterHealth <= 0.2 * calculateMonsterHealth()) { // Check if monster health is below 30%
                        randomEvent(); // Trigger "Monster Escape" event
                    } else {
                        // Handle other random events here
                    }
                }

                // Check if hunger or thirst penalties apply
                checkHungerThirstPenalties();
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

    private void initializeDamageIndicator() {
        DBHelper dbHelper = new DBHelper(this);
        int initialDamage = dbHelper.getUserDamage();
        damageIndicatorTextView.setText(String.valueOf(initialDamage));
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
        int userDamage = dbHelper.getUserDamage();
        int randomNumber = random.nextInt(100); // Generate a random number between 0 and 99

        if (randomNumber < 95) {
            // Spawn a regular monster 99% of the time
            currentMonster = random.nextInt(9) + 1; // Generates a number between 1 and 9
        } else {
            // Spawn a boss monster 1% of the time
            currentMonster = random.nextInt(4) + 10; // Generates a number between 10 and 13 (boss monsters)
        }

        int monsterResId = getResources().getIdentifier("monster" + currentMonster, "drawable", getPackageName());
        monsterImage.setImageResource(monsterResId);

        // Update background if it's a boss level
        if (currentMonster >= 10) {
            findViewById(R.id.background).setBackgroundResource(R.drawable.backgroundboss);
        } else {
            findViewById(R.id.background).setBackgroundResource(R.drawable.background);
        }

        updateMonsterStats();
    }

    private int calculateMonsterHealth() {
        int healthVariance = random.nextInt(21) - 10;
        int userDamage = dbHelper.getUserDamage();
        if (currentMonster >= 10) {
            return (int) ((baseMonsterHealth + (currentMonster * 155)) * Math.pow(1.2, currentMonster) + (userDamage * 1.5)) + healthVariance;
        } else {
            return (int) ((baseMonsterHealth + (currentMonster * 50)) * Math.pow(1.2, currentMonster) + (userDamage * 1.5)) + healthVariance;
        }
    }

    private int calculateMonsterCoins() {
        int userDamage = dbHelper.getUserDamage();
        int coinVariance = random.nextInt(11) - 5;
        int maxCoins = 1000; // Maximum allowed coins

        int baseCoins;
        if (currentMonster >= 10) {
            baseCoins = baseMonsterCoins * currentMonster * 10; // Base coins on user damage for boss-level monsters
        } else {
            baseCoins = baseMonsterCoins * currentMonster * 3;
        }

        int coins = baseCoins + coinVariance;
        return Math.min(coins, maxCoins);
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
        double multiplier = 1.0;
        if (hunger > 0 && thirst > 0) {
            multiplier = 3.0;
        } else if (hunger > 0 || thirst > 0) {
            multiplier = 2.0;
        }

        int userDamage = dbHelper.getUserDamage();
        int finalAmount = (int) (baseAmount * multiplier * (userDamage / 10.0)); // Base coins on user damage
        coins += finalAmount;

        // Ensure that coins don't exceed the maximum value
        int maxCoins = 99999999; // Define your maximum allowed coins
        coins = Math.min(coins, maxCoins);

        updateCoinText();
        ContentValues values = new ContentValues();
        values.put("coins", coins);
        db.update("user", values, "id = 1", null);
    }

    private void updateCoinText() {
        coinTextView.setText(String.valueOf(coins));
    }

    private void updateMonsterHealthBar() {
        monsterHealthBar.setMax(calculateMonsterHealth());
        monsterHealthBar.setProgress(monsterHealth);
    }

    private void updateHungerThirst() {
        hunger -= random.nextInt(3);
        thirst -= random.nextInt(3);
        hunger = Math.max(0, hunger);
        thirst = Math.max(0, thirst);
        hungerTextView.setText(String.valueOf(hunger));
        thirstTextView.setText(String.valueOf(thirst));

        DBHelper dbHelper = new DBHelper(this);
        dbHelper.updateHungerThirst(hunger, thirst);
    }

    private void updateAttackIndicator() {
        if (hunger == 0 && thirst == 0) {
            attackIndicatorTextView.setText("Hunger and thirst depleted! Attack power halved.");
            attackIndicatorTextView.setVisibility(View.VISIBLE);
        } else if (hunger == 0) {
            attackIndicatorTextView.setText("Hunger depleted! Attack power halved.");
            attackIndicatorTextView.setVisibility(View.VISIBLE);
        } else if (thirst == 0) {
            attackIndicatorTextView.setText("Thirst depleted! Attack power halved.");
            attackIndicatorTextView.setVisibility(View.VISIBLE);
        } else {
            attackIndicatorTextView.setVisibility(View.INVISIBLE);
        }
    }

    // Random Event: Monster Escape Penalty
    private void randomEvent() {
        eventTextView.setText("Monster escaped!");
        eventTextView.setVisibility(View.VISIBLE);

        // Delay the visibility change to invisible after 2000 milliseconds (2 seconds)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                eventTextView.setVisibility(View.INVISIBLE);
            }
        }, 5000);

        // Replace the current monster with a new one
        spawnNextMonster();
    }
    // Check if hunger or thirst penalties apply
    private void checkHungerThirstPenalties() {
        if (hunger == 0 || thirst == 0) {
            int penalty = random.nextInt(50) + 1; // Random penalty between 1 and 50 coins
            coins -= penalty;
            if (coins < 0) {
                coins = 0; // Ensure coins don't go negative
            }
            updateCoinText();
            eventTextView.setText("Hunger or thirst depleted! You lost " + penalty + " coins.");
            eventTextView.setVisibility(View.VISIBLE);

            // Delay the visibility change to invisible after 2000 milliseconds (2 seconds)
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    eventTextView.setVisibility(View.INVISIBLE);
                }
            }, 5000);
        }
    }
}
