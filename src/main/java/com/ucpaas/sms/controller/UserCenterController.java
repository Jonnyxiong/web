/**    
 * @Title: AccountController.java  
 * @Package: com.ucpaas.sms.controller.account
 * @Description: TODO
 * @author: Niu.T    
 * @date: 2016年9月23日 上午11:20:31  
 * @version: V1.0    
 */
package com.ucpaas.sms.controller;

import com.jsmsframework.common.dto.ResultVO;
import com.ucpaas.sms.exception.OperationException;
import com.ucpaas.sms.model.AgentApplyVO;
import com.ucpaas.sms.service.CommonService;
import com.ucpaas.sms.service.UserCenterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @ClassName: AccountController
 * @Description: 官网&客户平台 -> 账户管理Controller
 * @author: Niu.T
 * @date: 2016年9月23日 上午11:20:31
 */
@Controller
@RequestMapping("/userCenter")
// url:/模块/{id}/细分/list
public class UserCenterController extends BaseController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private UserCenterService userCenterService;

	@Autowired
	private CommonService commonService;

	/**
	 * @Description: 账户资料 -> 修改基本资料
	 * @author: Niu.T
	 * @date: 2016年9月23日 上午11:59:44
	 * @return: Map<String,Object>
	 */
	@RequestMapping(value = "/agentRegister", method = RequestMethod.POST)
	@ResponseBody
	public ResultVO agentRegister(@ModelAttribute("agentApplyVO") AgentApplyVO agentApplyVO) {

		// 1. 调用service,进行业务处理
		ResultVO resultVO;
		try {
			resultVO = userCenterService.agentRegister(agentApplyVO);
		} catch (OperationException e) {
			resultVO = ResultVO.failure(e.getMessage());
			logger.error("客户注册异常 ---> {}",e);
		}
		return resultVO;

	}


}
