package com.ucpaas.sms.service;

import com.ucpaas.sms.constant.Constant.DbTablePrefix;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface CommonService {
	
	List<String> getExistTable(DbTablePrefix dbTablePrefix, String startDate, String endDate) throws Exception;
	
	List<String> getExistTable(DbTablePrefix dbTablePrefix, String startDate, String endDate,String clientid) throws Exception;
	
//	List<SplitTableTime> getSplitTableTime(String start_time, String end_time) throws Exception;
	
	List<String> getExistTableForSplitTable(DbTablePrefix dbTablePrefix, String startDate, String endDate,String clientid) throws Exception;
	
	void randCheckCodePic(HttpServletRequest request, HttpServletResponse response) throws Exception;
	
	boolean randCodeCheck(HttpServletRequest request) throws Exception;
	
	Map<String, Object> clientAccountStatusCheck(String clientId) throws Exception;
	
	Map<String,Object> isExistOauthPicInfo(String client_id);

	/**
	 * @Description: 获取当月代理商账号最大值
	 * @author: Niu.T
	 * @date: 2016年12月30日    下午4:08:29
	 * @param agentIdPre
	 * @return String
	 */
	String getMostAgentNumForMonth(String agentIdPre);
}

