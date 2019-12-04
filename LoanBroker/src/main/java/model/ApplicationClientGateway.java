package model;

import bank.model.BankInterestRequest;
import com.google.gson.Gson;
import loanclient.model.LoanReply;
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

public abstract class ApplicationClientGateway {
    public static Map<String, LoanRequest> loanRequests = new HashMap<>();


    private static final String LOAN_REQUEST_QUEUE = "loanRequestQueue" ;
    private static final String LOAN_REPLY_QUEUE = "loanReplyQueue" ;


    private MessagingReceiveGateway messagingReceiveGateway;
    private MessagingSendGateway messagingSendGateway;

    final Logger logger = LoggerFactory.getLogger(getClass());

    public ApplicationClientGateway() {
        try {
            messagingSendGateway = new MessagingSendGateway(LOAN_REPLY_QUEUE);
            messagingReceiveGateway = new MessagingReceiveGateway(LOAN_REQUEST_QUEUE);

            messagingReceiveGateway.SetListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    try {
                        Gson gson = new Gson();
                        LoanRequest loanRequest = gson.fromJson(((TextMessage) message).getText(), LoanRequest.class);
                        if (loanRequest != null){
                            String replyId = message.getJMSCorrelationID();
                            LoanRequestArrived(replyId, loanRequest);
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
    protected abstract void LoanRequestArrived(String replyID, LoanRequest loanRequest);

    public void replyLoanRequest(String replyId, LoanReply loanReply) {
        try {

        Gson gson = new Gson();
        String requestMessage = gson.toJson(loanReply);

        // send message
        Message msg = messagingSendGateway.createMessage(requestMessage);
        msg.setJMSCorrelationID(replyId);
        messagingSendGateway.SendMessage(msg);
        logger.info("Request is forwarded to the bank: " + loanReply);

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
