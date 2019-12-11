package model;

import bank.model.BankInterestReply;
import bank.model.BankInterestRequest;
import com.google.gson.Gson;
import loanclient.model.MessagingReceiveGateway;
import loanclient.model.MessagingSendGateway;
import model.banks.ABNBank;
import model.banks.INGBank;
import model.banks.RaboBank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ApplicationBankGateway {

    private static final String INTEREST_REQUEST_QUEUE = "ingRequestQueue" ;
    private static final String INTEREST_REPLY_QUEUE = "interestReplyQueue" ;
    private static int counter = 0;

    private Map<Integer, BankReplyAggregator> bankReplyAggregators = new HashMap<>();
    private List<BankSender> bankSenderList = new ArrayList<>();


    private MessagingReceiveGateway messagingReceiveGateway;
    private MessagingSendGateway messagingSendGateway;

    final Logger logger = LoggerFactory.getLogger(getClass());

    public ApplicationBankGateway() {
        try {
            bankSenderList.add(new INGBank("ingRequestQueue"));
            bankSenderList.add(new ABNBank("abnRequestQueue"));
            bankSenderList.add(new RaboBank("raboRequestQueue"));
        } catch (JMSException e) {
            e.printStackTrace();
        }

        try {
            messagingSendGateway = new MessagingSendGateway(INTEREST_REQUEST_QUEUE);
            messagingReceiveGateway = new MessagingReceiveGateway(INTEREST_REPLY_QUEUE);

            messagingReceiveGateway.SetListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    try {
                        Gson gson = new Gson();
                        BankInterestReply bankInterestReply = gson.fromJson(((TextMessage) message).getText(), BankInterestReply.class);
                        int aggregationID = message.getIntProperty("aggregationID");
                        BankReplyAggregator bankReplyAggregator = bankReplyAggregators.get(aggregationID);
                        if(bankInterestReply!=null)
                        {
                            bankReplyAggregator.addReply(bankInterestReply);
                            if(bankReplyAggregator.finish()) {
                                BankInterestReply bestBankInterestReply = bankReplyAggregator.findBestReply();
                                String replyId = message.getJMSCorrelationID();
                                bankReplyArrived(replyId, bestBankInterestReply);
                            }
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
            msg.setIntProperty("aggregationID",counter);
            int nrOfBankRequest = 0;
            for(BankSender bank: bankSenderList)
            {
                if (bank.evaluateRequest(bankInterestRequest))
                {
                    bank.getSender().SendMessage(msg);
                    nrOfBankRequest++;
                }
            }
            BankReplyAggregator bankReplyAggregator = new BankReplyAggregator(nrOfBankRequest);
            bankReplyAggregators.put(counter, bankReplyAggregator);
            counter++;
            logger.info("Request is forwarded to the bank: " + bankInterestRequest + replyId);

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
