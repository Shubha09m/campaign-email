package com.example.SimActivation.Model;

import javax.mail.internet.MimeMessage;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

public class UploadResult implements Runnable {
	MimeMessageHelper helper;
	JavaMailSender javaMailSender;
	MimeMessage msg;
	
	public UploadResult(MimeMessageHelper helper, JavaMailSender javaMailSender, MimeMessage msg) {
		this.helper=helper;
		this.javaMailSender=javaMailSender;
		this.msg=msg;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		javaMailSender.send(msg);
		
		 try {
	            Thread.sleep(2000);
	        } catch(InterruptedException e) { System.out.println(e.getMessage()); }
	        System.out.println(Thread.currentThread().getName() + " (End)");  
	    }
	
}
