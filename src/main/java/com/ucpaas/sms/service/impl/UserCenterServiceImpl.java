/**    
 * @Title: AccountServiceImpl.java  
 * @Package: com.ucpaas.sms.service.account.impl
 * @Description: TODO
 * @author: Niu.T    
 * @date: 2016年9月23日 下午2:47:12  
 * @version: V1.0    
 */
package com.ucpaas.sms.service.impl;

import com.jsmsframework.common.dto.ResultVO;
import com.jsmsframework.common.util.StringUtils;
import com.ucpaas.sms.common.util.MD5;
import com.ucpaas.sms.common.util.SecurityUtils;
import com.ucpaas.sms.dao.HomeDao;
import com.ucpaas.sms.dao.MasterDao;
import com.ucpaas.sms.exception.OperationException;
import com.ucpaas.sms.model.AgentApplyVO;
import com.ucpaas.sms.service.EmailService;
import com.ucpaas.sms.service.UserCenterService;
import com.ucpaas.sms.util.AgentIdUtil;
import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**  
 * @ClassName: AccountServiceImpl  
 * @Description: 账户管理Service:(1)账户资料(2)账户安全
 * @author: Niu.T 
 * @date: 2016年9月23日 下午2:47:12  
 */
@Service
public class UserCenterServiceImpl implements UserCenterService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	HomeDao homeDao;
	@Autowired
	private MasterDao masterDao;
	@Autowired
	private EmailService emailService;


	/**
	 * 获取用户账户的基本信息(根据clientId)
	 */
	@Override
	@Transactional
	public ResultVO agentRegister(AgentApplyVO agentApplyVO){
		Map<String, Object> checkInApplyTable = homeDao.agentApplyCheckInApply(agentApplyVO);
		ResultVO checkResult = checkEmailAndMobileEnable(checkInApplyTable);
		if(checkResult != null){
			return checkResult;
		}

		Map<String, Object> checkInUserTable = homeDao.oemAgentApplyCheckInUser(agentApplyVO);
		checkResult = checkEmailAndMobileEnable(checkInUserTable);
		if(checkResult != null){
			return checkResult;
		}
		//status 0:待受理,1:受理不通过,2:已受理
		agentApplyVO.setStatus(2);
		int insertNum = homeDao.agentApply(agentApplyVO);
		if(insertNum < 1){
			return ResultVO.failure("申请代理商失败,请联系客服");
		}
		Map<String, String> params = new HashMap<>();
		params.put("productNum", "0");
		params.put("productType", "0");
		params.put("productId", null);
		params.put("email", agentApplyVO.getEmail());
		params.put("belongSale", ConfigUtils.default_belongSale);// todo 平台bu
		params.put("applyId", agentApplyVO.getId());
		params.put("domain", ConfigUtils.oem_domain_name);
		params.put("copyright", ConfigUtils.default_copyright);
		params.put("nav_backcolor", ConfigUtils.nav_backcolor);
		params.put("nav_text_color", ConfigUtils.nav_text_color);

		return confirmAccept(params);
	}

	private ResultVO checkEmailAndMobileEnable(Map<String, Object> map){
		if(null != map){
			String email = Objects.toString(map.get("email"), "");
			String mobile = Objects.toString(map.get("mobile"), "");
			if(StringUtils.isNoneBlank(email)){
                ResultVO failure = ResultVO.failure("邮箱已经被注册");
                failure.setCode(-1);
                return failure;
			}
			if(StringUtils.isNoneBlank(mobile)){
                ResultVO failure = ResultVO.failure("手机已经被注册");
                failure.setCode(-2);
                return failure;
			}
		}
		return null;
	}


	private ResultVO confirmAccept(Map<String, String> params) {

		logger.debug("oem确认受理,方法--->{},参数--->{}================","oemConfirmAccept",params.toString());
		ResultVO resultVO = ResultVO.failure();

//		Map<String,Object> data = new HashMap<String,Object>();
//		int i = masterDao.getOneInfo("userCenter.checkAgentApplyStatusById", params);
//		if(i == 0){
//			throw new OperationException("操作失败！该记录已经被处理");
//		}
//
//		params.put("status","2");//申请状态,0:待受理,1:受理不通过,2:已受理
//		int updateNum = masterDao.update("userCenter.updateApplyInfoById", params);
//		if(updateNum == 0){
//			throw new OperationException("更新申请表失败！");
//		}

//		Long userId = Long.valueOf(params.get("userId"));
//		String pageUrl = params.get("pageUrl");
//		String ip = params.get("ip");
		String password = UUID.randomUUID().toString().replace("-", "").substring(4,12);
		params.put("password", password);

		StringBuilder agentIdSb = new StringBuilder("");
		//生成用户、生成代理商、生成代理商账户、管理代理商角色
		params.put("agent_type","5");//代理商类型,1:销售代理商,2:品牌代理商,3:资源合作商,4:代理商和资源合作,5:OEM代理商
		sumbitRegisterContent(params);

		//添加oem资料
//		addOemDataConfig(params); todo

//		String vUrl = ConfigUtils.agent_site_url;//获取代理商服务器站点地址
		String vUrl = ConfigUtils.oem_agent_domain_name;//oem代理商站点地址

		Map<String,Object> mail = masterDao.getOneInfo("userCenter.querySmsMailprop", 100023);// 100023为代理商注册并直接生成账号和密码的邮件
		// 发送开户邮件到邮箱
		String body = (String) mail.get("text");
		body = body.replace("vUrl", vUrl);
		body = body.replace("vemail", params.get("email"));
		body = body.replace("password", password);
		boolean sendEmail = emailService.sendHtmlEmail(params.get("email"), (String)mail.get("subject"), body);
		if(sendEmail == false){
			resultVO.setMsg("注册成功！发送邮件失败！</br>请联系客服");
		}else{
			resultVO.setMsg("注册成功！请注意查收邮件");
		}
		resultVO.setSuccess(true);

//		logService.add(LogType.update,  LogEnum.账户信息管理.getValue(), userId, pageUrl, ip,"账户信息管理-代理商申请记录：受理代理申请",params, data);
		logger.debug("oem注册,返回{}", JsonUtils.toJson(resultVO));
		return resultVO;
	}

	/**
	 * @Description: 注册代理商信息
	 * @author: Niu.T
	 * @date: 2016年12月30日    下午6:01:43
	 * @param params
	 * @return int
	 */
	private int sumbitRegisterContent(Map<String, String> params) {
		Map<String,Object> agentApply = this.masterDao.getOneInfo("userCenter.queryAgentApplyById", params);
		Date date = new Date();
		//插入t_sms_user表
		int insertUser = this.insertSmsUserInfoForRegister(params, agentApply, date);
		if(insertUser <= 0){
			throw new OperationException("插入t_sms_user表失败");
		}

		int agent_id = AgentIdUtil.getAgentId();
		params.put("agent_id",agent_id+"");//保存代理商id，生成oem资料需要用到

		//插入t_sms_agent_info表
		agentApply.put("agent_type",params.get("agent_type"));
		if (params.get("belongSale") != null){
			agentApply.put("belong_sale", params.get("belongSale"));
		}
		int insertAgent = this.insertSmsAgentInfoForRegister(agent_id, agentApply,date);
		if(insertAgent <= 0){
			throw new OperationException("插入t_sms_agent_info表失败");
		}

		//插入t_sms_agent_account表
		int insertAgentAccount = this.insertSmsAgentAccountForRegister(agent_id);
		if(insertAgentAccount <= 0){
			throw new OperationException("插入t_sms_agent_account表失败");
		}

		//给用户赋代理商的角色t_sms_user_role
		int createUserRole = this.createUserRole((long)agentApply.get("userId"),params.get("agent_type"));
		if(createUserRole <= 0){
			throw new OperationException("给用户赋代理商的角色失败");
		}

		logger.debug("\r\n代理商受理 -> 向4张表添加的数据条数: \r\n\t ①t_sms_user --> {} \r\n\t ②t_sms_agent_info --> {}\r\n\t ③t_sms_agent_account --> {} \r\n\t ④t_sms_user_role --> {}",
				insertUser,insertAgent,insertAgentAccount,createUserRole);

		return insertUser;
	}

	/**
	 * @Description: 向t_sms_user表添加用户信息
	 * @author: Niu.T
	 * @date: 2016年12月30日    下午6:02:09
	 * @param params
	 * @param agentApply
	 * @param date
	 * @return int
	 */
	public int insertSmsUserInfoForRegister(Map<String, String> params,Map<String,Object> agentApply,Date date){

		String email = params.get("email");

		Map<String,Object> emailParams = new HashMap<>();
		emailParams.put("email",email);

		int emailAgentNum = this.masterDao.getOneInfo("userCenter.querySmsUserCountByEmail",emailParams);
		if(emailAgentNum > 0){
			throw new OperationException("操作失败，邮箱已经存在");
		}

		Map<String,Object> smsUserParams = new HashMap<String,Object>();
		String sid = SecurityUtils.generateSid();

		String agent_type = params.get("agent_type");
		String web_id = null;
		if("2".equals(agent_type)){
			web_id = "2";
		}else {
			web_id = "4";
		}

		String password = SecurityUtils.encryptMD5(params.get("password"));
		smsUserParams.put("sid", sid);
		smsUserParams.put("email", params.get("email"));
		smsUserParams.put("password", MD5.md5(password));	// 初始密码(随机八位数字和字母组合)
		smsUserParams.put("user_type", "1");
		smsUserParams.put("status", "1");
		smsUserParams.put("mobile", agentApply.get("mobile"));
		smsUserParams.put("realname", agentApply.get("realname"));
		smsUserParams.put("create_date", date);
		smsUserParams.put("update_date", date);
		smsUserParams.put("login_times", 0);
		smsUserParams.put("web_id", web_id);		//web应用ID：1短信调度系统 2代理商平台 3运营平台
		int insert = this.masterDao.insert("userCenter.insertSmsUser", smsUserParams);
		agentApply.put("userId", smsUserParams.get("id"));
		if(insert <= 0){
			logger.debug("代理商受理 / 添加用户信息失败(t_sms_user), 参数  --> {} \r\n\t insert结果 --> {}",smsUserParams,insert);
		}
		return insert;
	}

	//插入t_sms_agent_info表
	public int insertSmsAgentInfoForRegister(int agent_id,Map<String,Object> agentApply,Date date){

		Map<String,Object> smsAgentInfoParmas = new HashMap<String,Object>();
		smsAgentInfoParmas.put("agent_id", agent_id);
		smsAgentInfoParmas.put("admin_id", agentApply.get("userId"));
		smsAgentInfoParmas.put("agent_name", agentApply.get("realname"));
		smsAgentInfoParmas.put("shorter_name", agentApply.get("realname"));
		smsAgentInfoParmas.put("agent_type", agentApply.get("agent_type"));
		smsAgentInfoParmas.put("status", 1);
		smsAgentInfoParmas.put("oauth_status", 2);
		smsAgentInfoParmas.put("oauth_date", null);
		smsAgentInfoParmas.put("address", agentApply.get("address"));
		smsAgentInfoParmas.put("company", agentApply.get("company"));
		smsAgentInfoParmas.put("company_nbr", null);
		smsAgentInfoParmas.put("mobile", agentApply.get("mobile"));
		smsAgentInfoParmas.put("create_time", date);
		smsAgentInfoParmas.put("update_time", null);
		smsAgentInfoParmas.put("remark", null);

		if (agentApply.get("belong_sale") != null){
			smsAgentInfoParmas.put("belong_sale", agentApply.get("belong_sale"));
		}
		int insert = this.masterDao.insert("userCenter.insertSmsAgentInfo", smsAgentInfoParmas);
		if(insert <= 0){
			logger.debug("代理商受理 / 添加代理商信息失败(t_sms_agent_info), 参数  --> {} \r\n\t insert结果 --> {}",smsAgentInfoParmas,insert);
		}
		return insert;
	}

	public int insertSmsAgentAccountForRegister(int agent_id){
		Map<String,Object> smsAgentAccountParms = new HashMap<String,Object>();
		smsAgentAccountParms.put("agent_id", agent_id);
		smsAgentAccountParms.put("balance", 0);
		smsAgentAccountParms.put("credit_balance", 0);
		smsAgentAccountParms.put("accumulated_income", 0);
		smsAgentAccountParms.put("commission_income", 0);
		smsAgentAccountParms.put("accumulated_recharge", 0);
		int insert = this.masterDao.insert("userCenter.insertSmsAgentAccount", smsAgentAccountParms);
		if(insert <= 0){
			logger.debug("代理商受理 / 给代理商添加账户信息失败(t_sms_agent_account), 参数  --> {} \r\n\t insert结果 --> {}",smsAgentAccountParms,insert);
		}
		return insert;
	}

	private int createUserRole(Long userId,String agent_type){
		//代理商的角色id为3
		int roleId = 3;
		if("2".equals(agent_type)){
			//品牌代理商
			roleId = 3;
		}else{
			//oem代理商
			roleId = this.masterDao.getOneInfo("userCenter.queryOemRoleId",null);
		}

		Map<String,Object> userRoleParams = new HashMap<String,Object>();
		userRoleParams.put("role_id", roleId);
		userRoleParams.put("user_id", userId);
		int insert = this.masterDao.insert("userCenter.insertSmsUserRole", userRoleParams);
		if(insert <= 0){
			logger.debug("代理商受理 / 给代理商生成账号分配菜单(t_sms_user_role)失败, 参数  --> {} \r\n\t insert结果 --> {}",userRoleParams,insert);
		}
		return insert;
	}

}
