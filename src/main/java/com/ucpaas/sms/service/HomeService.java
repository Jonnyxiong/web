package com.ucpaas.sms.service;

import com.ucpaas.sms.model.AgentApplyVO;

import java.util.Map;

public interface HomeService {

	Map<String, Object> smsExperience(String mobile, String ip) throws Exception;
	
	Map<String, Object> agentApply(AgentApplyVO agentApplyVO) throws Exception;
}
