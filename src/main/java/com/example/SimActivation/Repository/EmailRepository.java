package com.example.SimActivation.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;

import com.example.SimActivation.Model.CampaignDetail;
import com.example.SimActivation.Model.EmailDetails;

@Repository
public class EmailRepository {
	
	@Autowired
    private JdbcTemplate jdbcTemplate;
	
	public int save(EmailDetails details) {
        return jdbcTemplate.update(
                "insert into emaildetails (name, email_id) values(?,?)",
                details.getName(), details.getEmailDetails());
        
     

}
	
	public List<CampaignDetail> getCampaignDetails() {
		String sql="Select * from property_master";
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		List<CampaignDetail> result = new ArrayList<CampaignDetail>();
		for(Map<String, Object> row:rows){
			CampaignDetail cam = new CampaignDetail();
			cam.setId((int)row.get("property_id"));
			cam.setName((String)row.get("property_desc"));
			result.add(cam);
		}
		return result;
		
		
	}
	
	public void propertyMapping() {
		String sql = "Select email_id,property_ids from user_details";
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		HashMap<String, String> hmap = new HashMap<String, String>();
		for(Map<String, Object> row:rows){
			String property_ids=(String)row.get("property_ids");
			String email_id=(String)row.get("email_id");
			String[] property=property_ids.split(",");
			
			for(String s:property) {
				jdbcTemplate.update(
		                "insert into property_mapping (email_id , property_id ) values(?,?)",
		                email_id, Integer.parseInt(s));
			}
			
		}
		

		
	}
}
