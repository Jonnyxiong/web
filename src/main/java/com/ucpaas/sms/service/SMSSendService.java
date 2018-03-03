package com.ucpaas.sms.service;

import com.jsmsframework.common.dto.ResultVO;
import com.jsmsframework.common.enums.HttpProtocolType;
import com.jsmsframework.sms.send.dto.JsmsAccessSmsDTO;
import com.jsmsframework.sms.send.po.JsmsAccessTimerSms;
import com.ucpaas.sms.common.entity.R;
import com.ucpaas.sms.model.AccessSmsBO;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.util.Map;

public interface SMSSendService {
	
	Map<String, Object> smsExperience(String mobile, String ip) ;
	@Deprecated
	Map<String, Object> sendSmsOem(AccessSmsBO smsModel);

	R sendSms(AccessSmsBO smsModel, HttpProtocolType httpProtocolType);

	ResultVO oemSmsSend(JsmsAccessSmsDTO jsmsAccessSmsDTO, String clientId);

	Map<String, Object> importMobile(CommonsMultipartFile excel) ;
	Map<String, Object> clientAccountStatusCheck(String clientId);

	ResultVO sendTimSmsOem(JsmsAccessTimerSms jsmsAccessTimerSms, String clientId, String taskId, Integer chargeNumTotal, String submitFlag);
}
