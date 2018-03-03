package com.ucpaas.sms.service.impl;

import com.jsmsframework.user.entity.JsmsClientInfoExt;
import com.jsmsframework.user.service.JsmsClientInfoExtService;
import com.ucpaas.sms.common.entity.R;
import com.ucpaas.sms.common.util.SecurityUtils;
import com.ucpaas.sms.constant.LoginResContent;
import com.ucpaas.sms.dao.SmsAccountDao;
import com.ucpaas.sms.model.SmsAccountModel;
import com.ucpaas.sms.model.po.SmsAccountModelPo;
import com.ucpaas.sms.service.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class LoginServiceImpl implements LoginService {

	private Logger logger = LoggerFactory.getLogger(LoginServiceImpl.class);

	@Autowired
	private SmsAccountDao smsAccountDao;

	@Autowired
	private JsmsClientInfoExtService clientInfoExtService;

	@Override
	public R loginValidate(String loginAccount, String loginPassword) {
		Map<String, Object> sqlParams = new HashMap<>();
		sqlParams.put("loginAccount", loginAccount);
		SmsAccountModelPo model = smsAccountDao.getAccountInfoByLoginAccount(sqlParams);

		if (model == null) {
			return R.error(LoginResContent.账号不存在.toString());
		}

		// Add by lpjLiu 20170926 v2.2.2 v5.14.0 增加帐号扩展属性
		JsmsClientInfoExt clientInfoExt = clientInfoExtService.getByClientId(model.getClientId());
		model.setClientInfoExt(clientInfoExt);
		if(model.getClientInfoExt() == null){
			return R.error(LoginResContent.账号不存在.toString());
		}

		int status = model.getStatus();
		if (status == 0) {
			return R.error(LoginResContent.账号未激活.toString());
		}

		if (status == 5) {
			return R.error(LoginResContent.账号已冻结.toString());
		}

		if (status == 6) {
			return R.error(LoginResContent.账号已注销.toString());
		}

		if (status == 7) {
			return R.error(LoginResContent.账号已锁定.toString());
		}

		// Mod by lpjLiu 20170926 v2.2.2 v5.14.0 增加帐号扩展属性
//		String password = model.getPassword() == null ? "" : model.getPassword();
		String password = model.getClientInfoExt().getWebPassword() == null?"":model.getClientInfoExt().getWebPassword();
		password = SecurityUtils.encryptMD5(password);
		if (!password.equals(loginPassword)) {
			return R.error(LoginResContent.密码错误.toString());
		}
		return R.ok(LoginResContent.登录成功.toString(), model);
	}

	@Override
	public SmsAccountModel getLoginClientInfo(String clientId) {
		// AjaxResult result = new AjaxResult();
		Map<String, Object> sqlParams = new HashMap<>();
		sqlParams.put("clientId", clientId);
		SmsAccountModel model = smsAccountDao.getAccountInfoByClientId(sqlParams);
		// result.setData(model);;
		return model;
	}

	@Override
	public SmsAccountModel getApiClientInfo(String clientid) {
		return smsAccountDao.getApiClientInfo(clientid);
	}
	
	

}
