package com.ucpaas.sms.app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/app")
public class AppController {

	private Logger logger = LoggerFactory.getLogger(AppController.class);

	@ExceptionHandler
	public String excption(Exception ex) {
		logger.error("【系统异常】", ex);
		return "common/error";
	}

	/**
	 * lightApp 页面
	 */
	@RequestMapping(value = "/index")
	public ModelAndView homePage(ModelAndView model) throws Exception {
		model.setViewName("lightApp/index");
		return model;
	}


}
