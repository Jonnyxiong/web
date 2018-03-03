/**    
 * @Title: AccountService.java  
 * @Package: com.ucpaas.sms.service.account
 * @Description: TODO
 * @author: Niu.T    
 * @date: 2016年9月23日 下午2:24:20  
 * @version: V1.0    
 */
package com.ucpaas.sms.service;

import com.jsmsframework.common.dto.ResultVO;
import com.ucpaas.sms.model.AgentApplyVO;

/**  
 * @ClassName: AccountService  
 * @Description: 账户管理Service:(1)账户资料(2)账户安全
 * @author: Niu.T 
 * @date: 2016年9月23日 下午2:24:20  
 */
public interface UserCenterService {

	/**
	 * @Description: 获取用户账户的基本信息(根据clientId)
	 * @author: Niu.T 
	 * @date: 2016年9月23日 下午2:33:24  
	 * @return: ResultVO
	 */
	ResultVO agentRegister(AgentApplyVO agentApplyVO) ;
	

}
