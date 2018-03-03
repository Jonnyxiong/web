package com.ucpaas.sms.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import com.ucpaas.sms.common.SmsException;
import com.ucpaas.sms.common.util.SecurityUtils;
import com.ucpaas.sms.dao.MasterDao;
import com.ucpaas.sms.model.Menu;
import com.ucpaas.sms.model.po.SmsAccountModelPo;
import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.util.MenuUtil;
import com.ucpaas.sms.util.web.ServletUtil;

public class BaseController {

	private Logger logger = LoggerFactory.getLogger(BaseController.class);

	@Autowired
	protected HttpServletRequest request;

	@Autowired
	protected HttpServletResponse response;

	@Autowired
	protected MasterDao masterDao;

	@ExceptionHandler
	public ModelAndView excption(Exception ex) {
		logger.error("【系统异常】", ex);
		ModelAndView mv = new ModelAndView();
		mv.setViewName("common/error");
		if (ex instanceof SmsException)
			mv.addObject("msg", ex.getMessage());
		return mv;
	}

	private List<Menu> getMenus(String url) {
		List<Menu> topMenus = MenuUtil.getTopMenus();
		for (Menu topMenu : topMenus) {

			if (url.equals(topMenu.getUrl())) {
				topMenu.setClassName("cur");
			}
			if (topMenu.getSubMenus() != null) {
				for (Menu subMenu : topMenu.getSubMenus()) {
					if (url.equals(subMenu.getUrl())) {
						subMenu.setClassName("cur");
						topMenu.setStyle("display:block;");
					}
				}
			}
		}
		return topMenus;
	}

	private List<Menu> getMenus(String url, SmsAccountModelPo accountModel) {
		List<Menu> topMenus = MenuUtil.getTopMenus();
		for (Menu topMenu : topMenus) {
			topMenu.setShow(1);
			topMenu.setShow(ServletUtil.hasRight(topMenu.getUrl(),accountModel) ? 1 : 0);

			if (url.equals(topMenu.getUrl())) {
				topMenu.setClassName("cur " + topMenu.getClassName());
			}

			if (topMenu.getSubMenus() != null) {
				for (Menu subMenu : topMenu.getSubMenus()) {
					if (url.equals(subMenu.getUrl())) {
						subMenu.setClassName("cur");
						topMenu.setStyle("display:block;");
					}

					subMenu.setShow(ServletUtil.hasRight(subMenu.getUrl(),accountModel) ? 1 : 0);
				}

				// 若全部都是不显示，本身菜单也不显示
				boolean hasMenu = false;
				for (Menu subMenu : topMenu.getSubMenus()) {
					if (subMenu.getShow() == 1) {
						hasMenu = true;
						break;
					}
				}

				if (!hasMenu) {
					topMenu.setShow(0);
				}
			}
		}
		return topMenus;
	}

	Map<String, Object> getSMSOemData(String domain_name) throws SmsException {
		Map<String, Object> smsOemData = null;
		if (ConfigUtils.oem_domain_name.equalsIgnoreCase(domain_name)) {
			smsOemData = new HashMap<>();
			smsOemData.put("domain_name", ConfigUtils.oem_domain_name);
			smsOemData.put("copyright_text", "2016 ucpaas.com 版权所有 粤ICP备14046848号");
		} else {
			smsOemData = masterDao.getOneInfo("smsOemData.getByDomainName", domain_name);
		}

		if (smsOemData == null || smsOemData.size() == 0) {
			logger.error(domain_name + "域名未绑定代理商，请联系运营人员处理。");
			throw new SmsException(domain_name + "域名未绑定代理商，请联系运营人员处理。");
		}

		return smsOemData;
	}

	public void everyPageShouldHave(ModelAndView mv, String domain_name, String url, HttpServletRequest request)
			throws Exception {
		// List<Menu> topMenus = getMenus(url);
		List<Menu> topMenus = getMenus(url, ServletUtil.getLoginUserInfo(request));
		logger.info("菜单栏为menu={}", topMenus);
		mv.addObject("topMenus", topMenus);

		Map<String, Object> smsOemData = getSMSOemData(request.getServerName());
		oemAjust(smsOemData, request);

		mv.addObject("oem", smsOemData);
	}

	/**
	 * 跳转图片获取和样式
	 * 
	 * @param smsOemData
	 */
	void oemAjust(Map<String, Object> smsOemData, HttpServletRequest request) {
		String icoUrl = (String) smsOemData.get("tab_logo_url");
		String logoUrl = (String) smsOemData.get("logo_url");
		String comLogoUrl = (String) smsOemData.get("company_logo_url");
		String smspImgUrl = ConfigUtils.smsp_img_url.endsWith("/")
				? ConfigUtils.smsp_img_url.substring(0, ConfigUtils.smsp_img_url.lastIndexOf("/"))
				: ConfigUtils.smsp_img_url;
		if (icoUrl != null && !"".equals(icoUrl)) {
			icoUrl = SecurityUtils.encodeDes3(icoUrl);
			icoUrl = smspImgUrl + "/file/scanPic.html?path=" + icoUrl;
			smsOemData.put("tab_logo_url", icoUrl);
		} else {
			smsOemData.put("tab_logo_url", request.getContextPath() + "/img/logo2.png");
		}
		if (logoUrl != null && !"".equals(logoUrl)) {
			logoUrl = SecurityUtils.encodeDes3(logoUrl);
			logoUrl = smspImgUrl + "/file/scanPic.html?path=" + logoUrl;
			smsOemData.put("logo_url", logoUrl);
		} else {
			smsOemData.put("logo_url", request.getContextPath() + "/img/logo2.png");
		}
		if (comLogoUrl != null && !"".equals(comLogoUrl)) {
			comLogoUrl = SecurityUtils.encodeDes3(comLogoUrl);
			comLogoUrl = smspImgUrl + "/file/scanPic.html?path=" + comLogoUrl;
			smsOemData.put("company_logo_url", comLogoUrl);
		} else {
			smsOemData.put("company_logo_url", request.getContextPath() + "/img/logo.png");
		}

		String navigation_text_color = (String) smsOemData.get("navigation_text_color");
		if (navigation_text_color == null || "".equals(navigation_text_color.trim())) {
			smsOemData.put("navigation_text_color", "#506470;");
		}
		String navigation_backcolor = (String) smsOemData.get("navigation_backcolor");
		if (navigation_backcolor == null || "".equals(navigation_backcolor.trim())) {
			smsOemData.put("navigation_backcolor", "#2b333f;");
		}
	}
}