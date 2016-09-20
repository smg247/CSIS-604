package com.stephengoeddel.distributedSorting.messages;


import com.stephengoeddel.distributedSorting.Driver;
import com.stephengoeddel.distributedSorting.StringUtily;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.List;

public class SortedNumberConsumer implements Runnable, ExceptionListener {
    private List<Integer> numbers;
    private final int serverPort;

    public SortedNumberConsumer(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        try {
            ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("tcp://" + Driver.SERVER_ADDRESS + ":" + serverPort);
            Connection connection = activeMQConnectionFactory.createConnection();
            connection.start();
            connection.setExceptionListener(this);

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue unsortedQueue = session.createQueue("Sorted");

            MessageConsumer consumer = session.createConsumer(unsortedQueue);
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
        } catch (Exception e) {
            System.out.println("Encountered Exception while trying to Dequeue the sorted numbers from the message queue " + e.getMessage());
        }
    }

    @Override
    public void onException(JMSException e) {
        System.out.println("JMS Exception: " + e.getMessage());
    }

    public List<Integer> getNumbers() {
        return numbers;
    }
}
