package loanclient.model;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class MessagingSendGateway {
    Connection connection; // to connect to the ActiveMQ

    Session session = null; // session for creating messages, producers and

    private Destination questionsDestination; // reference to a question queue destination
    private MessageProducer questionProducer = null; // for sending message

    public MessagingSendGateway() {
    }

    public MessagingSendGateway(String MESSAGE_REQUEST_QUEUE) throws JMSException {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

        connection = connectionFactory.createConnection();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        questionsDestination = session.createQueue(MESSAGE_REQUEST_QUEUE);

        questionProducer = session.createProducer(questionsDestination);
        connection.start();
    }

    public void SendMessage(Message message) throws JMSException {
        questionProducer.send(message);
    }

    public Message createMessage(String requestMessage) throws JMSException {
        Message message = session.createTextMessage(requestMessage);
        return message;
    }
    public Destination getDestination() {
        return questionsDestination;
    }

    public void setDestination(Destination questionsDestination) {
        this.questionsDestination = questionsDestination;
    }
}
