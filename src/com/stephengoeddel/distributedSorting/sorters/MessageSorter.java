package com.stephengoeddel.distributedSorting.sorters;


import com.stephengoeddel.distributedSorting.StringUtily;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.List;

public class MessageSorter extends RemoteSorter {

    MessageSorter(List<Integer> numbers, String serverAddress, int serverPort) {
        super(numbers, serverAddress, serverPort);
    }

    @Override
    public void run() {
        try {
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://" + serverAddress + ":" + serverPort);
            Connection connection = connectionFactory.createConnection();
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create the destination (Topic or Queue)
            Destination destination = session.createQueue("Unsorted");

            MessageProducer producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            String numberString = StringUtily.createStringFromNumbers(numbers);

            TextMessage textMessage = session.createTextMessage(numberString);
            producer.send(textMessage);

            session.close();
            connection.close();
        } catch (Exception e) {
            System.out.println("Encountered Exception attempting to queue message from MessageSorter: " + e.getMessage());
        }
    }
}
