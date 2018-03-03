package com.ucpaas.sms.controller;

import com.jsmsframework.channel.service.JsmsKeywordListService;
import com.jsmsframework.channel.service.JsmsWhiteKeywordChannelService;
import com.jsmsframework.common.dto.R;
import com.jsmsframework.common.util.JsonUtil;
import com.ucpaas.sms.common.util.StringUtils;
import com.ucpaas.sms.constant.Constant;
import com.ucpaas.sms.model.po.SmsAccountModelPo;
import com.ucpaas.sms.util.web.ServletUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/content")
public class ContentTestController extends BaseController {
	@Autowired
	private JsmsKeywordListService jsmsKeywordListService;

	/**
	 * 内容测试页面
	 */
	@RequestMapping(value = "/test", method = RequestMethod.GET)
	public ModelAndView smsView(HttpServletRequest request, ModelAndView mv, HttpSession session) throws Exception {
		everyPageShouldHave(mv, request.getServerName(), "/content/test", request);
		SmsAccountModelPo accountModel = (SmsAccountModelPo) session.getAttribute(Constant.LOGIN_USER_INFO);
		mv.addObject("accountModel", accountModel);
		mv.setViewName("content/test");
		return mv;
	}

	@ResponseBody
	@RequestMapping(value = "/user", method = RequestMethod.POST)
	public String accountInfo(HttpServletRequest request,String content) throws Exception {
		Map<Integer, String> returnMap = new HashMap<>();
		if (StringUtils.isNotBlank(content)) {
            return JsonUtil.toJson(jsmsKeywordListService.checkKeyword(content));
		} else {
			returnMap.put(201, "输入验证短信为空");
			return JsonUtil.toJson(returnMap);
		}
	}
}
