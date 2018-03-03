package com.ucpaas.sms.util.web;

import com.ucpaas.sms.common.util.StringUtils;
import com.ucpaas.sms.constant.Constant;
import com.ucpaas.sms.model.po.SmsAccountModelPo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class ServletUtil {

	private static Logger logger = LoggerFactory.getLogger(ServletUtil.class);

	public static SmsAccountModelPo getLoginUserInfo(HttpServletRequest request) {

		SmsAccountModelPo LOGIN_USER_INFO = (SmsAccountModelPo) request.getSession().getAttribute("LOGIN_USER_INFO");

		return LOGIN_USER_INFO;
	}

	/**
	 * 是否直客
	 * 
	 * @param smsAccountModelPo
	 * @return
	 */
	public static boolean isZk(SmsAccountModelPo smsAccountModelPo) {
		return smsAccountModelPo.getAgentId() == null || smsAccountModelPo.getAgentId() < 20;
	}

	/**
	 * 是否OEM客户
	 * 
	 * @param smsAccountModelPo
	 * @return
	 */
	public static boolean isOem(SmsAccountModelPo smsAccountModelPo) {
		return !isZk(smsAccountModelPo)
				&& smsAccountModelPo.getAgentType().intValue() == Constant.AgentType.OEM代理商.getValue().intValue();
	}

	/**
	 * 是否品牌
	 * 
	 * @param smsAccountModelPo
	 * @return
	 */
	public static boolean isBr(SmsAccountModelPo smsAccountModelPo) {
		return !isZk(smsAccountModelPo)
				&& smsAccountModelPo.getAgentType().intValue() == Constant.AgentType.品牌代理商.getValue().intValue();
	}

	/**
	 * 是否预付
	 * 
	 * @param smsAccountModelPo
	 * @return
	 */
	public static boolean isYuFu(SmsAccountModelPo smsAccountModelPo) {
		return smsAccountModelPo.getPaytype().intValue() == Constant.PayType.预付.getValue().intValue();
	}

	public static Map<String, String> getFormData(HttpServletRequest request) {
		Map<String, String> formData = new HashMap<String, String>();
		String value;
		for (Map.Entry<String, String[]> map : request.getParameterMap().entrySet()) {
			value = StringUtils.join(map.getValue(), ",");
			if (StringUtils.isNotBlank(value)) {
				formData.put(map.getKey(), value.trim());
			}
		}

		logger.debug("\n\nformData-------------------------" + formData + "\n");
		return formData;
	}

	public static String getParameterTrim(HttpServletRequest request, String name) {
		return StringUtils.trim(request.getParameter(name));
	}

	public static Boolean hasRight(String url, SmsAccountModelPo accountModel){
		boolean isHouFu = accountModel.getPaytype() == 1;
		boolean isHttp = accountModel.getSmsfrom() == 6;
		boolean isOem = isOem(accountModel);

		if (StringUtils.isBlank(url)){
			return true;
		}

		// 后付费没有首页、短信发送
		if (url.contains("console")) {
			if (isHouFu) {
				return false;
			}
		}
        // 品牌没有智能模板功能
		if (url.contains("autoTemplate/list")) {
			if (!isOem) {
				return false;
			}
		}

		//去除后付费的短信发送 限制
/*		if (url.contains("bussiness/send")) {
			if (isHouFu || !isHttp) {
				return false;
			}
		}*/

		// OEM、品牌后付费没有彩印、短信模版、我的订单
		if (url.contains("bussiness/template") || url.contains("mms/send") || url.contains("mms/template") || url.contains("mms/record") || url.contains("mms/ussdday") || url.contains("mms/ussdmonthly") || url.contains("mms/flashday") || url.contains("mms/flashmonthly")) {
			if (isOem || isHouFu || !isHttp) {
				return false;
			}
		}

		if (url.contains("finance/order/list")) {
			if (isOem || isHouFu) {
				return false;
			}
		}

		// 后付费、没有我的消费
		if (url.contains("finance/recharge/myConsumption")) {
			if (isHouFu) {
				return false;
			}
		}

		// OEM没有FAQ
		if (url.contains("help/FAQ")) {
			if (isOem) {
				return false;
			}
		}

		// OEM、后付费没有短信余额提醒、资质信息
		return true;
	}

	public static String getProductName(Integer productType) {
		switch (productType) {
			case 0:
				return "行业";
			case 1:
				return "营销";
			case 2:
				return "国际";
			case 3:
				return "验证码";
			case 4:
				return "通知";
			case 7:
				return "USSD";
			case 8:
				return "闪信";
			default:
				return "未知";
		}
	}
}
