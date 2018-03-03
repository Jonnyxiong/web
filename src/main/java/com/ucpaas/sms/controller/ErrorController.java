package com.ucpaas.sms.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/error")
public class ErrorController {

	@ApiOperation(value = "404页面")
	@RequestMapping(value = "/404", method = RequestMethod.GET)
	public String noPage(HttpServletRequest request, Model model) {
		return "error/404";
	}
	@ApiOperation(value = "500页面")
	@RequestMapping(value = "/500", method = RequestMethod.GET)
	public String errorPage(HttpServletRequest request, Model model) {
		return "error/500";
	}
}
