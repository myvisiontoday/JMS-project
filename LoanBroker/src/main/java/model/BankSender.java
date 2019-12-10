package model;

import bank.model.BankInterestRequest;
import loanclient.model.MessagingSendGateway;

import javax.jms.JMSException;

public abstract class BankSender {
    private MessagingSendGateway messagingSendGateway;
    private String queueName;
    public BankSender(String queueName) throws JMSException {
        this.queueName = queueName;
        messagingSendGateway = new MessagingSendGateway(queueName);
    }

    public abstract boolean evaluateRequest(BankInterestRequest bankInterestRequest);

    public MessagingSendGateway getSender(){
            return messagingSendGateway;
    }
}
