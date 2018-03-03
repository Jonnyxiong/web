package com.ucpaas.sms.service;

import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.enums.WebId;
import com.ucpaas.sms.model.PageBean;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.model.param.PageParam;

import java.util.List;
import java.util.Map;

public interface BussinessService {
	
	PageContainer queryDayStat(Map<String, Object> params) throws Exception;
	
	PageContainer queryMonthStat(Map<String, Object> params) throws Exception;
	
	PageContainer querySmsSendRecord(Map<String, Object> params) throws Exception;
	
	List<Map<String, Object>> querySmsSendRecordCount(Map<String, Object> params) throws Exception;
	
	List<Map<String, Object>> querySmsRecord4Excel(Map<String, Object> params) throws Exception;
	
	PageBean queryInterShortMessage(PageParam pageParam,String countryInfo);

	PageContainer queryStatistic(Map<String, Object> params) throws Exception;

	Map queryStatisticTotal(Map<String, Object> formData) throws Exception;

	List<Map<String, Object>> exportStatistics(Map<String, Object> formData) throws Exception;

	// 品牌
	List<Map<String,Object>> exportBussinessData(Map<String, Object> queryParams);

	// 品牌
	List<Map<String,Object>> exportMonthStat(Map params);

	// 品牌
	int exportBussinessDataCount(Map<String, Object> queryParams);

	JsmsPage smsTimerSendQuery(JsmsPage jsmsPage, WebId webId, String agengId);
}
