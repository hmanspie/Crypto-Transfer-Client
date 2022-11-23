package com.rebalcomb.email;

import com.rebalcomb.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;

/**
 * @author Alexandr Bratchyk
 * Клас для роботи з email. А також формату вихідного листа
 */
public class EmailUtil {

    private static final Logger logger = LogManager.getLogger(EmailUtil.class);

    /**
     * @author Alexandr Bratchyk
     * Utility method to send simple HTML email
     * @param session
     * @param toEmail
     * @param subject
     * @param body
     */
    public static void sendEmail(Session session, String toEmail, String subject, String body){
        try
        {
            MimeMessage msg = new MimeMessage(session);
            //set message headers
            msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
            msg.addHeader("format", "flowed");
            msg.addHeader("Content-Transfer-Encoding", "8bit");


            msg.setFrom(new InternetAddress("CryptoChat@manager.com", "CryptoChat [NO REPLY]"));

            msg.setReplyTo(InternetAddress.parse("no_reply@example.com", false));

            msg.setSubject(subject, "UTF-8");

//            msg.setText(body, "UTF-8");

            msg.setSentDate(new Date());

            msg.setContent(body,"text/html");

            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
//            System.out.println("Message is ready");
            Transport.send(msg);
            logger.info("Message send successful");
            //System.out.println("EMail Sent Successfully!!");
        }
        catch (Exception e) {
            logger.error("No internet connection, message dont send");
        }
    }
}