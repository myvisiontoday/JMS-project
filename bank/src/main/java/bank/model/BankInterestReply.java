package bank.model;

import java.util.Objects;

/**
 * This class stores information about the bank reply
 *  to a loan request of the specific client
 * 
 */
public class BankInterestReply {

    private double interest; // the loan interest
    private String bankId; // the unique quote Id
    
    public BankInterestReply() {
        this.interest = 0;
        this.bankId = "";
    }
    
    public BankInterestReply(double interest, String quoteId) {
        this.interest = interest;
        this.bankId = quoteId;
    }

    @SuppressWarnings("unused")
    public double getInterest() {
        return interest;
    }

    @SuppressWarnings("unused")
    public void setInterest(double interest) {
        this.interest = interest;
    }

    @SuppressWarnings("unused")
    public String getBankId() {
        return bankId;
    }

    @SuppressWarnings("unused")
    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String toString() {
        return "quote=" + this.bankId + " interest=" + this.interest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BankInterestReply that = (BankInterestReply) o;
        return Double.compare(that.interest, interest) == 0 &&
                Objects.equals(bankId, that.bankId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interest, bankId);
    }
}
