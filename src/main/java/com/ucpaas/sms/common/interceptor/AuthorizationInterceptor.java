/**
 * lpjLiu 2017-04-25
 */
package com.ucpaas.sms.common.interceptor;

import com.ucpaas.sms.common.annotation.IgnoreAuth;
import com.ucpaas.sms.common.entity.R;
import com.ucpaas.sms.common.util.SpringContextUtils;
import com.ucpaas.sms.common.util.StringUtils;
import com.ucpaas.sms.common.util.security.Des3Utils;
import com.ucpaas.sms.constant.Constant;
import com.ucpaas.sms.dao.SmsAccountDao;
import com.ucpaas.sms.model.SmsAccountModel;
import com.ucpaas.sms.service.LoginService;
import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.util.web.ServletUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 权限验证
 */
public class AuthorizationInterceptor extends HandlerInterceptorAdapter {
	private final Logger logger = LoggerFactory.getLogger(AuthorizationInterceptor.class);
	private static Set<String> whitelist = null;

	{
		whitelist = new HashSet<String>();

		// 品牌
		whitelist.add("/index.html");
		whitelist.add("/experience.html");
		whitelist.add("/sms_experience");
		whitelist.add("/randCheckCodePic");
		whitelist.add("/randCodeCheck");
		whitelist.add("/agentApply");

		// whitelist.add("/app/index");
		//
		// whitelist.add("/console/login");
		// whitelist.add("/console/lockscreen");
		// whitelist.add("/console/loginValidate");

		// whitelist.add("/register");

		// oem
		whitelist.add("/login");
		whitelist.add("/lockscreen");
		whitelist.add("/error");
		whitelist.add("/userCenter/agentRegister");

		// // 登录
		// whitelist.add("/login");
		// whitelist.add("/login.html");
		// whitelist.add("/login.jsp");
		// whitelist.add("/favicon.ico");
		//
		// // 公共
		// whitelist.add("/agent/common");
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		// 获取访问路径

		String accessUrl = request.getRequestURI().toString();

		if (check(request, accessUrl, handler)) {
			return true;
		} else {
			// 如果数据库用户信息，跳到登录页面
			if (request.getHeader("x-requested-with") != null
					&& request.getHeader("x-requested-with").equalsIgnoreCase("XMLHttpRequest")) {
				// ajax请求，直接让session超时，跳到登录页面
				response.setHeader("sessionstatus", "TIMEOUT");
				response.setHeader("CONTEXTPATH", request.getContextPath() + "/lockscreen");

				// 设置错误码401
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				// 设置状态码401
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			} else {
				// 普通请求，跳到登录页面
				response.sendRedirect(request.getContextPath() + "/login");

				// TODO 品牌
				// response.sendRedirect(request.getContextPath() +
				// "/console/login");
			}
		}

		return false;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		if (modelAndView != null) {
			// 放入项目全路径
			modelAndView.addObject("ctx", request.getScheme() + "://" + request.getServerName() + ":"
					+ request.getServerPort() + request.getContextPath());
			modelAndView.addObject("contextPath", request.getContextPath());
		}
	}

	private boolean isAutoLogin(HttpServletRequest request) {
		String token = request.getParameter("oemAuthToken");
		if (StringUtils.isBlank(token)) {
			return false;
		}

		String enc = Des3Utils.decodeDes3(token);
		if (StringUtils.isEmpty(enc)) {
			return false;
		}

		// 客户id====手机号码====密码====当前时间
		String[] arr = enc.split("====");
		if (arr.length != 4) {
			return false;
		}

		String mobile = arr[1];
		String pwd = arr[2];
		long time = Long.parseLong(arr[3]);
		LoginService loginService = SpringContextUtils.getBean(LoginService.class);
		if (loginService == null) {
			return false;
		}

		// 验证失效时间
		Long tokenTime = null;
		Object obj = request.getSession().getAttribute(Constant.LOGIN_USER_TOKEN);
		if (obj != null) {
			tokenTime = Long.parseLong(obj.toString());
		}

		if (tokenTime != null && (new Date().getTime() > tokenTime)) {
			return false;
		}

		R result = loginService.loginValidate(mobile, pwd);
		if (result.getCode() == 0) {
			request.getSession().setAttribute(Constant.LOGIN_USER_INFO, result.getData());
			request.getSession().setAttribute(Constant.WEB_ID, ConfigUtils.web_id);
			request.getSession().setAttribute(Constant.IS_OEM,
					ServletUtil.isOem(ServletUtil.getLoginUserInfo(request)));


			// 将时间加10分钟存入
			Long newLong = time + 600 * 1000;
			request.getSession().setAttribute(Constant.LOGIN_USER_TOKEN, newLong);
		}
		return result.getCode() == 0;
	}

	/**
	 * 用户是否登录
	 *
	 * @param request
	 * @return
	 */
	public boolean isLogin(HttpServletRequest request) {
		SmsAccountModel accountModel = (SmsAccountModel) request.getSession().getAttribute(Constant.LOGIN_USER_INFO);
		String webId = (String) request.getSession().getAttribute(Constant.WEB_ID);
		if (accountModel == null || StringUtils.isEmpty(webId)) {
			return false;
		}

		if (webId.equals(ConfigUtils.web_id)) {
			Map<String, Object> sqlParams = new HashMap<>();
			sqlParams.put("clientId", accountModel.getClientId());
			SmsAccountDao smsAccountDao = SpringContextUtils.getBean(SmsAccountDao.class);
			SmsAccountModel dbAccount = smsAccountDao.getAccountInfoByClientId(sqlParams);
			if (dbAccount.getStatus() != 1) {
				request.getSession().setAttribute(Constant.LOGIN_USER_INFO, null);
				request.getSession().setAttribute(Constant.WEB_ID, null);
				return false;
			}
			return true;
		}

		return false;
	}

	private boolean check(HttpServletRequest request, String accessUrl, Object handler) {
		// 获取资源路径，跳过，不处理
		if (accessUrl.equals("/") || accessUrl.contains("assets/") || accessUrl.contains("static/")
				|| accessUrl.contains("js/") || accessUrl.contains("css/") || accessUrl.contains("img/")
				|| accessUrl.contains("fonts/")|| accessUrl.contains("font/")) {
			return true;
		}

		// 循环判断访问路径是否包含了这个地址
		Boolean canAccess = false;
		for (String url : whitelist) {
			if (accessUrl.contains(url)) {
				canAccess = true;
				break;
			}
		}

		if (canAccess) {
			return true;
		}

		// 验证是否是对外接口
		// if(accessUrl.startsWith("/api/")){
		// return false;
		// }

		// 如果有@IgnoreAuth注解，则不验证
		IgnoreAuth annotation = null;
		if (handler instanceof HandlerMethod) {
			annotation = ((HandlerMethod) handler).getMethodAnnotation(IgnoreAuth.class);
		}

		if (annotation != null) {
			return true;
		}

		if (isLogin(request)) {
			if (accessUrl.contains("swagger-ui.html") || accessUrl.contains("webjars")
					|| accessUrl.contains("v2/api-docs")) {
				if (ConfigUtils.environmentFlag.equals("development")
						|| ConfigUtils.environmentFlag.equals("devtest")) {
					return true;
				} else {
					return false;
				}
			}

			return true;
		} else {
			if (isAutoLogin(request)) {
				return true;
			}

			return false;
		}
	}
}
