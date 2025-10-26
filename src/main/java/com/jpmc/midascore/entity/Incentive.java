package com.jpmc.midascore.entity;

public class Incentive {
    private float amount;

    // Constructors
    public Incentive() {
    }

    public Incentive(float amount) {
        this.amount = amount;
    }

    // Getters and Setters
    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Incentive[amount=" + amount + "]";
    }
}