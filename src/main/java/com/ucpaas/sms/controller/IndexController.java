package com.ucpaas.sms.controller;

import com.ucpaas.sms.common.AjaxResult;
import com.ucpaas.sms.constant.Constant;
import com.ucpaas.sms.model.AgentApplyVO;
import com.ucpaas.sms.model.po.SmsAccountModelPo;
import com.ucpaas.sms.service.CommonService;
import com.ucpaas.sms.service.HomeService;
import com.ucpaas.sms.util.CommonUtil;
import com.ucpaas.sms.util.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
public class IndexController extends BaseController {
	private static Logger logger = LoggerFactory.getLogger(IndexController.class);

	@Autowired
	private CommonService commonService;

	@Autowired
	private HomeService homeService;

	/**
	 * 默认页面
	 */
	@RequestMapping(value = "/")
	public void root(HttpServletResponse response) throws Exception {
		String domain_name = request.getServerName();

		// 若是默认域名跳转至云之讯主页
		if (ConfigUtils.oem_domain_name.equalsIgnoreCase(domain_name)) {
			try {
				response.sendRedirect("/index.html");
			} catch (IOException e) {
				logger.error("springmvc转发/login失败", e);
				throw e;
			}
		}
		// 自定义域名跳转至登录页面
		else {
			try {
				response.sendRedirect("/login");
			} catch (IOException e) {
				logger.error("springmvc转发/login失败", e);
				throw e;
			}
		}
	}

	/**
	 * 主页
	 */
	@RequestMapping(value = "/index.html")
	public ModelAndView homePage(ModelAndView model) {
		SmsAccountModelPo accountModel = (SmsAccountModelPo) request.getSession()
				.getAttribute(Constant.LOGIN_USER_INFO);
		model.addObject("account", accountModel);
		model.setViewName("official/index");
		return model;
	}

	/**
	 * 在线体验页面
	 */
	@RequestMapping(value = "/experience.html")
	public String experience() {
		return "official/experience";
	}

	/**
	 * 生成验证码图片
	 */
	@RequestMapping(value = "/randCheckCodePic")
	@ResponseBody
	public void randCheckCodePic() throws Exception {
		commonService.randCheckCodePic(request, response);
	}

	/**
	 * 校验验证码
	 */
	@RequestMapping(value = "/randCodeCheck")
	@ResponseBody
	public Map<String, Object> randCodeCheck() throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("result", commonService.randCodeCheck(request));
		return result;
	}

	/**
	 * 体验短信
	 */
	@RequestMapping(value = "/sms_experience", method = RequestMethod.POST)
	@ResponseBody
	public AjaxResult sms(@RequestParam("mobile") String mobile) throws Exception {
		AjaxResult ajaxResult;
		Map<String, Object> result = homeService.smsExperience(mobile, CommonUtil.getClientIP(request));

		if (result != null) {
			ajaxResult = new AjaxResult(AjaxResult.SUCCESS, result);
		} else {
			ajaxResult = new AjaxResult(AjaxResult.ERROR, "短信体验失败");
		}

		return ajaxResult;
	}

	/**
	 * 代理商申请
	 */
	@RequestMapping(value = "/agentApply", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> agentApply(@ModelAttribute("agentApplyVO") AgentApplyVO agentApplyVO) throws Exception {
		return homeService.agentApply(agentApplyVO);
	}

}
