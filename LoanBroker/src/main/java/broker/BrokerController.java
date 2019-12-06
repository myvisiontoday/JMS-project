package broker;

import bank.model.BankInterestReply;
import bank.model.BankInterestRequest;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import loanclient.model.LoanReply;
import loanclient.model.LoanRequest;import model.ApplicationClientGateway;
import model.ApplicatonBankGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class BrokerController implements Initializable {
    public static Map<String, LoanRequest> loanRequests = new HashMap<>();
    public static Map<String, BankInterestRequest> bankRequests = new HashMap<>();

    //two gateways for Broker application
    private ApplicationClientGateway applicationClientGateway;
    private ApplicatonBankGateway applicatonBankGateway;

    @FXML
    private ListView<ListViewLine> lvBankRequestReply;

    public BrokerController() {
        applicationClientGateway = new ApplicationClientGateway() {
            //Loan request listener from Client
            @Override
            protected void LoanRequestArrived(String replyID, LoanRequest loanRequest) {
                BankInterestRequest bankInterestRequest = new BankInterestRequest();
                bankInterestRequest.setAmount(loanRequest.getAmount());
                bankInterestRequest.setTime(loanRequest.getTime());
                //send request to the Bank
                applicatonBankGateway.sendInterestRequest(replyID,bankInterestRequest);

                //add to the hash map?????
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

                 applicationClientGateway.replyLoanRequest(replyID, loanReply);
                 LoanRequest loanRequest = loanRequests.get(replyID);
                 if(loanRequest!=null) {
                     Platform.runLater(new Runnable() {
                         @Override
                         public void run() {
                             ListViewLine listViewLine = getRequestReply(loanRequest);
                             listViewLine.setBankReply(bankInterestReply);
                             lvBankRequestReply.refresh();
                         }
                     });
                 }
            }
        };

    }


    /**
     * This method returns the line of lvMessages which contains the given loan request.
     * @param request BankInterestRequest for which the line of lvMessages should be found and returned
     * @return The ListViewLine line of lvMessages which contains the given request
     */
    private ListViewLine getRequestReply(LoanRequest request) {

        for (int i = 0; i < lvBankRequestReply.getItems().size(); i++) {
            ListViewLine rr =  lvBankRequestReply.getItems().get(i);
            if (rr.getBankRequest() != null && rr.getBankRequest().equals(request)) {
                return rr;
            }
        }
        return null;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}
