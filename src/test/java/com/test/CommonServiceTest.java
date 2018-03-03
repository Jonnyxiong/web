package com.test;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ucpaas.sms.service.CommonService;

public class CommonServiceTest extends SupperTest{
	
	@Autowired
	private CommonService commonService;
	
	
	@Test
	public void getExistTableForSplitTableTest() throws Exception{
		
		/*String startDate = "";
		String endDate = "";
		
		List<String> data = commonService.getExistTableForSplitTable(DbTablePrefix.T_SMS_ACCESS_YYYYMMDD, startDate, endDate);
		
		System.out.println("-------------->"+data.toString());*/
		
	}

}
