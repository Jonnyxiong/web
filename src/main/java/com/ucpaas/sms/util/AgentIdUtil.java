package com.ucpaas.sms.util;

import com.ucpaas.sms.common.util.DateUtils;
import com.ucpaas.sms.common.util.StringUtils;

import java.util.Date;

public class AgentIdUtil {
	/**
	 * @Description: 获取当前代理商id(已有当月代理商id最大值最大值)
	 * @author: Niu.T
	 * @date: 2016年12月30日 下午6:02:51
	 * @return int
	 */
	public static synchronized int getAgentId() {
		if(StaticInitVariable.AGENT_NUM == -1){
			throw  new IllegalStateException("给代理商开户时未初始化ID或没有剩余的ID可供分配");
		}
		if(StaticInitVariable.AGENTID_PRE == null){
			throw  new IllegalStateException("给代理商开户时未初始化前缀");
		}
		Date date = new Date();
		int num = 0;
		String agentIdPre = DateUtils.formatDate(date, "yyyyMM");
		if (agentIdPre.equals(StaticInitVariable.AGENTID_PRE)) {
			num = StaticInitVariable.AGENT_NUM;
			StaticInitVariable.AGENT_NUM = num + 1;
		} else {
			StaticInitVariable.AGENTID_PRE = agentIdPre;
			num = 1;
			StaticInitVariable.AGENT_NUM = num + 1;
		}
		// 拼接AgentId
		String agentId = agentIdPre + StringUtils.addZeroForNum(num, 4, "0");
		return Integer.valueOf(agentId);
	}
}
