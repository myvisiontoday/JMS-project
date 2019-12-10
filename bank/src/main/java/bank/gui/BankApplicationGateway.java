package bank.gui;

import bank.model.BankInterestReply;
import bank.model.BankInterestRequest;
import com.google.gson.Gson;
import loanclient.model.MessagingReceiveGateway;
import loanclient.model.MessagingSendGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.HashMap;
import java.util.Map;

public abstract class BankApplicationGateway {
    private static Map<String, Integer> messageAndAggregatingId = new HashMap<>();
    final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String INTEREST_REPLY_QUEUE = "interestReplyQueue" ;

    private MessagingSendGateway messagingSendGateway;
    private MessagingReceiveGateway messagingReceiveGateway;


    public BankApplicationGateway(String queueName) {
        try {
            messagingSendGateway = new MessagingSendGateway(INTEREST_REPLY_QUEUE);
            messagingReceiveGateway = new MessagingReceiveGateway(queueName);
            messagingReceiveGateway.SetListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    Gson gson = new Gson();
                    try {
                        BankInterestRequest bankInterestRequest = gson.fromJson(((TextMessage) message).getText(), BankInterestRequest.class);
                        String replyId = message.getJMSCorrelationID();
                        brokerRequestArrived(replyId, bankInterestRequest);
                        int aggregationID = message.getIntProperty("aggregationID");
                        //put message id and relevant aggregation id
                        messageAndAggregatingId.put(replyId,aggregationID );
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
        message.setIntProperty("aggregationID",messageAndAggregatingId.get(replyId));
        messagingSendGateway.SendMessage(message);
        logger.info("Reply to the broker"+bankInterestReply+replyId);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
