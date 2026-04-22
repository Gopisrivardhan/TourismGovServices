package com.tourismgov.notification.service;



public interface EmailService {
   
    void sendNotificationEmail(String to,String name, String subject, String body);
}
