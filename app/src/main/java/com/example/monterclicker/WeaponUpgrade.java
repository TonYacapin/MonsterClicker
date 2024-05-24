package com.example.monterclicker;

public class WeaponUpgrade {
    private int weaponId;
    private int cost;
    private int newLevel;
    private int newPower;

    public WeaponUpgrade(int weaponId, int cost, int newLevel, int newPower) {
        this.weaponId = weaponId;
        this.cost = cost;
        this.newLevel = newLevel;
        this.newPower = newPower;
    }

    public int getWeaponId() {
        return weaponId;
    }

    public void setWeaponId(int weaponId) {
        this.weaponId = weaponId;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getNewLevel() {
        return newLevel;
    }

    public void setNewLevel(int newLevel) {
        this.newLevel = newLevel;
    }

    public int getNewPower() {
        return newPower;
    }

    public void setNewPower(int newPower) {
        this.newPower = newPower;
    }
}
