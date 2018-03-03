package com.ucpaas.sms.controller;

import com.ucpaas.sms.common.util.SecurityUtils;
import com.ucpaas.sms.constant.Constant;
import com.ucpaas.sms.model.SmsAccountModel;
import com.ucpaas.sms.model.po.SmsAccountModelPo;
import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.util.HttpUtils;
import com.ucpaas.sms.util.web.ServletUtil;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Controller
@RequestMapping("/help")
public class HelpController extends BaseController {

	private static Logger logger = LoggerFactory.getLogger(HelpController.class);

	// 帮助中心：短信接入简介
	@RequestMapping(value = "/smsAccessIntroduce")
	public ModelAndView smsAccessIntroduce(ModelAndView view, HttpServletRequest request) throws Exception {
		SmsAccountModel accountModel = ServletUtil.getLoginUserInfo(request);
		view.addObject("accountModel", accountModel);
		everyPageShouldHave(view, request.getServerName(), "/help/smsAccessIntroduce", request);
		view.setViewName("help/smsAccessIntroduce");
		return view;
	}

	// 帮助中心：返回码
	@RequestMapping(value = "/FAQ")
	public ModelAndView FAQ(ModelAndView view, HttpServletRequest request) throws Exception {
		SmsAccountModel accountModel = ServletUtil.getLoginUserInfo(request);
		view.addObject("accountModel", accountModel);
		everyPageShouldHave(view, request.getServerName(), "/help/FAQ", request);
		view.setViewName("help/FAQ");
		return view;
	}

	// 帮助中心：短信接入简介
	@RequestMapping(value = "/apiIntroduce")
	public ModelAndView smsAccessIntroduce(ModelAndView view, HttpServletRequest request, HttpSession session)
			throws Exception {
		SmsAccountModelPo accountModel = ServletUtil.getLoginUserInfo(request);
		view.addObject("accountModel", accountModel);
		everyPageShouldHave(view, request.getServerName(), "/help/apiIntroduce", request);
		view.setViewName("help/apiIntroduce");
		return view;
	}

