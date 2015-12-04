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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.qpid.jms.*;
import javax.jms.*;

public class Publisher {

    public String destinationName;
    private MessageProducer producer;
    private Session session;

    public Publisher(String destinationName) {

        try {
            final String TOPIC_PREFIX = "topic://";

            String user = env("ACTIVEMQ_USER", "admin");
            String password = env("ACTIVEMQ_PASSWORD", "admin");
            String host = env("ACTIVEMQ_HOST", "localhost");
            int port = Integer.parseInt(env("ACTIVEMQ_PORT", "5672"));

            String connectionURI = "amqp://" + host + ":" + port;
            this.destinationName = destinationName;

            JmsConnectionFactory factory = new JmsConnectionFactory(connectionURI);

            Connection connection = factory.createConnection(user, password);
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Destination destination = null;
            if (destinationName.startsWith(TOPIC_PREFIX)) {
                destination = session.createTopic(destinationName.substring(TOPIC_PREFIX.length()));
            } else {
                destination = session.createQueue(destinationName);
            }

            producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        } catch (JMSException ex) {
            Logger.getLogger(Publisher.class.getName()).log(Level.SEVERE, null, ex);
        }
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

    public void add(String msg) {
        try {
            TextMessage tm = session.createTextMessage(msg);
            producer.send(tm);
        } catch (JMSException ex) {
            Logger.getLogger(Publisher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
