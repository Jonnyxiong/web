package com.ucpaas.sms.controller;

import com.ucpaas.sms.common.AjaxResult;
import com.ucpaas.sms.common.SmsException;
import com.ucpaas.sms.common.entity.R;
import com.ucpaas.sms.constant.Constant;
import com.ucpaas.sms.model.po.SmsAccountModelPo;
import com.ucpaas.sms.service.LoginService;
import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.util.web.ServletUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

@Controller
public class LoginController extends BaseController {
	private Logger logger = LoggerFactory.getLogger(LoginController.class);

	@Autowired
	private LoginService loginService;

	@RequestMapping(value = { "/login" }, method = RequestMethod.GET)
	public ModelAndView login(HttpServletRequest request, ModelAndView mv) throws Exception {
		// OEM代理个性化配置
		Map<String, Object> smsOemData = getSMSOemData(request.getServerName());
		super.oemAjust(smsOemData, request);

		mv.addObject("oem", smsOemData);
		mv.setViewName("login");
		return mv;
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	@ResponseBody
	public R loginValidate(HttpServletRequest request, String loginAccount, String password, HttpSession session) throws IOException {
		R result = loginService.loginValidate(loginAccount, password);
		if (result.getCode() == 0) {
			request.getSession().setAttribute(Constant.LOGIN_USER_INFO, result.getData());
			request.getSession().setAttribute(Constant.WEB_ID, ConfigUtils.web_id);
			request.getSession().setAttribute(Constant.IS_OEM,
					ServletUtil.isOem(ServletUtil.getLoginUserInfo(request)));
			session.setAttribute("isOEM",ServletUtil.isOem(ServletUtil.getLoginUserInfo(request)));
			session.setAttribute("clientid",((SmsAccountModelPo)result.getData()).getClientId());
		}
		return result;
	}

	@RequestMapping(value = "/quit", method = RequestMethod.GET)
	public String quit(HttpServletRequest request) {
		request.getSession().setAttribute(Constant.LOGIN_USER_INFO, null);
		request.getSession().setAttribute(Constant.WEB_ID, null);
		return "redirect:/login";
	}

	@RequestMapping("/lockscreen")
	public ModelAndView lockscreen(HttpServletRequest request, ModelAndView mv) throws SmsException {

		// OEM代理个性化配置
		Map<String, Object> smsOemData = getSMSOemData(request.getServerName());
		super.oemAjust(smsOemData, request);

		mv.addObject("oem", smsOemData);
		mv.setViewName("lockscreen");
		return mv;
	}

	@RequestMapping("/isSessionValid")
	@ResponseBody
	public AjaxResult isSessionValid() {
		AjaxResult result = new AjaxResult();
		result.setIsSuccess(true);
		return result;
	}

}
