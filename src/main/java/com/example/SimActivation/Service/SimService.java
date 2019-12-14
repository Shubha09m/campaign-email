package com.example.SimActivation.Service;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.SimActivation.Model.CampaignDetails;
import com.example.SimActivation.Model.CampaignStatus;
import com.example.SimActivation.Model.EmailDetails;
import com.example.SimActivation.Model.FetchCampaignDetails;
import com.example.SimActivation.Model.UploadResult;
import com.example.SimActivation.Repository.EmailRepository;

@Service
public class SimService {
	
	
	EmailDetails emaildetails = new EmailDetails() ;
	@Autowired
	EmailRepository emailRepository;
	@Autowired
    private JdbcTemplate jdbcTemplate;
	@Autowired
    JavaMailSender javaMailSender;
	
	
	
	ArrayList<String> detail= new ArrayList<String>();
	public void sendEmail(MultipartFile file) throws IOException, SQLException {
		String path="File.xlsx";
		File convFile = new File("D:/"+"/"+path);
	    file.transferTo(convFile);
	    
	    
	    String fileName="D:\\File.xlsx";
	    read(fileName);
	    saveToDB();
    	//saveToDatabase(dataHolder);
	}
	
	public void saveDetails(EmailDetails details) {
		emailRepository.save(details);
	}
	
	
	public  void read(String fileName) throws FileNotFoundException, IOException, SQLException {
		String jdbcURL = "jdbc:mysql://localhost:3306/EmailCamp";
        String username = "root";
        String password = "root";
        Connection connection = null;
		XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(fileName));
		XSSFSheet sheet = workbook.getSheetAt(0);
	
		connection = DriverManager.getConnection(jdbcURL, username, password);
		connection.setAutoCommit(false);
		
		String sql = "INSERT INTO user_details (email_id,Age,Name,city,country,State,zipcode,SUbscribe_status,property_ids ) VALUES (?,?,?,?,?,?,?,?,?)";
        PreparedStatement statement = connection.prepareStatement(sql); 
        int count = 0;
        int batchSize=20;

        Iterator<Row> rowIterator = sheet.iterator();
	    rowIterator.next(); 
            while (rowIterator.hasNext()) {
                Row nextRow = rowIterator.next();
                Iterator<Cell> cellIterator = nextRow.cellIterator();
 
                while (cellIterator.hasNext()) {
                    Cell nextCell = cellIterator.next();
 
                    int columnIndex = nextCell.getColumnIndex();
 
                    switch (columnIndex) {
                    case 0:
                        String email_id = nextCell.getStringCellValue();
                        statement.setString(1,email_id);
                        break;
                    case 1:
                    	int Age =  (int) nextCell.getNumericCellValue();
                        statement.setInt(2,Age);
                        break;
                    case 2:
                    	String Name = nextCell.getStringCellValue();
                        statement.setString(3,Name);
                        break;
                        
                    case 3:
                    	String city = nextCell.getStringCellValue();
                        statement.setString(4,city);
                        break;
                        
                    case 4:
                    	String country= nextCell.getStringCellValue();
                        statement.setString(5,country );
                        break;
                        
                    case 5:
                    	String State= nextCell.getStringCellValue();
                        statement.setString(6,State);
                        break;
                        
                    case 6:
                    	int zipcode  =  (int)nextCell.getNumericCellValue();
                        statement.setInt(7,zipcode );
                        break;
                        
                    case 7:
                    	String SUbscribe_status = nextCell.getStringCellValue();
                        statement.setString(8,SUbscribe_status);
                        break;
                        
                    case 8:
                    	String property_ids =  nextCell.getStringCellValue();
                    	System.out.println(property_ids);
                        statement.setString(9,property_ids);
                        break;
                        
                    
             
                    }
 
                }
            
                statement.addBatch();
                
                if (count % batchSize == 0) {
                    statement.executeBatch();
                }      
        }
            statement.executeBatch();
            
