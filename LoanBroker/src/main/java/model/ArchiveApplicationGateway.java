package model;

import bank.model.BankInterestReply;
import loanclient.model.LoanRequest;
import org.glassfish.jersey.client.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class ArchiveApplicationGateway {
    ClientConfig config;
    Client client;
    URI baseURI;
    WebTarget serviceTarget;
    final Logger logger = LoggerFactory.getLogger(getClass());

    public ArchiveApplicationGateway() {
        // code to access webservice
         config = new ClientConfig();
         client = ClientBuilder.newClient(config);
         baseURI = UriBuilder.fromUri("http://localhost:8080/archive/rest/accepted").build();
         serviceTarget = client.target(baseURI);
    }

    public void archiveLoan(BankInterestReply bankInterestReply, LoanRequest loanRequest){
            Loan loan = new Loan();
            loan.setSSN(loanRequest.getSsn());
            loan.setAmount(loanRequest.getAmount());
            loan.setTime(loanRequest.getTime());
            loan.setBank(bankInterestReply.getBankId());
            loan.setInterest(bankInterestReply.getInterest());

            Entity<Loan> entity =Entity.entity(loan, MediaType.APPLICATION_JSON);
            Response response = serviceTarget.request().accept(MediaType.APPLICATION_JSON).post(entity);
            if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
                logger.info("loan is archived sucessfully." + loanRequest);
            }
            else
                logger.info(response.getStatusInfo().toString());
    }
}
