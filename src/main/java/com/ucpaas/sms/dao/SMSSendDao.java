package com.ucpaas.sms.dao;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SMSSendDao {
	
	@Autowired
	private MasterDao masterDao;
	@Autowired
	private AccessDao accessDao;
	
	public Map<String, Object> smsExperienceCheck(Map<String, String> params){
		return accessDao.selectOne("smsSend.smsExperienceCheck", params);
	}
	
	public int insertExpRecord(Map<String, String> params){
		return accessDao.insert("smsSend.insertExpRecord", params);
	}
	
	public int smsAccUsableCheck(Map<String, String> params){
		return masterDao.selectOne("smsSend.smsAccUsableCheck", params);
	}
	
	public int getClientPaytype(String clientId){
		return masterDao.selectOne("smsSend.getClientPaytype", clientId);
	}
	
	public Map<String, Object> getClientAccountStatus(String clientId){
		return masterDao.selectOne("smsSend.getClientAccountStatus", clientId);
	}
	

}
