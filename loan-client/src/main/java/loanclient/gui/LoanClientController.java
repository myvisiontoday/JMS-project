package loanclient.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import loanclient.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class LoanClientController implements Initializable {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @FXML
    private TextField tfSsn;
    @FXML
    private TextField tfAmount;
    @FXML
    private TextField tfTime;
    @FXML
    private ListView<ListViewLine> lvLoanRequestReply;

    private ClientApplicationGateway clientApplicationGateway;

    public LoanClientController() {
        clientApplicationGateway = new ClientApplicationGateway() {
            @Override
            public void ReplyArrived(LoanRequest loanRequest, LoanReply loanReply) {
                ListViewLine listViewLine = getRequestReply(loanRequest);
                listViewLine.setLoanReply(loanReply);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        lvLoanRequestReply.refresh();
                    }
                });
            }
        };

    }

    @FXML
    public void btnSendLoanRequestClicked() {
        // create the BankInterestRequest

        int ssn = Integer.parseInt(tfSsn.getText());
        int amount = Integer.parseInt(tfAmount.getText());
        int time = Integer.parseInt(tfTime.getText());
        LoanRequest loanRequest = new LoanRequest(ssn,amount,time);

        clientApplicationGateway.requestLoan(loanRequest);

        //create the ListViewLine line with the request and add it to lvLoanRequestReply
        ListViewLine listViewLine = new ListViewLine(loanRequest);
        this.lvLoanRequestReply.getItems().add(listViewLine);

    }


    /**
     * This method returns the line of lvMessages which contains the given loan request.
     * @param request BankInterestRequest for which the line of lvMessages should be found and returned
     * @return The ListViewLine line of lvMessages which contains the given request
     */
    private ListViewLine getRequestReply(LoanRequest request) {

        for (int i = 0; i < lvLoanRequestReply.getItems().size(); i++) {
            ListViewLine rr =  lvLoanRequestReply.getItems().get(i);
            if (rr.getLoanRequest() != null && rr.getLoanRequest().equals(request)) {
                return rr;
            }
        }
        return null;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tfSsn.setText("123456");
        tfAmount.setText("80000");
        tfTime.setText("30");
    }
}
