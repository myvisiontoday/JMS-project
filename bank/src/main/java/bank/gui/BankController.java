package bank.gui;

import bank.model.BankInterestReply;
import bank.model.BankInterestRequest;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import loanclient.model.MessagingReceiveGateway;
import loanclient.model.MessagingSendGateway;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

class BankController implements Initializable {
    private static Map<String, Message> messages = new HashMap<>();

    private static final String INTEREST_REQUEST_TO_BANK = "interestRequestQueue" ;
    private static final String INTEREST_REPLY_QUEUE = "interestReplyQueue" ;
    private final String bankId;
    private MessagingSendGateway messagingSendGateway;


    @SuppressWarnings("unused")
    @FXML
    public ListView<ListViewLine> lvBankRequestReply;
    @SuppressWarnings("unused")
    @FXML
    public TextField tfInterest;

    public BankController(String queueName, String bankId) throws JMSException {
        this.bankId = bankId;
        LoggerFactory.getLogger(getClass()).info("Created BankController with arguments [queueName="+queueName+"] and [bankId="+bankId+"]");
        MessagingReceiveGateway messagingReceiveGateway = new MessagingReceiveGateway(INTEREST_REQUEST_TO_BANK);
        messagingSendGateway = new MessagingSendGateway(INTEREST_REPLY_QUEUE);

        messagingReceiveGateway.SetListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                Gson gson = new Gson();
                BankInterestRequest bankInterestRequest = new BankInterestRequest();
                try {
                    bankInterestRequest = gson.fromJson(((TextMessage) message).getText(), BankInterestRequest.class);
                    messages.put(message.getJMSMessageID(),message);
                } catch (JMSException e) {
                    e.printStackTrace();
                }
                //create the ListViewLine line with the request and add it to lvBankRequestReply
                ListViewLine listViewLine = new ListViewLine(bankInterestRequest);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        lvBankRequestReply.getItems().add(listViewLine);
                    }
                });
            }
        });
    }

    @SuppressWarnings("unused")
    @FXML
    public void btnSendBankInterestReplyClicked() throws JMSException {
        double interest = Double.parseDouble(tfInterest.getText());
        BankInterestReply bankInterestReply = new BankInterestReply(interest, bankId);

         // @TODO send the bankInterestReply
            BankInterestReply reply = new BankInterestReply();
            reply.setBankId(this.bankId);
            reply.setInterest(interest);
            Message oldMessage = null;
            Gson gson = new Gson();
            String textMessage = gson.toJson(bankInterestReply);
            ListViewLine listViewLine = lvBankRequestReply.getSelectionModel().getSelectedItem();
            for(Message m : messages.values())
            {
                BankInterestRequest request = gson.fromJson(((TextMessage) m).getText(), BankInterestRequest.class);
                BankInterestRequest request1 = listViewLine.getBankInterestRequest();
                if(request.toString().equals(request1.toString()))
                {
                    oldMessage = m;
                }
            }
            if(oldMessage!=null){
                Message newMessage = messagingSendGateway.createMessage(textMessage);
                newMessage.setJMSCorrelationID(oldMessage.getJMSMessageID());
                messagingSendGateway.SendMessage(newMessage);
            }
        if (listViewLine!= null){
            listViewLine.setBankInterestReply(bankInterestReply);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    lvBankRequestReply.refresh();
                }
            });
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}
