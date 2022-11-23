package com.rebalcomb.email;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.util.Properties;

public class TLSEmail {

    /**
     *@author Alexandr Bratchyk
     * Класс для наштування власної скриньки
     */
    public  void answerToEmail(String email, String text) {
        final String fromEmail = "cryptotransferserviceserver@gmail.com"; //requires valid gmail id
        final String password = "yjltkfvtlcxhhgha"; // correct password for gmail id
//        final String toEmail = "alexbrat10@gmail.com"; // can be any email id
        final String toEmail = email; // can be any email id


//        System.out.println("TLSEmail Start");
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com"); //SMTP Host
        props.put("mail.smtp.port", "587"); //TLS Port
        props.put("mail.smtp.auth", "true"); //enable authentication
        props.put("mail.smtp.starttls.enable", "true"); //enable STARTTLS
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");


        //create Authenticator object to pass in Session.getInstance argument
        Authenticator auth = new Authenticator() {
            //override the getPasswordAuthentication method
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        };
        Session session = Session.getInstance(props, auth);

        String bodyText = "<h1> WELCOME, DEAR USER </h1> <br/>" +
                "<h2> If you wanna using our chat, please, verify your email using this code: </b>"+ text +"<b> </h2>" +
                "<br/>If you dont making verify process - write to us: <b>cryptotransferserviceserver@gmail.com</b><h3> ";
        EmailUtil.sendEmail(session, toEmail,"CryptoChat verification procseedure", bodyText);

    }


}