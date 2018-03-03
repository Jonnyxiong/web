package com.ucpaas.sms.dao;

import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.model.SmsClientTariff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class BussinessDao {

	@Autowired
	private MasterDao masterDao;
	
	@Autowired
	private AccessDao accessDao;
	
	public PageContainer queryDayStatBr(Map<String, Object> params) throws Exception {
		return accessDao.getSearchPage("bussiness.queryDayStatBr", "bussiness.queryDayStatCountBr", params);
	}

	public PageContainer queryDayStatOem(Map<String, Object> params) throws Exception {
		return accessDao.getSearchPage("bussiness.queryDayStatOem", "bussiness.queryDayStatCountOem", params);
	}

	public PageContainer queryMonthStatBr(Map<String, Object> params) throws Exception {
		return accessDao.getSearchPage("bussiness.queryMonthStatBr", "bussiness.queryMonthStatCountBr", params);
	}

	public PageContainer queryMonthStatOem(Map<String, Object> params) throws Exception {
		return accessDao.getSearchPage("bussiness.queryMonthStatOem", "bussiness.queryMonthStatCountOem", params);
	}
	
	public PageContainer querySmsRecordPageBr(Map<String, Object> params) throws Exception {
		return accessDao.getSearchPage("bussiness.querySmsSendRecordBr", "bussiness.querySmsSendRecordCount", params);
	}

	public PageContainer querySmsRecordPageOem(Map<String, Object> params) throws Exception {
		return accessDao.getSearchPage("bussiness.querySmsSendRecordOem", "bussiness.querySmsSendRecordCount", params);
	}
	
	public List<Map<String, Object>> querySmsSendRecordCount(Map<String, Object> params) throws Exception{
		return accessDao.selectList("bussiness.querySmsSendRecordCount", params);
	}


	public List<Map<String, Object>> querySmsRecord4ExcelBr(Map<String, Object> params) throws Exception {
		return accessDao.getSearchList("bussiness.querySmsSendRecord4ExcelBr", params);
	}


	public List<Map<String, Object>> querySmsRecord4ExcelOem(Map<String, Object> params) throws Exception {
		return accessDao.getSearchList("bussiness.querySmsSendRecord4ExcelOem", params);
	}

	public List<SmsClientTariff> querySmsClientTariffList(Map<String,Object> sqlParams){
		return masterDao.selectList("smsClientTariffMapper.querySmsClientTariffList", sqlParams);
	}
	public List<Map<String,Object>> querySmsClientTariffMapList(Map<String,Object> sqlParams){
		return masterDao.getSearchList("smsClientTariffMapper.querySmsClientTariffMapList", sqlParams);
	}
	
	public Integer querySmsClientTariffListCount(Map<String,Object> sqlParams){
		return masterDao.selectOne("smsClientTariffMapper.querySmsClientTariffListCount", sqlParams);
	}

	public PageContainer queryStatisticBr(Map<String, Object> params) {
		return accessDao.getSearchPage("bussiness.queryStatisticBr", "bussiness.queryStatisticCountBr", params);
	}
	public PageContainer queryStatisticZk(Map<String, Object> params) {
		return accessDao.getSearchPage("bussiness.queryStatisticZk", "bussiness.queryStatisticCountZk", params);
	}

	public PageContainer queryStatisticOem(Map<String, Object> params) {
		return accessDao.getSearchPage("bussiness.queryStatisticOem", "bussiness.queryStatisticCountOem", params);
	}

	public Map queryStatisticTotalOem(Map<String, Object> formData) {
		return accessDao.selectOne("bussiness.queryStatisticTotalOem", formData);
	}

	public Map queryStatisticTotalBr(Map<String, Object> formData) {
		return accessDao.selectOne("bussiness.queryStatisticTotalBr", formData);
	}
	public Map queryStatisticTotalZk(Map<String, Object> formData) {
		return accessDao.selectOne("bussiness.queryStatisticTotalZk", formData);
	}

}
