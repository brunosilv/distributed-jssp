package edu.ufp.inf.sd.project.consumer;

/**
 * Consumer will keep running to listen for messages from queue and print them out.
 * <p>
 * DefaultConsumer is a class implementing the Consumer interface, used to buffer
 * the messages pushed to us by the server.
 * <p>
 * Compile with RabbitMQ java client on the classpath:
 * javac -cp amqp-client-4.0.2.jar RPCServer.java RPCClient.java
 * <p>
 * Run with need rabbitmq-client.jar and its dependencies on the classpath.
 * java -cp .:amqp-client-4.0.2.jar:slf4j-api-1.7.21.jar:slf4j-simple-1.7.22.jar ReceiveLogs
 * java -cp .:amqp-client-4.0.2.jar:slf4j-api-1.7.21.jar:slf4j-simple-1.7.22.jar Producer
 * <p>
 * OR
 * export CP=.:amqp-client-4.0.2.jar:slf4j-api-1.7.21.jar:slf4j-simple-1.7.22.jar
 * java -cp $CP Producer
 * java -cp %CP% Producer
 * <p>
 * The client will print the message it gets from the publisher via RabbitMQ.
 * The client will keep running, waiting for messages (Use Ctrl-C to stop it).
 * Try running the publisher from another terminal.
 * <p>
 * Check RabbitMQ Broker runtime info (credentials: guest/guest4rabbitmq):
 * http://localhost:15672/
 *
 * @author rui
 */

import com.rabbitmq.client.*;
import edu.ufp.inf.sd.project.client.WorkerState;
import edu.ufp.inf.sd.project.server.Task;
import edu.ufp.inf.sd.project.util.geneticalgorithm.CrossoverStrategies;
import edu.ufp.inf.sd.project.util.geneticalgorithm.GeneticAlgorithmJSSP;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Worker {

    public String id;
    public WorkerState lastObserverState;
    private Integer credits;
    private Integer workResult;
    private Task currentTask;
    private final Channel channelToRabbitMq;
    private final String exchangeName;
    private final BuiltinExchangeType exchangeType;
    private final String messageFormat;
    private String receivedMessage;
    private final String qa_queue = "jssp_ga_" + this.id;
    private final String qa_queue_results = qa_queue + "_results";

    public Worker(String id, Task task) throws IOException, TimeoutException {
        this.currentTask = task;
        this.id = id;
        System.out.println();
        this.credits = 0;
        this.workResult = 0;
        this.lastObserverState = new WorkerState("Started working...", false);

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
        attachConsumerToChannelExchangeWithKey();

        //============ Call GA ============
        String jsspInstancePath = this.currentTask.getPath();
        String queue = qa_queue;
        String resultsQueue = qa_queue;
        CrossoverStrategies strategy = CrossoverStrategies.ONE;
        Logger.getLogger(this.getClass().getName()).log(Level.INFO,
                "GA is running for {0}, check queue {1}",
                new Object[]{jsspInstancePath, resultsQueue});
        GeneticAlgorithmJSSP ga = new GeneticAlgorithmJSSP(jsspInstancePath, queue, strategy);
        consumeGa();
        ga.run();

    }

    public static void main(String[] argv) throws Exception {
        String uuid = UUID.randomUUID().toString();
        new Worker(uuid, new Task("edu/ufp/inf/sd/project/data/abz5.txt", new File("edu/ufp/inf/sd/project/data/abz5.txt")));
    }

    /**
     * Binds the channel to rabbitMQ
     */
    private void bindExchangeToChannelRabbitMQ() throws IOException {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Declaring Exchange '" + this.exchangeName + "' with type " + this.exchangeType);
        this.channelToRabbitMq.exchangeDeclare(this.exchangeName, this.exchangeType);
    }

    /**
     * Creates a Consumer associated with an unnamed queue. to the exchange
     * TODO: Create a non-durable, exclusive, autodelete queue with a generated name.
     * TODO: Create binding: tell exchange to send messages to a queue; fanout exchange ignores the last parameter (binding key)
     * TODO: Consume with deliver and cancel callbacks
     */
    public void attachConsumerToChannelExchangeWithKey() {
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

                //Store the received message
                setReceivedMessage(message);
                if (message.contains("stop")) {
                    System.out.println(" [x] Consumer Tag [" + consumerTag + "] - Received '" + message + "'");
                    stopGa();
                    System.out.println("Choice: " + this.workResult);
                    String jobGroupNotification = this.id + " | " + this.workResult;
                    sendChoiceMessageToParticipants(jobGroupNotification);
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
     * @desc this method sends messages to all the participants of the exchange
     * TODO: Publish message
     * @param msgToSend
     * @throws IOException
     */
    public void sendChoiceMessageToParticipants(String msgToSend) throws IOException {
        //RoutingKey will be ignored by FANOUT exchange
        String routingKey = "";
        BasicProperties prop = MessageProperties.PERSISTENT_TEXT_PLAIN;
        System.out.println(this.id + " | Sending the work result: " + msgToSend);
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


    /**
     * @desc consumes the queue of GA results
     */
    public void consumeGa() {
        try {
            //Declare queue from which to consume (declare it also here, because consumer may start before publisher)
            this.channelToRabbitMq.queueDeclare(qa_queue_results, false, false, false, null);
            Logger.getAnonymousLogger().log(Level.INFO, Thread.currentThread().getName() + ": Will create Deliver Callback...");
            System.out.println(" [*] Waiting for messages from. To exit press CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                Logger.getAnonymousLogger().log(Level.INFO, Thread.currentThread().getName() + ": Message received " + message);
                System.out.println(" [x] Received '" + message + "'");
                if (message.contains("=")) {
                    String[] messageSplit = message.split("=");
                    setWorkResult(Integer.parseInt(messageSplit[1].replace(" ", "")));
                }
            };
            Logger.getAnonymousLogger().log(Level.INFO, Thread.currentThread().getName() + ": Register Deliver Callback...");
            //Associate callback with channel queue
            this.channelToRabbitMq.basicConsume(qa_queue_results, true, deliverCallback, consumerTag -> {
            });

        } catch (Exception e) {
            //Logger.getLogger(Recv.class.getName()).log(Level.INFO, e.toString());
            e.printStackTrace();
        }
    }


    /**
     * @desc sends a message stop to the queue of GA for it to stop
     * @throws IOException
     */
    public void stopGa() throws IOException {
        String message = "stop";
        // Publish a message to the queue (content is byte array encoded with UTF-8)
        this.channelToRabbitMq.basicPublish("", qa_queue, null, message.getBytes("UTF-8"));
        System.out.println("Stoping GA with message: " + message);
    }

    public WorkerState getLastObserverState() {
        return lastObserverState;
    }

    public void setLastObserverState(WorkerState lastObserverState) {
        this.lastObserverState = lastObserverState;
    }

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public Integer getWorkResult() {
        return workResult;
    }

    public void setWorkResult(Integer workResult) {
        this.workResult = workResult;
    }
}
