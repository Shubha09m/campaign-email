package com.example.SimActivation.Service;

import java.io.*;
import java.sql.*;
import java.util.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.SimActivation.Model.CampaignDetails;
import com.example.SimActivation.Model.CampaignStatus;
import com.example.SimActivation.Model.EmailDetails;
import com.example.SimActivation.Repository.EmailRepository;

@Service
public class SimService {
	
	
	EmailDetails emaildetails = new EmailDetails() ;
	@Autowired
	EmailRepository emailRepository;
	@Autowired
    private JdbcTemplate jdbcTemplate;
	
	
	
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
		
		String sql = "INSERT INTO user_details (email_id,Age,Name,city,country,State,zipcode,SUbscribe_status ) VALUES (?,?, ?,?,?,?,?,?)";
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
		} catch(Exception e) {
			msg = e.getMessage();
			campaignStatus.setStatus(msg);
		}
		return campaignStatus;
	}
	
}
							
	
	
	

	
		
	
	

		
	
         
	
	
	


	
	
	
	


