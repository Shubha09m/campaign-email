package com.example.SimActivation.Controller;


import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.SimActivation.Model.CampaignDetail;
import com.example.SimActivation.Model.CampaignDetails;
import com.example.SimActivation.Model.CampaignStatus;
import com.example.SimActivation.Model.EmailDetails;
import com.example.SimActivation.Repository.EmailRepository;
import com.example.SimActivation.Service.SimService;

@RestController
public class SimController {
	
	MultipartFile file;
	
	@Autowired
	SimService simService;
	
	@Autowired
    JavaMailSender javaMailSender;
	
	@Autowired
	 Environment env;
	
	@Autowired
	EmailRepository emailRepository;
	
	
	
	
	@GetMapping(value="/")
	public String Health() {
		return "working";
	}
	
	@PostMapping(path = "/uploads")
	public String upload(@RequestParam("file") MultipartFile file) throws IOException, SQLException {
		this.file= file;
		simService.sendEmail(file);
		return "file";
	}
	
	@PostMapping(path = "/details")
	public void upload(@RequestBody EmailDetails emailDetails) throws IOException {
		simService.saveDetails(emailDetails);
		
	}
	
	@RequestMapping(value="/sendMAil", method=RequestMethod.GET)
	public String sendMail() throws MessagingException {
		String placeHolder = "Souvik"; // fetch from DB
		//String replaced = htmlFormat.replaceAll("name_sendmail", placeHolder);
		MimeMessage msg = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true);
        helper.setBcc(new String[] {"souvik.c287@gmail.com","souravkhoso1@gmail.com"}); // should fetch from DB
        helper.setSubject("Testing from Spring Boot"); // fetch from DB

        //helper.setText(replaced, true);
        javaMailSender.send(msg);
		return "mailSend";
	}
	
	@RequestMapping(value="/properties",method=RequestMethod.GET)
	public List<CampaignDetail> returnCampaignDetail() {
		//System.out.println(emailRepository);
		return emailRepository.getCampaignDetails();
	}
	
	@RequestMapping(value = "/campaign", method = RequestMethod.POST)
	public CampaignStatus addSubscriber(@RequestBody CampaignDetails campaignDetails) {
		return simService.addCampaign(campaignDetails);
	}
}
