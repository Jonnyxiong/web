package com.ucpaas.sms.service;

		import com.ucpaas.sms.common.entity.R;
		import com.ucpaas.sms.model.SmsAccountModel;

public interface LoginService {

	R loginValidate(String loginAccount, String loginPassword);

	SmsAccountModel getLoginClientInfo(String clientId);

	/**
	 * @Description: 根据api接口提供的参数(clientid)获取客户的基本信息
	 * @author: Niu.T
	 * @date: 2017年2月6日    下午12:10:20
	 * @param clientId
	 * @return SmsAccountModel
	 */
	SmsAccountModel getApiClientInfo(String clientId);
}
