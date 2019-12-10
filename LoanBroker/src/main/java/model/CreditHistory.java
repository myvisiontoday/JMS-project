package model;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

public class CreditHistory {
    private int creditScore;
    private int history;

    public CreditHistory() {
        this.creditScore = 0;
        this.history = 0;
    }

    public CreditHistory(int creditScore, int history) {
        this.creditScore = creditScore;
        this.history = history;
    }

    public int getCreditScore() {
        return this.creditScore;
    }

    public void setCreditScore(int creditScore) {
        this.creditScore = creditScore;
    }

    public int getHistory() {
        return this.history;
    }

    public void setHistory(int history) {
        this.history = history;
    }

    public String toString() {
        String var10000 = String.valueOf(this.creditScore);
        return "score=" + var10000 + " history=" + String.valueOf(this.history);
    }
}

