package edu.ufp.inf.sd.project.producer;

import com.rabbitmq.client.*;
import edu.ufp.inf.sd.project.server.State;
import edu.ufp.inf.sd.project.server.Task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * RabbitMQ speaks multiple protocols. This tutorial uses AMQP 0-9-1, which is
 * an open, general-purpose protocol for messaging. There are a number of
 * clients for RabbitMQ in many different languages. We'll use the Java client
 * provided by RabbitMQ.
 * <p>
 * Download client library (amqp-client-4.0.2.jar) and its dependencies (SLF4J
 * API and SLF4J Simple) and copy them into lib directory.
 * <p>
 * Jargon terms:
 * RabbitMQ is a message broker, i.e., a server that accepts and forwards messages.
 * Producer is a program that sends messages (Producing means sending).
 * Queue is a post box which lives inside a RabbitMQ broker (large message buffer).
 * Consumer is a program that waits to receive messages (Consuming means receiving).
 * The server, client and broker do not have to reside on the same host
 *
 * @author rui
 */
public class JobGroup {

    /* name of the queue */
    public final static String QUEUE_NAME = "jssp_group";
    private State subjectState;
    private Task task;
    private ArrayList<String> observers;
    //Preferences for exchange...
    private final Channel channelToRabbitMq;
    private final String exchangeName;
    private final BuiltinExchangeType exchangeType;
    private final String messageFormat;
    private String receivedMessage;

    public JobGroup(Task task) throws IOException, TimeoutException {
        this.task = task;
        this.subjectState = new State("", "JobGroup", State.JobGroupState.CREATED);
        this.observers = new ArrayList<>();
        this.finalResults = new HashMap<>();


        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("guest");
        factory.setPassword("guest");

        Connection connection = factory.newConnection();
        this.channelToRabbitMq = connection.createChannel();

        this.exchangeName = "JobGroup";
        this.exchangeType = BuiltinExchangeType.FANOUT;
        //String[] bindingKeys={"",""};
        //this.exchangeBindingKeys=bindingKeys;
        this.messageFormat = "UTF-8";

        bindExchangeToChannelRabbitMQ();
        attachJobGroupToChannelExchangeWithKey();
    }

    private HashMap<String, Integer> finalResults;

    public static void main(String[] argv) throws IOException, TimeoutException {

        JobGroup jobGroup = new JobGroup(new Task("edu/ufp/inf/sd/project/data/abz5.txt", new File("edu/ufp/inf/sd/project/data/abz5.txt")));
        sleep(10000);
        jobGroup.sendMessage("stop");

    }

    /**
     * Binds the channel to given exchange name and type.
     *  TODO: Declare exchange type
     */
    private void bindExchangeToChannelRabbitMQ() throws IOException {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Declaring Exchange '" + this.exchangeName + "' with type " + this.exchangeType);
        this.channelToRabbitMq.exchangeDeclare(this.exchangeName, this.exchangeType);

    }

    /**
     * Creates a Consumer associated with an unnamed queue.
     *  TODO: Create exclusive queue with a generated name.
     *  TODO: Consume with deliver and cancel callbacks
     */
    public void attachJobGroupToChannelExchangeWithKey() {
        try {
            String queueName = this.channelToRabbitMq.queueDeclare().getQueue();
            String routingKey = "";
            this.channelToRabbitMq.queueBind(queueName, this.exchangeName, routingKey);

            Logger.getLogger(this.getClass().getName()).log(Level.INFO, " Created consumerChannel bound to Exchange " + this.exchangeName + "...");

            /* Use a DeliverCallback lambda function instead of DefaultConsumer to receive messages from queue;
               DeliverCallback is an interface which provides a single method:
                void handle(String tag, Delivery delivery) throws IOException; */
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), messageFormat);
                System.out.println(" [x] Consumer Tag [" + consumerTag + "] - Received '" + message + "'");
                //Store the received message
                if (message.contains("stop")) {

                }else if(message.contains("|")){

                }
            };
            CancelCallback cancelCallback = consumerTag -> {
                System.out.println(" [x] Consumer Tag [" + consumerTag + "] - Cancel Callback invoked!");
            };

            this.channelToRabbitMq.basicConsume(queueName, true, deliverCallback, cancelCallback);


        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e.toString());
        }
    }

    /**
     * Publish messages to existing exchange instead of the nameless one.
     * The routingKey is empty ("") since the fanout exchange ignores it.
     TODO: Publish message
     */
    public void sendMessage(String msgToSend) throws IOException {
        //RoutingKey will be ignored by FANOUT exchange
        String routingKey = "";
        BasicProperties prop = MessageProperties.PERSISTENT_TEXT_PLAIN;
        System.out.println("sending to workers... " + msgToSend);
        this.channelToRabbitMq.basicPublish(this.exchangeName, routingKey, null, msgToSend.getBytes("UTF-8"));

    }

    /**
     * @return the most recent message received from the broker
     */
    public String getReceivedMessage() {
        return receivedMessage;
    }

    /**
     * @param receivedMessage the received message to set
     */
    public void setReceivedMessage(String receivedMessage) {
        this.receivedMessage = receivedMessage;
    }

    public State getSubjectState() {
        return subjectState;
    }

    public void setSubjectState(State subjectState) {
        this.subjectState = subjectState;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public ArrayList<String> getObservers() {
        return observers;
    }

    public void setObservers(ArrayList<String> observers) {
        this.observers = observers;
    }

    public HashMap<String, Integer> getFinalResults() {
        return finalResults;
    }

    public void setFinalResults(HashMap<String, Integer> finalResults) {
        this.finalResults = finalResults;
    }
}
