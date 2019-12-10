package bank.gui;

import bank.model.BankInterestReply;
import bank.model.BankInterestRequest;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;

class BankController implements Initializable {
    private static Map<String, BankInterestRequest> messages = new HashMap<>();
    final Logger logger = LoggerFactory.getLogger(getClass());

    private final String bankId;
    private BankApplicationGateway bankApplicationGateway;


    @SuppressWarnings("unused")
    @FXML
    public ListView<ListViewLine> lvBankRequestReply;
    @SuppressWarnings("unused")
    @FXML
    public TextField tfInterest;

    public BankController(String queueName, String bankId) {
        this.bankId = bankId;
        LoggerFactory.getLogger(getClass()).info("Created BankController with arguments [queueName="+queueName+"] and [bankId="+bankId+"]");
        bankApplicationGateway = new BankApplicationGateway(queueName) {
            @Override
            public void brokerRequestArrived(String replyId, BankInterestRequest bankInterestRequest) {
                //create the ListViewLine line with the request and add it to lvBankRequestReply
                ListViewLine listViewLine = new ListViewLine(bankInterestRequest);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        lvBankRequestReply.getItems().add(listViewLine);
                    }
                });

                //put it to hash map
                messages.put(replyId, bankInterestRequest);
            }
        };
    }

    @SuppressWarnings("unused")
    @FXML
    public void btnSendBankInterestReplyClicked() {
        double interest = Double.parseDouble(tfInterest.getText());
        BankInterestReply bankInterestReply = new BankInterestReply(interest, bankId);

         // @TODO send the bankInterestReply
        Gson gson = new Gson();
        String textMessage = gson.toJson(bankInterestReply);

        ListViewLine listViewLine = lvBankRequestReply.getSelectionModel().getSelectedItem();
        String replyId = null;
        Iterator it = messages.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if(pair.getValue().equals(listViewLine.getBankInterestRequest()))
                {
                    replyId = (String) pair.getKey();
                }
            it.remove(); // avoids a ConcurrentModificationException
        }
        if(replyId!=null)
        {
            this.bankApplicationGateway.sendReplyToBroker(replyId, bankInterestReply);
        }
        else
            logger.info("replyID not found");


        listViewLine.setBankInterestReply(bankInterestReply);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                lvBankRequestReply.refresh();
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}
