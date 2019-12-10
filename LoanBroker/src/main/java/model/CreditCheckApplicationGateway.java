package model;

import org.glassfish.jersey.client.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class CreditCheckApplicationGateway {
    ClientConfig config;
    Client client;
    URI baseURI;
    WebTarget serviceTarget;
    final Logger logger = LoggerFactory.getLogger(getClass());
    public CreditCheckApplicationGateway() {
        // code to access webservice
        config = new ClientConfig();
        client = ClientBuilder.newClient(config);
        baseURI = UriBuilder.fromUri("http://localhost:8080/credit/rest/history").build();
        serviceTarget = client.target(baseURI);
    }
    public CreditHistory creditCheck(int ssn){
        CreditHistory creditHistory = null;
        Invocation.Builder builder = serviceTarget.path(String.valueOf(ssn)).request().accept(MediaType.APPLICATION_JSON);
        Response response = builder.get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            creditHistory = response.readEntity(CreditHistory.class);
        }
        return creditHistory;
    }
}
