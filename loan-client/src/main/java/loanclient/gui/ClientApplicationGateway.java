package loanclient.gui;

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

public abstract class ClientApplicationGateway {
    private static Map<String, LoanRequest> messages = new HashMap<>();


    private static final String MESSAGE_REQUEST_QUEUE = "loanRequestQueue" ;
    private static final String MESSAGE_REPLY_QUEUE = "loanReplyQueue" ;

    private MessagingSendGateway messagingSendGateway ;
    private MessagingReceiveGateway messagingReceiveGateway;

    final Logger logger = LoggerFactory.getLogger(getClass());
    public ClientApplicationGateway() {
        try {
            messagingSendGateway = new MessagingSendGateway(MESSAGE_REQUEST_QUEUE);
            messagingReceiveGateway = new MessagingReceiveGateway(MESSAGE_REPLY_QUEUE);
            messagingReceiveGateway.SetListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    Gson gson = new Gson();
                    LoanReply loanReply = new LoanReply();
                    LoanRequest loanRequest = null;
                    try {
                        loanReply = gson.fromJson(((TextMessage) message).getText(), LoanReply.class);
                        logger.info("Request is sent to the Client: " + message.getJMSMessageID());
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                    logger.info("Received the loan reply: " + loanReply);

                    try {
                         loanRequest = messages.get(message.getJMSCorrelationID());
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                    if (loanRequest != null){
                    ReplyArrived(loanRequest,loanReply);
                    }
                    else {
                        logger.info("reply id did not match");
                    }
                }
            });
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public abstract void ReplyArrived(LoanRequest loanRequest, LoanReply loanReply);

    public void requestLoan(LoanRequest loanRequest) {
        try {
            // @TODO: send the loanRequest here...
            //serialize to json
            Gson gson = new Gson();
            String requestMessage = gson.toJson(loanRequest);
            // send message
            Message message = null;
            message = messagingSendGateway.createMessage(requestMessage);
            message.setJMSReplyTo(messagingReceiveGateway.getReceiveDestination());
            messagingSendGateway.SendMessage(message);
            logger.info("Sent the loan request: " + loanRequest);
            logger.info("Request is sent to the Client: " + message.getJMSMessageID());
            messages.put(message.getJMSMessageID(),loanRequest);

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
