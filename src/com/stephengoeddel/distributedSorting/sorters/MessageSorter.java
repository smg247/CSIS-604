package com.stephengoeddel.distributedSorting.sorters;


import com.stephengoeddel.distributedSorting.StringUtily;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.List;

class MessageSorter extends RemoteSorter implements ExceptionListener {

    MessageSorter(List<Integer> numbers, String serverAddress, int serverPort) {
        super(numbers, serverAddress, serverPort);
    }

    @Override
    public void run() {
        try {
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://" + serverAddress + ":" + serverPort);
            enqueueUnsortedNumbers(connectionFactory);
            dequeueSortedNumbers(connectionFactory);
        } catch (Exception e) {
            System.out.println("Encountered Exception attempting to queue message from MessageSorter: " + e.getMessage());
        }
    }

    private void enqueueUnsortedNumbers(ActiveMQConnectionFactory connectionFactory) throws JMSException {
        Connection connection = connectionFactory.createConnection();
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Destination unsortedQueue = session.createQueue("Unsorted");

        MessageProducer producer = session.createProducer(unsortedQueue);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        String numberString = StringUtily.createStringFromNumbers(numbers);

        TextMessage textMessage = session.createTextMessage(numberString);
        producer.send(textMessage);

        producer.close();
        session.close();
        connection.close();
    }

    private void dequeueSortedNumbers(ActiveMQConnectionFactory connectionFactory) throws JMSException {
        Connection connection = connectionFactory.createConnection();
        connection.start();
        connection.setExceptionListener(this);

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue sortedQueue = session.createQueue("Sorted");

        MessageConsumer consumer = session.createConsumer(sortedQueue);
        Message message = consumer.receive(100000);
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            numbers = StringUtily.createNumbersFromString(textMessage.getText());
        } else {
            System.out.println("Received wrong type of message: " + message.getClass());
        }

        consumer.close();
        session.close();
        connection.close();
    }

    @Override
    public void onException(JMSException e) {
        System.out.println("JMS Exception: " + e.getMessage());
    }
}
