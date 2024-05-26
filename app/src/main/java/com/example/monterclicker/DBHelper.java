package com.example.monterclicker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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
                "thirst INTEGER, " +
                "damage INTEGER)");

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
        db.execSQL("INSERT INTO user (id, coins, hunger, thirst, damage) VALUES (1, 0, 100, 100, 20)");

        // Insert initial data into items table
        db.execSQL("INSERT INTO items (name, cost, type) VALUES ('Sword', 10000, 'weapon_upgrade')");;
        db.execSQL("INSERT INTO items (name, cost, type) VALUES ('Food', 40, 'food')");
        db.execSQL("INSERT INTO items (name, cost, type) VALUES ('Water', 20, 'water')");

        // Insert initial data into weapons table
        db.execSQL("INSERT INTO weapons (id, level, power) VALUES (1, 1, 10)");

        // Insert initial data into weapon upgrades table
        db.execSQL("INSERT INTO weapon_upgrades (weapon_id, cost, new_level, new_power) VALUES (1, 10000, 2, 20)");
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

    public int getWeaponPower(int weaponId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT power FROM weapons WHERE id = ?", new String[]{String.valueOf(weaponId)});
        int power = 0;
        if (cursor != null && cursor.moveToFirst()) {
            power = cursor.getInt(cursor.getColumnIndexOrThrow("power"));
            cursor.close();
        }
        return power;
    }

    public WeaponUpgrade getWeaponUpgrade() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT cost, new_level, new_power FROM weapon_upgrades WHERE id = 1", null);
        WeaponUpgrade upgrade = null;
        if (cursor != null && cursor.moveToFirst()) {
            int cost = cursor.getInt(cursor.getColumnIndexOrThrow("cost"));
            int newLevel = cursor.getInt(cursor.getColumnIndexOrThrow("new_level"));
            int newPower = cursor.getInt(cursor.getColumnIndexOrThrow("new_power"));
            upgrade = new WeaponUpgrade(1, cost, newLevel, newPower); // Assuming WeaponUpgrade constructor requires an ID
            cursor.close();
        }
        return upgrade;
    }

    public void upgradeWeapon(int newPower) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("power", newPower);
        db.update("weapons", values, "id = 1", null); // Assuming the weapon ID is always 1
    }

    public void updateHungerThirst(int hunger, int thirst) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE user SET hunger = ?, thirst = ? WHERE id = 1", new Object[]{hunger, thirst});
    }

    public void updateUserDamage(int newDamage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("damage", newDamage);
        db.update("user", values, "id = ?", new String[]{String.valueOf(1)});
    }

    public int getUserDamage() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT damage FROM user WHERE id = 1", null);
        int damage = 0;
        if (cursor != null && cursor.moveToFirst()) {
            damage = cursor.getInt(cursor.getColumnIndexOrThrow("damage"));
            cursor.close();
        }
        return damage;
    }
}
