package bank.gui;

import bank.model.BankInterestReply;
import bank.model.BankInterestRequest;
import com.google.gson.Gson;
import javafx.application.Platform;
import loanclient.model.MessagingReceiveGateway;
import loanclient.model.MessagingSendGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public abstract class BankApplicationGateway {
    final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String INTEREST_REQUEST_TO_BANK = "interestRequestQueue" ;
    private static final String INTEREST_REPLY_QUEUE = "interestReplyQueue" ;

    private MessagingSendGateway messagingSendGateway;
    private MessagingReceiveGateway messagingReceiveGateway;


    public BankApplicationGateway() {
        try {
            messagingSendGateway = new MessagingSendGateway(INTEREST_REPLY_QUEUE);
            messagingReceiveGateway = new MessagingReceiveGateway(INTEREST_REQUEST_TO_BANK);
            messagingReceiveGateway.SetListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    Gson gson = new Gson();
                    try {
                        BankInterestRequest bankInterestRequest = gson.fromJson(((TextMessage) message).getText(), BankInterestRequest.class);
                        String replyId = message.getJMSCorrelationID();
                        brokerRequestArrived(replyId, bankInterestRequest);
                        logger.info("Request received from broker "+ bankInterestRequest+replyId);
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public abstract void brokerRequestArrived(String replyId, BankInterestRequest bankInterestRequest);

    public void sendReplyToBroker(String replyId, BankInterestReply bankInterestReply){
        try {
        Gson gson = new Gson();
        String replyMessage = gson.toJson(bankInterestReply);
        Message message = messagingSendGateway.createMessage(replyMessage);
        message.setJMSCorrelationID(replyId);
        messagingSendGateway.SendMessage(message);
        logger.info("Reply to the broker"+bankInterestReply+replyId);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
