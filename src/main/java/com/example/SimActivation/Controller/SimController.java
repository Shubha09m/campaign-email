package com.example.SimActivation.Controller;


import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import com.example.SimActivation.Model.FetchCampaignDetails;
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
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	
	
	@CrossOrigin(origins = "*")
	@GetMapping(value="/")
	public String Health() {
		return "working";
	}
	
	@CrossOrigin(origins = "*")
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
	@CrossOrigin(origins = "*")
	@RequestMapping(value="/properties",method=RequestMethod.GET)
	public List<CampaignDetail> returnCampaignDetail() {
		//System.out.println(emailRepository);
		return emailRepository.getCampaignDetails();
	}
	
	@CrossOrigin(origins = "*")
	@RequestMapping(value = "/campaign", method = RequestMethod.POST)
	public CampaignStatus addSubscriber(@RequestBody CampaignDetails campaignDetails) throws MessagingException {
		String status = "Not send";
		CampaignStatus campaignStatus=simService.addCampaign(campaignDetails);
		if(campaignStatus.getStatus()=="insert success in DB") {
			String result=simService.sendMail(campaignDetails);
			if(result=="mailSend") {
				campaignStatus.setStatus("Send success");
				return campaignStatus;
			}
		}
		campaignStatus.setStatus(status);
		return campaignStatus;
	}
	
	@CrossOrigin(origins = "*")
	@RequestMapping(value = "/unSubcribe/{email_id}", method = RequestMethod.POST)
	public String unSubcribe(@PathVariable(value="email_id") String email_id) {
		String msg = "Failed To Unsubscribe";
		try {
		jdbcTemplate.update("update user_details set SUbscribe_status='N' where email_id=? AND SUbscribe_status='Y'",email_id);
		msg = "Unsubscribed Successfully";
		}catch(Exception e) {
			e.printStackTrace();
			return msg;
		}
		return msg;
		
		
	}
	
	@CrossOrigin(origins = "*")
	@RequestMapping(value = "/get", method = RequestMethod.GET)
	public FetchCampaignDetails dashboard() {
		FetchCampaignDetails f = new FetchCampaignDetails();
		String sql="select campaign_id ,title ,company_name , l.total_mail_send,n.Un_Subscribe_count from campaign_master ,(select count(distinct email_id) as total_mail_send from user_details) l,(select count(distinct SUbscribe_status) as Un_Subscribe_count from user_details where SUbscribe_status='N') n  limit 1";
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		for(Map<String, Object> row:rows) {
			f.setId((int)row.get("campaign_id"));
			f.setTitle((String)row.get("title"));
			f.setCompany((String)row.get("company_name"));
			f.setInterestedRecepientsCount((long)row.get("total_mail_send"));
			f.setAvailableRecepientsCount((long)row.get("Un_Subscribe_count"));
		}
		return f;
	}
	
	
}
