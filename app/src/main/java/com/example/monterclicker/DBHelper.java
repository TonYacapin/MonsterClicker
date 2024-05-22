package com.example.monterclicker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "monsterClicker.db";
    private static final int DATABASE_VERSION = 2;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create user table
        db.execSQL("CREATE TABLE user (" +
                "id INTEGER PRIMARY KEY, " +
                "coins INTEGER, " +
                "hunger INTEGER, " +
                "thirst INTEGER)");

        // Create items table
        db.execSQL("CREATE TABLE items (" +
                "id INTEGER PRIMARY KEY, " +
                "name TEXT, " +
                "cost INTEGER, " +
                "type TEXT)");

        // Create weapons table
        db.execSQL("CREATE TABLE weapons (" +
                "id INTEGER PRIMARY KEY, " +
                "level INTEGER, " +
                "power INTEGER)");

        // Create weapon upgrades table
        db.execSQL("CREATE TABLE weapon_upgrades (" +
                "id INTEGER PRIMARY KEY, " +
                "weapon_id INTEGER, " +
                "cost INTEGER, " +
                "new_level INTEGER, " +
                "new_power INTEGER)");

        // Insert initial data into user table
        db.execSQL("INSERT INTO user (id, coins, hunger, thirst) VALUES (1, 0, 100, 100)");

        // Insert initial data into items table
        db.execSQL("INSERT INTO items (name, cost, type) VALUES ('Sword', 100, 'weapon_upgrade')");
        db.execSQL("INSERT INTO items (name, cost, type) VALUES ('Shield', 150, 'weapon_upgrade')");
        db.execSQL("INSERT INTO items (name, cost, type) VALUES ('Food', 50, 'food')");
        db.execSQL("INSERT INTO items (name, cost, type) VALUES ('Water', 30, 'water')");

        // Insert initial data into weapons table
        db.execSQL("INSERT INTO weapons (id, level, power) VALUES (1, 1, 10)");

        // Insert initial data into weapon upgrades table
        db.execSQL("INSERT INTO weapon_upgrades (weapon_id, cost, new_level, new_power) VALUES (1, 200, 2, 20)");
        db.execSQL("INSERT INTO weapon_upgrades (weapon_id, cost, new_level, new_power) VALUES (1, 300, 3, 30)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS user");
        db.execSQL("DROP TABLE IF EXISTS items");
        db.execSQL("DROP TABLE IF EXISTS weapons");
        db.execSQL("DROP TABLE IF EXISTS weapon_upgrades");
        onCreate(db);
    }

    // Method to upgrade a weapon
    public void upgradeWeapon(int weaponId, int newLevel, int newPower, int cost) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE weapons SET level = ?, power = ? WHERE id = ?", new Object[]{newLevel, newPower, weaponId});
        db.execSQL("UPDATE user SET coins = coins - ? WHERE id = 1", new Object[]{cost});
    }

    // Method to update hunger and thirst levels
    public void updateHungerThirst(int hunger, int thirst) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE user SET hunger = ?, thirst = ? WHERE id = 1", new Object[]{hunger, thirst});
    }
}
