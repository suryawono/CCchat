/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package mq;

import chat.Server;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.qpid.jms.*;
import javax.jms.*;

public abstract class Listener implements Runnable {

    public String destinationName,uid;

    public Listener(String destinationName,String uid) {
        this.destinationName = destinationName;
        this.uid=uid;
    }

    private static String env(String key, String defaultValue) {
        String rc = System.getenv(key);
        if (rc == null) {
            return defaultValue;
        }
        return rc;
    }

    private static String arg(String[] args, int index, String defaultValue) {
        if (index < args.length) {
            return args[index];
        } else {
            return defaultValue;
        }
    }

    public abstract void extractor(String s);

    @Override
    public void run() {
        try {
            final String TOPIC_PREFIX = "topic://";

            String user = env("ACTIVEMQ_USER", "admin");
            String password = env("ACTIVEMQ_PASSWORD", "password");
            String host = env("ACTIVEMQ_HOST", "localhost");
            int port = Integer.parseInt(env("ACTIVEMQ_PORT", "5672"));

            String connectionURI = "amqp://" + host + ":" + port;

            JmsConnectionFactory factory = new JmsConnectionFactory(connectionURI);

            Connection connection = factory.createConnection(user, password);
            connection.setClientID(uid);
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            MessageConsumer consumer;
            if (destinationName.startsWith(TOPIC_PREFIX)) {
                Topic destination = session.createTopic(destinationName.substring(TOPIC_PREFIX.length()));
                consumer = session.createDurableSubscriber(destination, uid);
            } else {
                Destination destination = session.createQueue(destinationName);
                consumer = session.createConsumer(destination);
            }
            while (true) {
                Message msg = consumer.receive();
                if (msg instanceof TextMessage) {
                    String body = ((TextMessage) msg).getText();
                    this.extractor(body);
                } else {
                    System.out.println("Unexpected message type: " + msg.getClass());
                }
            }
        } catch (JMSException ex) {
            Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
