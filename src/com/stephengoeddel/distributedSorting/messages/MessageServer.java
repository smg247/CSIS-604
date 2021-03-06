package com.stephengoeddel.distributedSorting.messages;


import com.stephengoeddel.distributedSorting.Driver;
import com.stephengoeddel.distributedSorting.StringUtily;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageServer {

    public static void main(String[] args) {
        String serverAddress = args[0];
        int serverPort = Integer.parseInt(args[1]);

        try {
            while (true) {
                Thread thread = new Thread(new SortingMessageConsumer(serverAddress, serverPort));
                thread.start();
                thread.join();
            }
        } catch (Exception e) {
            System.out.println("Exception encountered in the Message sorting server: " + e.getMessage());
            System.exit(1);
        }
    }

    static class SortingMessageConsumer implements Runnable {
        private final String serverAddress;
        private final int serverPort;

        SortingMessageConsumer(String serverAddress, int serverPort) {
            this.serverAddress = serverAddress;
            this.serverPort = serverPort;
        }

        @Override
        public void run() {
            Connection connection = null;
            Session session = null;
            MessageConsumer consumer = null;
            try {
                ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("tcp://" + serverAddress + ":" + serverPort);
                connection = activeMQConnectionFactory.createConnection();
                connection.start();

                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Queue unsortedQueue = session.createQueue("Unsorted");

                consumer = session.createConsumer(unsortedQueue);
                Message message = consumer.receive(100000);
                if (message instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) message;
                    List<Integer> numbers = StringUtily.createNumbersFromString(textMessage.getText());
                    Collections.sort(numbers);
                    System.out.println("Sorted " + numbers.size() + " numbers");

                    Queue sortedQueue = session.createQueue("Sorted");
                    MessageProducer producer = session.createProducer(sortedQueue);
                    producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

                    TextMessage sortedNumberMessage = session.createTextMessage(StringUtily.createStringFromNumbers(numbers));
                    producer.send(sortedNumberMessage);

                } else {
                    System.out.println("Received wrong type of message: " + message.getClass());
                }

            } catch (Exception e) {
                System.out.println("Exception in the ActiveMQConsumer: " + e.getMessage());
            } finally {
                try {
                    consumer.close();
                    session.close();
                    connection.close();
                } catch (Exception e) {
                    System.out.println("Can't properly close connection to queue, exiting...");
                    System.exit(1);
                }
            }
        }
    }
}
