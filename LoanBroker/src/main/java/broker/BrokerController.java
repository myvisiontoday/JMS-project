package broker;

import bank.model.BankInterestReply;
import bank.model.BankInterestRequest;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import loanclient.model.LoanReply;
import loanclient.model.LoanRequest;
import loanclient.model.MessagingReceiveGateway;
import loanclient.model.MessagingSendGateway;
import model.ApplicationClientGateway;
import model.ApplicatonBankGateway;
import org.apache.activemq.command.ActiveMQDestination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class BrokerController implements Initializable {
    public static Map<String, LoanRequest> loanRequests = new HashMap<>();
    public static Map<String, BankInterestRequest> bankRequests = new HashMap<>();

    private ApplicationClientGateway applicationClientGateway;
    private ApplicatonBankGateway applicatonBankGateway;

    final Logger logger = LoggerFactory.getLogger(getClass());

    //four Application Gateways

    @FXML
    private ListView<ListViewLine> lvBankRequestReply;

    public BrokerController() throws JMSException {
        applicationClientGateway = new ApplicationClientGateway() {
            //Loan request listener from Client
            @Override
            protected void LoanRequestArrived(String replyID, LoanRequest loanRequest) {
                BankInterestRequest bankInterestRequest = new BankInterestRequest();
                bankInterestRequest.setAmount(loanRequest.getAmount());
                bankInterestRequest.setTime(loanRequest.getTime());
                //send request to the Bank
                applicatonBankGateway.sendInterestRequest(replyID,bankInterestRequest);

                //add to the hash map????? to do
                loanRequests.put(replyID, loanRequest);

                //add to the BankRequest
                bankRequests.put(replyID, bankInterestRequest);

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                    ListViewLine listViewLine = new ListViewLine(loanRequest);
                    lvBankRequestReply.getItems().add(listViewLine);
                    }
                });
            }
        };
        applicatonBankGateway = new ApplicatonBankGateway() {
            @Override
            protected void bankReplyArrived(String replyID, BankInterestReply bankInterestReply) {
                Gson gson = new Gson();
                 LoanReply loanReply = new LoanReply();
                 loanReply.setBankID(bankInterestReply.getBankId());
                 loanReply.setInterest(bankInterestReply.getInterest());
            }
        };

        receiveMessagingGatewayClientToBroker.SetListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                Gson gson = new Gson();
                BankInterestRequest bankInterestRequest = null;
                try {
                    // @TODO: send the BankInterestRequest to the bank...
                    LoanRequest loanRequest = gson.fromJson(((TextMessage) message).getText(), LoanRequest.class);

                    bankInterestRequest = new BankInterestRequest();
                    bankInterestRequest.setAmount(loanRequest.getAmount());
                    bankInterestRequest.setTime(loanRequest.getTime());
                    String requestMessage = gson.toJson(bankInterestRequest);

                    // send message
                    Message msg = sendMessagingGatewayBrokerToBank.createMessage(requestMessage);
                    msg.setJMSReplyTo(receiveMessagingGatewayBankToBroker.getReceiveDestination());
                    msg.setJMSCorrelationID(message.getJMSMessageID());
                    sendMessagingGatewayBrokerToBank.SendMessage(msg);

                    //add to the hash map
                    messages.put(message.getJMSMessageID(),message);
                    messages.put(msg.getJMSMessageID(),msg);
                } catch (JMSException e) {
                    e.printStackTrace();
                }

                //create the ListViewLine line with the request and add it to lvLoanRequestReply
                ListViewLine listViewLine = new ListViewLine(bankInterestRequest);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        lvBankRequestReply.getItems().add(listViewLine);
                    }
                });
                logger.info("Sent the loan request: " + bankInterestRequest);
            }
        });
        receiveMessagingGatewayBankToBroker.SetListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                BankInterestReply reply = null;
                Gson gson = new Gson();
                try {
                    reply = gson.fromJson(((TextMessage)message).getText(),BankInterestReply.class);

                    // @TODO: send the BankInterestRequest to the bank...
                    LoanReply loanReply = new LoanReply();
                    loanReply.setBankID(reply.getBankId());
                    loanReply.setInterest(reply.getInterest());
                    String requestMessage = gson.toJson(loanReply);

                    // prepare to send message
                    Message oldMessage = messages.get(message.getJMSCorrelationID());
                    //String destinationQueue =((ActiveMQDestination)oldMessage.getJMSReplyTo()).getPhysicalName();

                    //create message
                    Message msg = sendMessagingGatewayBrokerToClient.createMessage(requestMessage);
                    msg.setJMSCorrelationID(oldMessage.getJMSCorrelationID());
                    sendMessagingGatewayBrokerToClient.SendMessage(msg);

                    messages.put(msg.getJMSMessageID(),msg);
                    logger.info("Sent the loan reply: " + loanReply);

                    //create the ListViewLine line with the request and add it to lvLoanRequestReply
                    BankInterestRequest request = gson.fromJson(((TextMessage)oldMessage).getText(),BankInterestRequest.class);
                    ListViewLine listViewLine = getRequestReply(request);
                    listViewLine.setBankReply(reply);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            lvBankRequestReply.refresh();
                        }
                    });

                } catch (JMSException e) {
                    e.printStackTrace();
                }

            }
        });

    }


    /**
     * This method returns the line of lvMessages which contains the given loan request.
     * @param request BankInterestRequest for which the line of lvMessages should be found and returned
     * @return The ListViewLine line of lvMessages which contains the given request
     */
    private ListViewLine getRequestReply(BankInterestRequest request) {

        for (int i = 0; i < lvBankRequestReply.getItems().size(); i++) {
            ListViewLine rr =  lvBankRequestReply.getItems().get(i);
            if (rr.getBankRequest() != null && rr.getBankRequest().toString().equals(request.toString())) {
                return rr;
            }
        }
        return null;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}