            connection.commit();
            connection.close();
	    
        workbook.close();
        emailRepository.propertyMapping();
	    
	}
    
	
	public void saveToDB() {
		for (int i=0 ; i< detail.size();i++ ) {
			System.out.println(detail);
		}
		
	}
	
	public CampaignStatus addCampaign(CampaignDetails campaignDetails) {
		String msg = "insert success in DB";
		CampaignStatus campaignStatus = new CampaignStatus();
		campaignStatus.setStatus(msg);
		try {
			
			
			jdbcTemplate.update(
	                "insert into campaign_master (title,template,campaign_date,company_name, perference_target_ids) values (?,?,?,?,?)",
	                campaignDetails.getTitle(),campaignDetails.getTemplate(),campaignDetails.getCreatedDate(),
	                campaignDetails.getCompany(),
	                campaignDetails.getPerference_target_ids());
			String id =jdbcTemplate.queryForObject("select campaign_id  from campaign_master where title=?",new Object[]{campaignDetails.getTitle()}, String.class);
			//System.out.println(id);
			campaignStatus.setCampaignId(id);
			
			campMapping();
			
			
			
		} catch(Exception e) {
			msg = e.getMessage();
			campaignStatus.setStatus(msg);
		}
		return campaignStatus;
	}
	
	public String sendMail(CampaignDetails campaignDetails) throws MessagingException {
		
		
		
		int id =jdbcTemplate.queryForObject("select campaign_id from campaign_master order by campaign_id limit 1", int.class);
		
		String sql="select distinct e.email_id,e.Name from property_mapping u ,user_details e where u.email_id=e.email_id and e.SUbscribe_status='Y' and  u.property_id in (select property_id from \r\n" + 
				"campaign_mapping where campaign_id=?)";
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, new Object[]{id});
		for(Map<String, Object> row:rows){
			
			String name=(String) row.get("Name");
			String email_id=(String)row.get("email_id");
			
			
			//int noOfUploadTasks = jdbcTemplate.queryForObject("select Count(Distinct email_id)  from user_details",int.class);
			int noOfUploadTasks=1;
			ExecutorService exServicePool = Executors.newFixedThreadPool(noOfUploadTasks);
			
			
			for(int i=1; i<=noOfUploadTasks; i++) {
				String placeHolder = name;
				String html_format=campaignDetails.getTemplate();
				String replaced = html_format.replaceAll("name_sendmail", placeHolder);
				
				String replaced_email = replaced.replaceAll("email_sendmail","href=\"https://new-project-9tcbqz.stackblitz.io?email=" +email_id + "\"");
				System.out.println(replaced_email);
				MimeMessage msg = javaMailSender.createMimeMessage();
		        MimeMessageHelper helper = new MimeMessageHelper(msg, true);
		        helper.setBcc(email_id); // should fetch from DB
		        String subject =jdbcTemplate.queryForObject("select title from campaign_master", String.class);
		        System.out.println(subject);
		        helper.setSubject(subject); // fetch from DB
		        helper.setText(replaced, true);
		        helper.setText(replaced_email, true);
		        
		        UploadResult uploadRunnable  = new UploadResult(helper, javaMailSender,msg);
		        exServicePool.execute(uploadRunnable);
			}
		}
		
		
		return "mailSend";
}
	
	public void  campMapping() {
		String sql = "Select campaign_id,perference_target_ids from campaign_master";
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		for(Map<String, Object> row:rows){
			String perference_target_ids=(String)row.get("perference_target_ids");
			int campaign_id=(int)row.get("campaign_id");
			String[] property=perference_target_ids.split(",");
			
			for(String s:property) {
				jdbcTemplate.update(
		                "insert into campaign_mapping (campaign_id , property_id ) values(?,?)",
		                campaign_id, Integer.parseInt(s));
			}
			
		}
	}
	
	
	
}
							
	
	
	

	
		
	
	

		
	
         
	
	
	


	
	
	
	


