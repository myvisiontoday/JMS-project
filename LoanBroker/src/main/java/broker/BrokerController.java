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
import model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class BrokerController implements Initializable {
    public static Map<String, LoanRequest> loanRequests = new HashMap<>();
    public static Map<String, BankInterestRequest> bankRequests = new HashMap<>();

    private ApplicationClientGateway applicationClientGateway;
    private ApplicationBankGateway applicationBankGateway;

    private ArchiveApplicationGateway archiveApplicationGateway;
    private CreditCheckApplicationGateway creditCheckApplicationGateway;

    final Logger logger = LoggerFactory.getLogger(getClass());

    @FXML
    private ListView<ListViewLine> lvBankRequestReply;

    public BrokerController() {
        archiveApplicationGateway = new ArchiveApplicationGateway();
        creditCheckApplicationGateway = new CreditCheckApplicationGateway();

        applicationClientGateway = new ApplicationClientGateway() {
            //Loan request listener from Client
            @Override
            protected void LoanRequestArrived(String replyID, LoanRequest loanRequest) {
                // call web service for credit history
                CreditHistory creditHistory = creditCheckApplicationGateway.creditCheck(loanRequest.getSsn());

                BankInterestRequest bankInterestRequest = new BankInterestRequest();
                bankInterestRequest.setAmount(loanRequest.getAmount());
                bankInterestRequest.setTime(loanRequest.getTime());
                if(creditHistory!=null) {
                    bankInterestRequest.setCredit(creditHistory.getCreditScore());
                    bankInterestRequest.setHistory(creditHistory.getHistory());
                }
                else
                    logger.info("Something went wrong with Credit check WebService");

                //send request to the Bank
                applicationBankGateway.sendInterestRequest(replyID,bankInterestRequest);

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
        applicationBankGateway = new ApplicationBankGateway() {
            @Override
            protected void bankReplyArrived(String replyID, BankInterestReply bankInterestReply) {
                LoanRequest loanRequest = loanRequests.get(replyID);
                if(bankInterestReply.getInterest()>0.0)
                {
                    archiveApplicationGateway.archiveLoan(bankInterestReply,loanRequest);
                }
                Gson gson = new Gson();
                LoanReply loanReply = new LoanReply();
                loanReply.setBankID(bankInterestReply.getBankId());
                loanReply.setInterest(bankInterestReply.getInterest());

                 applicationClientGateway.replyLoanRequest(replyID, loanReply);
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
