/**    
 * @Title: AccountController.java  
 * @Package: com.ucpaas.sms.controller.account
 * @Description: TODO
 * @author: Niu.T    
 * @date: 2016年9月23日 上午11:20:31  
 * @version: V1.0    
 */
package com.ucpaas.sms.controller;

import com.ucpaas.sms.constant.Constant;
import com.ucpaas.sms.model.SmsAccountModel;
import com.ucpaas.sms.model.SmsOauthPic;
import com.ucpaas.sms.model.po.SmsAccountModelPo;
import com.ucpaas.sms.service.AccountService;
import com.ucpaas.sms.service.CommonService;
import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.util.web.ServletUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: AccountController
 * @Description: 官网&客户平台 -> 账户管理Controller
 * @author: Niu.T
 * @date: 2016年9月23日 上午11:20:31
 */
@Controller
@RequestMapping("/account")
// url:/模块/{id}/细分/list
public class AccountController extends BaseController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AccountService accountService;

	@Autowired
	private CommonService commonService;

	/**
	 * @Description: 账户管理 -> (跳转)获取账户基本信息
	 * @author: Niu.T
	 * @date: 2016年9月23日 上午11:41:54
	 * @return: String
	 * @throws Exception
	 */
	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public ModelAndView accountInfo(HttpServletRequest request, ModelAndView mv, HttpSession session) throws Exception {
		SmsAccountModelPo accountModel = (SmsAccountModelPo) session.getAttribute(Constant.LOGIN_USER_INFO);
		mv.addObject("accountModel", accountModel);
		everyPageShouldHave(mv, request.getServerName(), "/account/info", request);
		mv.setViewName("account/info");
		// 1. 获取当前登录账户的ID
		String clientId = accountModel.getClientId();
		// 2. 调用业务获取对应账户的基本信息
		SmsAccountModel accountInfo = accountService.getAccountInfo(clientId);
		// 3. 将数据添加到视图模型中
		mv.addObject("accountInfo", accountInfo);
		return mv;
	}

	/**
	 * @Description: 账户资料 -> 修改基本资料
	 * @author: Niu.T
	 * @date: 2016年9月23日 上午11:59:44
	 * @return: Map<String,Object>
	 */
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> updateAccount(SmsAccountModel smsAccountModel) {

		Map<String, Object> data = new HashMap<String, Object>();
		// 1. 调用service,进行业务处理
		data = accountService.updateAccInfo(smsAccountModel);
		return data;
	}

	/**
	 * @Description: 账户安全 -> (跳转至)账户安全信息
	 * @author: Niu.T
	 * @date: 2016年9月23日 下午2:16:05
	 * @return: String
	 * @throws Exception
	 */
	@RequestMapping("/security")
	public ModelAndView securityInfo(HttpServletRequest request, ModelAndView mv, HttpSession session)
			throws Exception {
		SmsAccountModelPo accountModel = (SmsAccountModelPo) session.getAttribute(Constant.LOGIN_USER_INFO);
		mv.addObject("accountModel", accountModel);
		everyPageShouldHave(mv, request.getServerName(), "/account/security", request);
		// 1. 获取当前登录账户的ID
		String clientId = accountModel.getClientId();
		// 2. 调用业务获取对应账户的基本信息
		SmsAccountModel accountInfo = accountService.getAccountInfo(clientId);
		// 3. 将数据添加到视图模型中
		mv.addObject("accountInfo", accountInfo);
		mv.setViewName("account/security");
		return mv;
	}

	/**
	 * @Description: 判断session是否有效
	 * @author: Niu.T
	 * @date: 2016年9月27日 下午8:45:41
	 * @return: boolean
	 */
	@RequestMapping("/online")
	@ResponseBody
	public boolean isOnline() {
		return true;
	}

	/**
	 * @Description: 账户安全 -> 密码编辑页面
	 * @author: Niu.T
	 * @date: 2016年9月23日 下午2:19:33
	 * @return: String
	 * @throws Exception
	 */
	@RequestMapping("/modifyPassword")
	public ModelAndView editPassword(HttpServletRequest request, ModelAndView mv, HttpSession session)
			throws Exception {
		SmsAccountModelPo accountModel = (SmsAccountModelPo) session.getAttribute(Constant.LOGIN_USER_INFO);
		mv.addObject("accountModel", accountModel);
		everyPageShouldHave(mv, request.getServerName(), "/account/security", request);
		String clientId = accountModel.getClientId();
		mv.addObject("clientId", clientId);
		mv.setViewName("account/modifyPassword");
		return mv;
	}

	/**
	 * @Description: 账户安全 -> 修改密码
	 * @author: Niu.T
	 * @date: 2016年9月24日 上午11:26:49
	 * @return: Map<String,Object>
	 */
	@RequestMapping("/updatePassword")
	@ResponseBody
	public Map<String, Object> updatePassword(HttpServletRequest request) {
		Map<String, Object> result = new HashMap<String, Object>();
		// String oldPassword =
		// EncryptUtils.decodeDes3(request.getParameter("oldPwd"));
		// String newPassword =
		// EncryptUtils.decodeDes3(request.getParameter("password1"));
		String oldPassword = request.getParameter("oldPwd");
		String newPassword = request.getParameter("password1");

		SmsAccountModel accountModel = (SmsAccountModel) request.getSession().getAttribute(Constant.LOGIN_USER_INFO);
		accountModel.setPassword(oldPassword);

		boolean isExist = accountService.checkPasswordById(accountModel);// 判断旧密码是否正确
		if (isExist) {
			accountModel.setPassword(newPassword);
			result = accountService.updatePassword(accountModel);

		} else {
			result.put("result", "fail");
			result.put("msg", "原密码错误");
		}
		return result;
	}

	@RequestMapping(value = "/isExistOauthPicInfo")
	@ResponseBody
	public Map<String, Object> isExistOauthPicInfo(HttpSession session) {
		SmsAccountModelPo accountModel = (SmsAccountModelPo) session.getAttribute(Constant.LOGIN_USER_INFO);
		Map<String, Object> result = new HashMap<String, Object>();
		String client_id = accountModel.getClientId();
		result = commonService.isExistOauthPicInfo(client_id);

		return result;
	}

	@RequestMapping(value = "/isOauth", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> clientOauthStatusCheck(HttpServletRequest request) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		SmsAccountModel userInfo = ServletUtil.getLoginUserInfo(request);
		result = commonService.clientAccountStatusCheck(userInfo.getClientId());

		return result;
	}

	/**
	 * @Description: 账户资料 -> (跳转至)账户资质信息(若为添加资质信息则跳转到添加页面)
	 * @author: huangwenjie
	 * @date: 2016年9月23日 下午12:02:10
	 * @return: String
	 * @throws Exception
	 */
	@RequestMapping(value = "/qualification", method = RequestMethod.GET)
	public ModelAndView getCerInfo(HttpServletRequest request, ModelAndView mv, HttpSession session) throws Exception {
		SmsAccountModelPo accountModel = (SmsAccountModelPo) session.getAttribute(Constant.LOGIN_USER_INFO);
		mv.addObject("accountModel", accountModel);
		everyPageShouldHave(mv, request.getServerName(), "/account/info", request);

		String clientId = accountModel != null ? accountModel.getClientId() : null;
		SmsOauthPic smsOauthPic = accountService.getCerInfo(clientId);
		String smspImgUrl = ConfigUtils.smsp_img_url.endsWith("/")
				? ConfigUtils.smsp_img_url.substring(0, ConfigUtils.smsp_img_url.lastIndexOf("/"))
				: ConfigUtils.smsp_img_url;
		if (smsOauthPic.getImgUrl() != null) {
			String img = smspImgUrl + "/file/scanPic.html?path=" + smsOauthPic.getImgUrl();
			smsOauthPic.setImgUrl(img);
		}
		mv.addObject("cerInfo", smsOauthPic);
		mv.addObject("smsp_img_url", smspImgUrl);
		mv.setViewName(smsOauthPic.getId() != null ? "/account/qualification/view" : "/account/qualification/save");
		return mv;
	}

	/**
	 * @Description: 账户资料 -> 保存账户资质
	 * @author: Niu.T
	 * @date: 2016年9月23日 下午2:06:54
	 * @return: Map<String,Object>
	 */
	@RequestMapping(value = "/qualification/save", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> saveCerInfo(SmsOauthPic smsOauthPic,
										   @RequestParam(value = "flag", required = false) String flag, HttpServletRequest request) {

		Map<String, Object> data = new HashMap<String, Object>();
		// 1. 判断页面提交的数据是否有效

		// String uploadUrl = ConfigUtils.client_oauth_pic;// 客户资质图片存放文件夹
		// 上传图片文件名称
		// 更新数据库图片地址
		// 3. 获取flag,判断 :(1)add 新添加资质 (2)update更新资质
		if (flag == null || !"update".equals(flag)) {// 不是更新则是添加
			try {
				data = accountService.addCerInfo(smsOauthPic);
			} catch (Exception e) {
				data.put("msg", "数据存储异常，请稍后再试");
				data.put("result", "fail");
				logger.debug(e.getMessage(), e);
			}
			System.out.println(data);
			return data;
		} else if (flag != null && "update".equals(flag)) {// 如果是更新
			try {
				data = accountService.updateCerInfo(smsOauthPic);
			} catch (Exception e) {
				data.put("msg", "数据存储异常，请稍后再试");
				data.put("result", "fail");
				logger.debug(e.getMessage(), e);
			}
		} else {
			data.put("msg", "请选择图片");
			data.put("result", "fail");

		}
		return data;
	}

	/**
	 * @Description: 资质信息-重新认证
	 * @author: Niu.T
	 * @date: 2016年9月23日 下午12:26:18
	 * @return: String
	 * @throws Exception
	 */
	@RequestMapping("/qualification/update")
	public ModelAndView updateCerInfo(HttpServletRequest request, ModelAndView mv, HttpSession session)
			throws Exception {
		SmsAccountModelPo accountModel = (SmsAccountModelPo) session.getAttribute(Constant.LOGIN_USER_INFO);
		mv.addObject("accountModel", accountModel);
		everyPageShouldHave(mv, request.getServerName(), "/account/info", request);

		String clientId = accountModel.getClientId();
		SmsOauthPic smsOauthPic = accountService.getCerInfo(clientId);
		String smspImgUrl = ConfigUtils.smsp_img_url.endsWith("/")
				? ConfigUtils.smsp_img_url.substring(0, ConfigUtils.smsp_img_url.lastIndexOf("/"))
				: ConfigUtils.smsp_img_url;
		String img = smspImgUrl + "/file/scanPic.html?path=" + smsOauthPic.getImgUrl();
		smsOauthPic.setImgUrl(img);
		mv.addObject("cerInfo", smsOauthPic);
		// 添加flag (1)update:更新客户资质 (2)add:新添加客户资质
		mv.addObject("flag", "update");
		mv.addObject("smsp_img_url", smspImgUrl); // 图片服务器地址
		mv.setViewName("/account/qualification/save");
		return mv;
	}
}
