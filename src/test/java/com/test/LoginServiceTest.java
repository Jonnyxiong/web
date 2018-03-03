package com.test;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ucpaas.sms.common.entity.R;
import com.ucpaas.sms.common.util.SecurityUtils;
import com.ucpaas.sms.service.LoginService;

public class LoginServiceTest extends SupperTest{
	
	@Autowired
	private LoginService loginService;
	
	
	@Test
	public void loginValidateTest(){
		String loginAccount = "hzz";
		String loginPassword = SecurityUtils.encryptMD5("123456");
		R result = loginService.loginValidate(loginAccount, loginPassword);
		System.out.println("flag:--------->"+result.getCode());
		System.out.println("结果:---------->"+result.getMsg());
	}

}
