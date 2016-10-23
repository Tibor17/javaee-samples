/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package app;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

import static javax.mail.Message.RecipientType.TO;

public final class Main {
    private Main() {
    }

    @SuppressWarnings("checkstyle:hideutilityclassconstructor")
    public static void main(String... args) throws Exception {

        // https://nilhcem.github.io/FakeSMTP/index.html
        // https://github.com/Nilhcem/FakeSMTP
        // https://www.hmailserver.com/download

        Properties props = new Properties();
        props.setProperty("mail.smtp.host", "mailserver"); // 10.197.200.18
        props.setProperty("mail.smtp.port", "25"); // 25 is POP3
        props.setProperty("mail.smtp.user", "<smtp server account name>"); // surname.name
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.timeout", "" + 60 * 1000);
        props.setProperty("mail.transport.protocol", "smtp");

        Session session = Session.getInstance(props, null);
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("< FROM: e-mail >", "Example.com Admin"));
        msg.addRecipient(TO, new InternetAddress("< TO: e-mail >", "Mr. User"));
        msg.setSubject("WTF");
        msg.setText("Hi There!");
        // Transport.send(msg); do not use
        // use session.getTransport("smtp") unless props.setProperty("mail.transport.protocol", "smtp");
        Transport tr = session.getTransport();
        tr.connect("mailserver", "<smtp server account name>", "<smtp server account password>");
        msg.saveChanges();
        tr.sendMessage(msg, msg.getAllRecipients());
        tr.close();
    }
}
