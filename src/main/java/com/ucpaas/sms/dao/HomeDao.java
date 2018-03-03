package com.ucpaas.sms.dao;

import com.ucpaas.sms.model.AgentApplyVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class HomeDao {
	
	@Autowired
	private MasterDao masterDao;
	
	public int agentApply(AgentApplyVO agentApplyVO){
		return masterDao.insert("home.insertAgentApply", agentApplyVO);
	}
	
	public Map<String, Object> agentApplyCheckInApply(AgentApplyVO agentApplyVO) {
		return masterDao.selectOne("home.agentApplyCheckInApply", agentApplyVO);
	}
	
	public Map<String, Object> agentApplyCheckInUser(AgentApplyVO agentApplyVO){
		return masterDao.selectOne("home.agentApplyCheckInUser", agentApplyVO);
	}

	public Map<String, Object> oemAgentApplyCheckInUser(AgentApplyVO agentApplyVO){
		return masterDao.selectOne("home.oemAgentApplyCheckInUser", agentApplyVO);
	}

}