	@RequestMapping(value = "/smsIntroduceDoc/br")
	public ResponseEntity<byte[]> download() throws IOException {

		String path = request.getSession().getServletContext().getRealPath("/template/sms-introduce.docx");
		String fileName = "短信https接口文档V1.0.docx";
		fileName = URLEncoder.encode(fileName, "UTF-8");
		File downloadFile = new File(path);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.setContentDispositionFormData("attachment", new String(fileName.getBytes(), "utf-8"));

		return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(downloadFile), headers, HttpStatus.CREATED);
	}

	@RequestMapping(value = "/smsIntroduceDoc/oem")
	public ResponseEntity<byte[]> download(HttpServletRequest request, HttpSession session) throws IOException {

		SmsAccountModelPo accountModel = ServletUtil.getLoginUserInfo(request);

		String domain_name = request.getServerName();
		String apiDocUrl = null;
		if (!ConfigUtils.oem_domain_name.equalsIgnoreCase(domain_name)) {
			Map<String, Object> smsOemData = masterDao.getOneInfo("smsOemData.getByDomainName", domain_name);
			if (smsOemData != null)
				apiDocUrl = (String) smsOemData.get("api_document_url");
		}

		if (!StringUtils.isEmpty(apiDocUrl)) {
			String smspImgUrl = ConfigUtils.smsp_img_url.endsWith("/")
					? ConfigUtils.smsp_img_url.substring(0, ConfigUtils.smsp_img_url.lastIndexOf("/"))
					: ConfigUtils.smsp_img_url;
			apiDocUrl = smspImgUrl + "/file/downloadFile.html?path=" + SecurityUtils.encodeDes3(apiDocUrl);
			String fileName = URLEncoder.encode("短信https接口文档V1.0.docx", "UTF-8"); // "SMS_HTTPS_API_DOCUMENT_V1.0.docx";
			String templatePath = request.getSession().getServletContext().getRealPath("/template/");
			StringBuffer path = new StringBuffer();
			path.append(templatePath).append("/").append(accountModel.getClientId())
					.append(new SimpleDateFormat("/yyyy/MM/dd/").format(new Date()));
			path.append(fileName);
			String filePath = HttpUtils.download(apiDocUrl, path.toString());
			if (StringUtils.isEmpty(filePath)) {
				logger.error("api文档下载失败,api_document_url=" + apiDocUrl);
				return null;
			}
			File downloadFile = new File(filePath);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			headers.setContentDispositionFormData("attachment", new String(fileName.getBytes(), "utf-8"));

			return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(downloadFile), headers, HttpStatus.CREATED);
		} else {
			logger.debug("api文档不存在，api_document_url不存在，下载默认的文档");
			String path = request.getSession().getServletContext().getRealPath("/template/sms-api.docx");
			String fileName = URLEncoder.encode("短信https接口文档V1.0.docx", "UTF-8"); // "SMS_HTTPS_API_DOCUMENT_V1.0.docx";
			File downloadFile = new File(path);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			headers.setContentDispositionFormData("attachment", new String(fileName.getBytes(), "utf-8"));

			return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(downloadFile), headers, HttpStatus.CREATED);
		}
	}

	@RequestMapping(value = "/smsCmppIntroduceDoc")
	public ResponseEntity<byte[]> downloadCmppDoc(HttpServletRequest request, HttpSession session) throws IOException {

		String path = request.getSession().getServletContext().getRealPath("/template/sms-cmpp-api-v2.0.doc");
		String fileName = URLEncoder.encode("短信cmpp接口文档V2.0.doc", "UTF-8"); // "SMS_CMPP_API_DOCUMENT_V2.0.doc";
		File downloadFile = new File(path);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.setContentDispositionFormData("attachment", new String(fileName.getBytes(), "utf-8"));

		return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(downloadFile), headers, HttpStatus.CREATED);
	}

	@RequestMapping(value = "/smsGuajiDoc")
	public ResponseEntity<byte[]> smsGuajiDoc() throws IOException {

		String path = request.getSession().getServletContext().getRealPath("/template/sms-guaji-introduce.docx");
		String fileName = "挂机短信https接口文档V1.0.docx";
		fileName = URLEncoder.encode(fileName, "UTF-8");
		File downloadFile = new File(path);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.setContentDispositionFormData("attachment", new String(fileName.getBytes(), "utf-8"));

		return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(downloadFile), headers, HttpStatus.CREATED);
	}

	@RequestMapping(value = "/smsSgipIntroduceDoc")
	public ResponseEntity<byte[]> downloadSgipDoc(HttpServletRequest request, HttpSession session) throws IOException {

		String path = request.getSession().getServletContext().getRealPath("/template/sms-sgip-api-v1.2.doc");
		// String fileName = "SMS_SGIP_API_DOCUMENT_V1.0.doc";
		String fileName = URLEncoder.encode("短信sgip接口文档V1.2.doc", "UTF-8");
		File downloadFile = new File(path);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.setContentDispositionFormData("attachment", new String(fileName.getBytes(), "utf-8"));

		return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(downloadFile), headers, HttpStatus.CREATED);
	}

	@RequestMapping(value = "/smsSmgpIntroduceDoc")
	public ResponseEntity<byte[]> downloadSmgpDoc(HttpServletRequest request, HttpSession session) throws IOException {

		String path = request.getSession().getServletContext().getRealPath("/template/sms-smgp-api-v3.0.3.doc");
		String fileName = URLEncoder.encode("短信smgp接口文档V3.0.doc", "UTF-8"); // "SMS_SMGP_API_DOCUMENT_V3.0.doc";
		File downloadFile = new File(path);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.setContentDispositionFormData("attachment", new String(fileName.getBytes(), "utf-8"));

		return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(downloadFile), headers, HttpStatus.CREATED);
	}

	@RequestMapping(value = "/smsSmppIntroduceDoc")
	public ResponseEntity<byte[]> downloadSmppDoc(HttpServletRequest request, HttpSession session) throws IOException {

		String path = request.getSession().getServletContext().getRealPath("/template/sms-smpp-api-v3.4.pdf");
		String fileName = URLEncoder.encode("短信smpp接口文档V3.4.pdf", "UTF-8"); // "SMS_SMPP_API_DOCUMENT_V3.4.doc";
		File downloadFile = new File(path);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.setContentDispositionFormData("attachment", new String(fileName.getBytes(), "utf-8"));

		return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(downloadFile), headers, HttpStatus.CREATED);
	}

	@RequestMapping(value = "/smsCaiyinDoc")
	public ResponseEntity<byte[]> smsCaiyinDoc() throws IOException {

		String path = request.getSession().getServletContext().getRealPath("/template/sms-caiyin-introduce.docx");
		String fileName = "彩印https接口文档V1.0.docx";
		fileName = URLEncoder.encode(fileName, "UTF-8");
		File downloadFile = new File(path);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.setContentDispositionFormData("attachment", new String(fileName.getBytes(), "utf-8"));

		return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(downloadFile), headers, HttpStatus.CREATED);
	}

	@RequestMapping(value = "/downloadFAQDoc")
	public ResponseEntity<byte[]> downloadFAQDoc(HttpServletRequest request, HttpSession session) throws IOException {

		SmsAccountModelPo accountModel = (SmsAccountModelPo) session.getAttribute(Constant.LOGIN_USER_INFO);

		String domain_name = request.getServerName();

		String FAQDocUrl = null;
		if (!ConfigUtils.oem_domain_name.equalsIgnoreCase(domain_name)) {
			Map<String, Object> smsOemData = masterDao.getOneInfo("smsOemData.getByDomainName", domain_name);
			if (smsOemData != null)
				FAQDocUrl = (String) smsOemData.get("FAQ_document_url");
		}

		if (!StringUtils.isEmpty(FAQDocUrl)) {
			String smspImgUrl = ConfigUtils.smsp_img_url.endsWith("/")
					? ConfigUtils.smsp_img_url.substring(0, ConfigUtils.smsp_img_url.lastIndexOf("/"))
					: ConfigUtils.smsp_img_url;
			FAQDocUrl = smspImgUrl + "/file/downloadFile.html?path=" + SecurityUtils.encodeDes3(FAQDocUrl);
			String fileName = URLEncoder.encode("FAQ文档V1.0.docx", "UTF-8"); // "SMS_FAQ_DOCUMENT_V1.0.docx";
			String templatePath = request.getSession().getServletContext().getRealPath("/template/");
			StringBuffer path = new StringBuffer();
			path.append(templatePath).append("/").append(accountModel.getClientId())
					.append(new SimpleDateFormat("/yyyy/MM/dd/").format(new Date()));
			path.append(fileName);
			String filePath = HttpUtils.download(FAQDocUrl, path.toString());
			if (StringUtils.isEmpty(filePath)) {
				logger.error("FAQ文档下载失败,FAQ_document_url=" + FAQDocUrl);
				return null;
			}
			File downloadFile = new File(filePath);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			headers.setContentDispositionFormData("attachment", new String(fileName.getBytes(), "utf-8"));

			return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(downloadFile), headers, HttpStatus.CREATED);
		} else {
			logger.debug("FAQ文档不存在，FAQ_document_url不存在，下载默认的文档");
			String path = request.getSession().getServletContext().getRealPath("/template/sms-fqa.docx");
			String fileName = URLEncoder.encode("FAQ文档V1.0.docx", "UTF-8");// "SMS_FAQ_DOCUMENT_V1.0.docx";
			File downloadFile = new File(path);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			headers.setContentDispositionFormData("attachment", new String(fileName.getBytes(), "utf-8"));

			return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(downloadFile), headers, HttpStatus.CREATED);
		}
	}

}
