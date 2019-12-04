package model;

import bank.model.BankInterestReply;
import bank.model.BankInterestRequest;
import com.google.gson.Gson;
import loanclient.model.LoanRequest;
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

public abstract class ApplicatonBankGateway {
    public static Map<String, BankInterestRequest> loanRequests = new HashMap<>();


    private static final String INTEREST_REQUEST_QUEUE = "interestRequestQueue" ;
    private static final String INTEREST_REPLY_QUEUE = "interestReplyQueue" ;


    private MessagingReceiveGateway messagingReceiveGateway;
    private MessagingSendGateway messagingSendGateway;

    final Logger logger = LoggerFactory.getLogger(getClass());

    public ApplicatonBankGateway() {
        try {
            messagingSendGateway = new MessagingSendGateway(INTEREST_REQUEST_QUEUE);
            messagingReceiveGateway = new MessagingReceiveGateway(INTEREST_REPLY_QUEUE);

            messagingReceiveGateway.SetListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    try {
                        Gson gson = new Gson();
                        BankInterestReply bankInterestReply = gson.fromJson(((TextMessage) message).getText(), BankInterestReply.class);
                        if (bankInterestReply != null){
                            String replyId = message.getJMSCorrelationID();
                            bankReplyArrived(replyId, bankInterestReply);

                        }
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
    protected abstract void bankReplyArrived(String replyID, BankInterestReply bankInterestReply);

    public void sendInterestRequest(String replyId, BankInterestRequest bankInterestRequest) {
        try {
            Gson gson = new Gson();
            String requestMessage = gson.toJson(bankInterestRequest);

            // send message
            Message msg = messagingSendGateway.createMessage(requestMessage);
            msg.setJMSCorrelationID(replyId);
            messagingSendGateway.SendMessage(msg);
            logger.info("Request is forwarded to the bank: " + bankInterestRequest);

            //add to the hash map????? to do
            loanRequests.put(replyId,bankInterestRequest);

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
