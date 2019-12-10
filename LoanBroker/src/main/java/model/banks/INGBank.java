package model.banks;

import bank.model.BankInterestRequest;
import model.BankSender;

import javax.jms.JMSException;

public class INGBank extends BankSender {
    public INGBank(String queueName) throws JMSException {
        super(queueName);
    }

    @Override
    public boolean evaluateRequest(BankInterestRequest bankInterestRequest) {
        return true;
    }
}
