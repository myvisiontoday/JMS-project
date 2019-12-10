package model;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

public class Loan {
    private int SSN;
    private int amount;
    private int time;
    private double interest;
    private String bank;

    public Loan() {
        this.SSN = 0;
        this.amount = 0;
        this.time = 0;
        this.interest = -1.0D;
        this.bank = "NO_BANK";
    }

    public Loan(int SSN, int amount, int time, double interest, String bank) {
        this.SSN = SSN;
        this.amount = amount;
        this.time = time;
        this.interest = interest;
        this.bank = bank;
    }

    public int getSSN() {
        return this.SSN;
    }

    public void setSSN(int SSN) {
        this.SSN = SSN;
    }

    public int getAmount() {
        return this.amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getTime() {
        return this.time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public double getInterest() {
        return this.interest;
    }

    public void setInterest(double interest) {
        this.interest = interest;
    }

    public String getBank() {
        return this.bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String toString() {
        return "Loan{SSN=" + this.SSN + ", amount=" + this.amount + ", time=" + this.time + ", interest=" + this.interest + ", bank='" + this.bank + "'}";
    }
}
