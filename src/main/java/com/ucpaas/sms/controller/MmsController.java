package com.ucpaas.sms.controller;

import com.jsmsframework.common.dto.ResultVO;
import com.jsmsframework.common.enums.HttpProtocolType;
import com.ucpaas.sms.common.AjaxResult;
import com.ucpaas.sms.common.SmsException;
import com.ucpaas.sms.common.entity.R;
import com.ucpaas.sms.common.util.SecurityUtils;
import com.ucpaas.sms.common.util.StringUtils;
import com.ucpaas.sms.common.util.file.ExcelUtils;
import com.ucpaas.sms.constant.Constant;
import com.ucpaas.sms.enums.SMSType;
import com.ucpaas.sms.model.*;
import com.ucpaas.sms.model.po.SmsAccountModelPo;
import com.ucpaas.sms.service.BussinessService;
import com.ucpaas.sms.service.SMSSendService;
import com.ucpaas.sms.service.TemplateService;
import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.util.DateUtil;
import com.ucpaas.sms.util.web.ServletUtil;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.*;

/**
 * Created by csy on 2017/9/4.
 */
@Controller
@RequestMapping("/mms")
public class MmsController extends BaseController {
	private static final Logger logger = LoggerFactory.getLogger(MmsController.class);

	@Autowired
	private BussinessService bussinessService;

	@Autowired
	private SMSSendService smsSendService;

	@Autowired
	private TemplateService templateService;

	/**
	 * 彩印发送页面
	 */
	@RequestMapping(value = "/send", method = RequestMethod.GET)
	public ModelAndView smsView(HttpServletRequest request, ModelAndView mv, HttpSession session) throws Exception {
		mv.addObject("accountModel", ServletUtil.getLoginUserInfo(request));
		everyPageShouldHave(mv, request.getServerName(), "/mms/send", request);
		mv.setViewName("/mms/send");
		return mv;
	}

	/**
	 * 短信发送
	 */
	@RequestMapping(value = "/sendSms", method = RequestMethod.POST)
	@ResponseBody
	public R sendSms(@ModelAttribute("smsModel") AccessSmsBO accessSmsBO,
			@RequestParam(value = "arr[]", required = false) String[] arr) throws Exception {
		accessSmsBO.setContents(arr);
		if (arr != null)
			for (String ar : arr) {
				if (ar.contains("{") || ar.contains("}") || ar.contains("#*#")) {
					return R.error("内容不能含有关键字符'{'、'}'或'#*#'");
				}
			}
		SmsAccountModel userInfo = ServletUtil.getLoginUserInfo(request);
		accessSmsBO.setClientid(userInfo.getClientId());
		accessSmsBO.setPassword(SecurityUtils.encryptMD5(userInfo.getPassword()));
		HttpProtocolType httpProtocolType = HttpProtocolType.getInstanceByValue(userInfo.getHttpProtocolType());
		return smsSendService.sendSms(accessSmsBO,httpProtocolType);
	}

	// 短信导出实时检查数据
	@RequestMapping(value = "/checkSmsRecordNum")
	@ResponseBody
	public AjaxResult checkSmsRecordNum() throws Exception {
		AjaxResult ajaxResult = new AjaxResult();

		Map<String, String> params = ServletUtil.getFormData(request);
		params.put("clientid", ServletUtil.getLoginUserInfo(request).getClientId());
		String start_time = params.get("start_time");
		String end_time = params.get("end_time");

		// 设置默认时间
		if (start_time == null || end_time == null) {
			DateTime dt = DateTime.now();
			start_time = dt.withMillisOfDay(0).toString("yyyy-MM-dd HH:mm");
			end_time = dt.toString("yyyy-MM-dd HH:mm");
			params.put("start_time", start_time);
			params.put("end_time", end_time);
		}

		Map<String, Object> queryParams = new HashMap<>();
		for (Map.Entry e : params.entrySet()) {
			queryParams.put(e.getKey().toString(), e.getValue());
		}
		List<Integer> smsTypes = new ArrayList<>();
		smsTypes.add(SMSType.USSD.getValue());
		smsTypes.add(SMSType.闪信.getValue());
		queryParams.put("smsTypes", smsTypes);

		// 查询短信记录数量
		List<Map<String, Object>> resultList = bussinessService.querySmsSendRecordCount(queryParams);

		int totalCount = 0;
		if (resultList != null && resultList.size() > 0) {
			totalCount = Integer.parseInt(resultList.get(0).get("totalCount").toString());
		}

		if (totalCount == 0) {
			ajaxResult.setIsSuccess(false);
		} else {
			ajaxResult.setIsSuccess(true);
		}

		return ajaxResult;
	}

	/**
	 * 彩印模板页面
	 */
	@RequestMapping(value = "/template", method = RequestMethod.GET)
	public ModelAndView template(HttpServletRequest request, ModelAndView mv, HttpSession session) throws Exception {
		mv.addObject("accountModel", ServletUtil.getLoginUserInfo(request));
		everyPageShouldHave(mv, request.getServerName(), "/mms/template", request);
		mv.setViewName("/mms/template");
		return mv;
	}

	/**
	 * 彩印发送记录
	 */
	@RequestMapping(value = "/record", method = RequestMethod.GET)
	public ModelAndView record(HttpServletRequest request, ModelAndView mv) throws Exception {
		SmsAccountModelPo accountModel = ServletUtil.getLoginUserInfo(request);
		Map<String, String> params = ServletUtil.getFormData(request);
		params.put("clientid", accountModel.getClientId());
		String start_time = params.get("start_time");
		String end_time = params.get("end_time");
		String mobile = params.get("mobile");
		String content = params.get("content");

		// 设置默认时间
		if (start_time == null || end_time == null) {
			DateTime dt = DateTime.now();
			start_time = dt.withMillisOfDay(0).toString("yyyy-MM-dd HH:mm:ss");
			end_time = dt.toString("yyyy-MM-dd HH:mm:ss");
			params.put("start_time", start_time);
			params.put("end_time", end_time);
		}
		request.setAttribute("start_time", start_time);
		request.setAttribute("end_time", end_time);
		request.setAttribute("mobile", mobile);
		request.setAttribute("content", content);
		DateTime dt = DateTime.now();
		request.setAttribute("min_time", dt.minusMonths(3).toString("yyyy-MM-dd"));
		request.setAttribute("max_time", dt.toString("yyyy-MM-dd"));

		Map<String, Object> queryParams = new HashMap<>();
		for (Map.Entry e : params.entrySet()) {
			queryParams.put(e.getKey().toString(), e.getValue());
		}

		List<Integer> smsTypes = new ArrayList<>();
		smsTypes.add(SMSType.USSD.getValue());
		smsTypes.add(SMSType.闪信.getValue());
		queryParams.put("smsTypes", smsTypes);

		queryParams.put("isOem", ServletUtil.isOem(accountModel));
		PageContainer page = bussinessService.querySmsSendRecord(queryParams);

		// 指定返回视图和模型
		everyPageShouldHave(mv, request.getServerName(), "/mms/record", request);
		mv.setViewName("/mms/record");
		mv.addObject("page", page);
		mv.addObject("accountModel", accountModel);

		return mv;
	}

	@RequestMapping(value = "/exportRecord",method = RequestMethod.POST)
	@ResponseBody
	public Map exportRecord(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("success", false);
		result.put("msg", "生成报表失败");
		try {
			SmsAccountModelPo accountModel = ServletUtil.getLoginUserInfo(request);
			if (accountModel == null) {
				result.put("success", false);
				result.put("msg", "非法请求");
				return result;
			}
			String clientId = accountModel.getClientId();
			Map<String, String> params = ServletUtil.getFormData(request);
			params.put("clientid", clientId);
			String start_time = params.get("start_time");
			String end_time = params.get("end_time");
			StringBuffer fileName = new StringBuffer();
			fileName.append("彩印短信记录-");
			// 设置默认时间
			if (start_time == null || end_time == null) {
				DateTime dt = DateTime.now();
				start_time = dt.withMillisOfDay(0).toString("yyyy-MM-dd HH:mm");
				end_time = dt.toString("yyyy-MM-dd HH:mm");
				params.put("start_time", start_time);
				params.put("end_time", end_time);
			}
			Date endTime = DateUtil.stringToDate(end_time, "yyyy-MM-dd HH:mm");
			fileName.append(DateUtil.dateToStr(endTime, "yyyyMMddHHmm"));
			fileName.append(".xls");

			String text = params.get("text");
			String filePath = ConfigUtils.temp_file_dir + "/" + clientId + "/" + fileName.toString();

			Excel excel = new Excel();
			excel.setFilePath(filePath);
			excel.setTitle("短信记录");
			if (text != null) {
				excel.addRemark("查询条件：" + text);
			}
			excel.addHeader(20, "手机号", "phone");
			excel.addHeader(20, "短信类型", "smstype_name");
			excel.addHeader(20, "发送内容", "content");
			excel.addHeader(20, "发送状态", "status");
			excel.addHeader(20, "状态码", "errorcode_name");
			excel.addHeader(20, "提交时间", "subTime");
			excel.addHeader(20, "发送时间", "sendTime");
			Map<String, Object> queryParams = new HashMap<>();
			for (Map.Entry e : params.entrySet()) {
				queryParams.put(e.getKey().toString(), e.getValue());
			}
			List<Integer> smsTypes = new ArrayList<>();
			smsTypes.add(SMSType.USSD.getValue());
			smsTypes.add(SMSType.闪信.getValue());
			queryParams.put("smsTypes", smsTypes);
			queryParams.put("isOem", ServletUtil.isOem(accountModel));
			excel.setDataList(bussinessService.querySmsRecord4Excel(queryParams));
			if (ExcelUtils.exportExcel(excel)) {
				result.put("success", true);
				result.put("msg", "报表生成成功");
				result.put("fileName", fileName.toString());
				return result;
			}
		} catch (Exception e) {
			logger.error("生成报表失败", e);
		}
		return result;

	}

	/**
	 * USSD日统计
	 */
	@RequestMapping(value = "/ussdday", method = RequestMethod.GET)
	public ModelAndView ussdday(HttpServletRequest request, ModelAndView mv, HttpSession session) throws Exception {
		SmsAccountModelPo accountModel = ServletUtil.getLoginUserInfo(request);

		mv.addObject("accountModel", accountModel);

		// 日发送数据分页查询
		Map<String, String> params = ServletUtil.getFormData(request);

		String start_time = params.get("start_time");
		String end_time = params.get("end_time");

		// 设置默认时间
		if (start_time == null || end_time == null) {
			DateTime dt = DateTime.now();
			dt = dt.minusDays(1);
			start_time = dt.minusMonths(3).toString("yyyy-MM-dd");
			end_time = dt.toString("yyyy-MM-dd");
			params.put("start_time", start_time);
			params.put("end_time", end_time);
		}
		request.setAttribute("start_time", start_time);
		request.setAttribute("end_time", end_time);
		DateTime dt = DateTime.now();
		dt = dt.minusDays(1);
		request.setAttribute("min_time", dt.minusMonths(3).toString("yyyy-MM-dd"));
		request.setAttribute("max_time", dt.toString("yyyy-MM-dd"));

		params.put("clientid", accountModel.getClientId());

		Map<String, Object> queryParams = new HashMap<>();
		for (Map.Entry e : params.entrySet()) {
			queryParams.put(e.getKey().toString(), e.getValue());
		}
		List<Integer> productTypes = new ArrayList<>();
		productTypes.add(Integer.valueOf(Constant.OrderProductType.USSD.getValue()));
		queryParams.put("productTypes", productTypes);
		queryParams.put("startDate", Integer.valueOf(start_time.replace("-", "")));
		queryParams.put("endDate", Integer.valueOf(end_time.replace("-", "")));

		queryParams.put("isOem", ServletUtil.isOem(accountModel));
		PageContainer dayStatPage = bussinessService.queryDayStat(queryParams);
		mv.addObject("dayStatPage", dayStatPage);

		everyPageShouldHave(mv, request.getServerName(), "/mms/ussdday", request);
		mv.setViewName("/mms/ussdday");
		return mv;
	}

	/**
	 * 导出USSD日统计
	 *
	 * @param request
	 * @param mv
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/exportUssdDayStat")
	@ResponseBody
	public ResultVO exportUssdDayStat(HttpServletRequest request, ModelAndView mv) throws Exception {
		ResultVO result = new ResultVO();
		// 代理商信息
		SmsAccountModelPo accountModel = ServletUtil.getLoginUserInfo(request);
		mv.addObject("accountModel", accountModel);

		Map<String, String> params = ServletUtil.getFormData(request);

		String start_time = params.get("start_time");
		String end_time = params.get("end_time");
		// 设置默认时间
		if (start_time == null || end_time == null) {
			DateTime dt = DateTime.now();
			dt = dt.minusDays(1);
			start_time = dt.minusDays(45).toString("yyyy-MM-dd");
			end_time = dt.toString("yyyy-MM-dd");
			params.put("start_time", start_time);
			params.put("end_time", end_time);
		}
		request.setAttribute("start_time", start_time);
		request.setAttribute("end_time", end_time);

		Map<String, Object> queryParams = new HashMap<>();
		for (Map.Entry e : params.entrySet()) {
			queryParams.put(e.getKey().toString(), e.getValue());
		}

		queryParams.put("clientid", accountModel.getClientId());

		List<Integer> productTypes = new ArrayList<>();
		productTypes.add(Integer.valueOf(Constant.OrderProductType.USSD.getValue()));
		queryParams.put("productTypes", productTypes);
		queryParams.put("startDate", Integer.valueOf(start_time.replace("-", "")));
		queryParams.put("endDate", Integer.valueOf(end_time.replace("-", "")));

		try {
			queryParams.put("limit", "LIMIT 0 , 60000");
			queryParams.put("isOem", ServletUtil.isOem(accountModel));
			List<Map<String, Object>> data = bussinessService.exportBussinessData(queryParams);
			if (data == null || data.size() < 1) {
				result.setMsg("根据条件查询到的记录数为0，导出Excel文件失败");
				return result;
			}
			StringBuffer fileName = new StringBuffer();
			fileName.append("USSD日统计");
			Excel excel = new Excel();
			fileName.append(queryParams.get("startDate")).append("-").append(queryParams.get("endDate"));
			excel.setTitle(fileName.toString());
			fileName.append(".xls");
			String filePath = ConfigUtils.temp_file_dir + "/" + accountModel.getClientId() + "/" + fileName.toString();
			excel.setFilePath(filePath);
			excel.addHeader(10, "序号", "rownum");
			excel.addHeader(20, "日期", "date");
			excel.addHeader(20, "提交条数", "usersmstotal");
			excel.addHeader(20, "发送条数", "sendTotal");
			excel.addHeader(20, "成功条数", "successTotal");
			excel.addHeader(20, "平台拦截条数", "interceptTotal");
			excel.addHeader(20, "失败条数", "failTotal");
			excel.addHeader(20, "计费条数", "chargeTotal");

			excel.setShowRownum(false);
			int i = 1;
			for (Map da : data) {
				da.put("rownum", i++);
			}
			excel.setDataList(data);
			if (ExcelUtils.exportExcel(excel)) {
				result.setSuccess(true);
				result.setMsg("报表生成成功");
				result.setData(fileName.toString());
				return result;
			}
		} catch (Exception e) {
			logger.error("传统短信日统计报表生成失败", e);
		}
		return result;
	}

	// 短信导出实时检查数据
	@RequestMapping(value = "/checkUSSDDayNum")
	@ResponseBody
	public AjaxResult checkUSSDDayNum() throws Exception {
		AjaxResult ajaxResult = new AjaxResult();

		Map<String, String> params = ServletUtil.getFormData(request);
		params.put("clientid", ServletUtil.getLoginUserInfo(request).getClientId());
		String start_time = params.get("start_time");
		String end_time = params.get("end_time");

		// 设置默认时间
		if (start_time == null || end_time == null) {
			DateTime dt = DateTime.now();
			dt = dt.minusDays(1);
			start_time = dt.withMillisOfDay(0).toString("yyyy-MM-dd HH:mm");
			end_time = dt.toString("yyyy-MM-dd HH:mm");
			params.put("start_time", start_time);
			params.put("end_time", end_time);
		}

		Map<String, Object> queryParams = new HashMap<>();
		for (Map.Entry e : params.entrySet()) {
			queryParams.put(e.getKey().toString(), e.getValue());
		}
		List<Integer> productTypes = new ArrayList<>();
		productTypes.add(Integer.valueOf(Constant.OrderProductType.USSD.getValue()));
		queryParams.put("productTypes", productTypes);
		queryParams.put("startDate", Integer.valueOf(start_time.replace("-", "")));
		queryParams.put("endDate", Integer.valueOf(end_time.replace("-", "")));

		// 查询短信记录数量
		if (bussinessService.exportBussinessDataCount(queryParams) == 0) {
			ajaxResult.setIsSuccess(false);
		} else {
			ajaxResult.setIsSuccess(true);
		}

		return ajaxResult;
	}

	/**
	 * USSD月统计
	 */
	@RequestMapping(value = "/ussdmonthly", method = RequestMethod.GET)
	public ModelAndView ussdmonthly(HttpServletRequest request, ModelAndView mv, HttpSession session) throws Exception {
		SmsAccountModelPo accountModel = ServletUtil.getLoginUserInfo(request);

		// 月发送数据分页查询
		Map<String, String> params = ServletUtil.getFormData(request);
		params.put("clientid", accountModel.getClientId());

		Map<String, Object> queryParams = new HashMap<>();
		for (Map.Entry e : params.entrySet()) {
			queryParams.put(e.getKey().toString(), e.getValue());
		}
		List<Integer> productTypes = new ArrayList<>();
		productTypes.add(Integer.valueOf(Constant.OrderProductType.USSD.getValue()));
		queryParams.put("productTypes", productTypes);

		queryParams.put("isOem", ServletUtil.isOem(accountModel));
		PageContainer monthStatPage = bussinessService.queryMonthStat(queryParams);
		mv.addObject("monthStatPage", monthStatPage);

		mv.addObject("accountModel", accountModel);
		everyPageShouldHave(mv, request.getServerName(), "/mms/ussdday", request);
		mv.setViewName("/mms/ussdmonthly");
		return mv;
	}

	/**
	 * 导出USSD月统计
	 *
	 * @param request
	 * @param mv
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/exportussdmonthly")
	@ResponseBody
	public ResultVO exportUssdMonthly(HttpServletRequest request, ModelAndView mv) throws Exception {
		ResultVO result = new ResultVO();

		// 代理商信息
		SmsAccountModelPo accountModel = ServletUtil.getLoginUserInfo(request);
		mv.addObject("accountModel", accountModel);

		try {
			Map params = new HashMap();
			params.put("clientid", accountModel.getClientId());
			List<Integer> productTypes = new ArrayList<>();
			productTypes.add(Integer.valueOf(Constant.OrderProductType.USSD.getValue()));
			params.put("productTypes", productTypes);
			params.put("limit", "LIMIT 0 , 60000");

			params.put("isOem", ServletUtil.isOem(accountModel));
			List<Map<String, Object>> data = bussinessService.exportMonthStat(params);
			if (data == null || data.size() < 1) {
				result.setMsg("根据条件查询到的记录数为0，导出Excel文件失败");
				return result;
			}
			StringBuffer fileName = new StringBuffer();
			fileName.append("USSD月统计");
			Excel excel = new Excel();
			excel.setTitle(fileName.toString());
			fileName.append(".xls");
			String filePath = ConfigUtils.temp_file_dir + "/" + accountModel.getClientId() + "/" + fileName.toString();
			excel.setFilePath(filePath);
			excel.addHeader(10, "序号", "rownum");
			excel.addHeader(20, "日期", "date");
			excel.addHeader(20, "提交条数", "usersmstotal");
			excel.addHeader(20, "发送条数", "sendTotal");
			excel.addHeader(20, "成功条数", "successTotal");
			excel.addHeader(20, "平台拦截条数", "interceptTotal");
			excel.addHeader(20, "失败条数", "failTotal");
			excel.addHeader(20, "计费条数", "chargeTotal");

			excel.setShowRownum(false);
			int i = 1;
			for (Map da : data) {
				da.put("rownum", i++);
			}
			excel.setDataList(data);
			if (ExcelUtils.exportExcel(excel)) {
				result.setSuccess(true);
				result.setMsg("报表生成成功");
				result.setData(fileName.toString());
				return result;
			}
		} catch (Exception e) {
			logger.error("传统短信月统计报表生成失败", e);
		}
		return result;
	}

	/**
	 * 闪信日统计
	 */
	@RequestMapping(value = "/flashday", method = RequestMethod.GET)
	public ModelAndView flashday(HttpServletRequest request, ModelAndView mv, HttpSession session) throws Exception {
		SmsAccountModelPo accountModel = ServletUtil.getLoginUserInfo(request);

		// 日发送数据分页查询
		Map<String, String> params = ServletUtil.getFormData(request);

		String start_time = params.get("start_time");
		String end_time = params.get("end_time");
		// 设置默认时间
		if (start_time == null || end_time == null) {
			DateTime dt = DateTime.now();
			dt = dt.minusDays(1);
			start_time = dt.minusMonths(3).toString("yyyy-MM-dd");
			end_time = dt.toString("yyyy-MM-dd");
			params.put("start_time", start_time);
			params.put("end_time", end_time);
		}
		request.setAttribute("start_time", start_time);
		request.setAttribute("end_time", end_time);
		DateTime dt = DateTime.now();
		dt = dt.minusDays(1);
		request.setAttribute("min_time", dt.minusMonths(3).toString("yyyy-MM-dd"));
		request.setAttribute("max_time", dt.toString("yyyy-MM-dd"));

		params.put("clientid", ServletUtil.getLoginUserInfo(request).getClientId());

		Map<String, Object> queryParams = new HashMap<>();
		for (Map.Entry e : params.entrySet()) {
			queryParams.put(e.getKey().toString(), e.getValue());
		}
		List<Integer> productTypes = new ArrayList<>();
		productTypes.add(Integer.valueOf(Constant.OrderProductType.FLASH.getValue()));
		queryParams.put("productTypes", productTypes);
		queryParams.put("startDate", Integer.valueOf(start_time.replace("-", "")));
		queryParams.put("endDate", Integer.valueOf(end_time.replace("-", "")));
		queryParams.put("isOem", ServletUtil.isOem(accountModel));
		PageContainer dayStatPage = bussinessService.queryDayStat(queryParams);
		mv.addObject("dayStatPage", dayStatPage);

		mv.addObject("accountModel", accountModel);
		everyPageShouldHave(mv, request.getServerName(), "/mms/ussdday", request);
		mv.setViewName("/mms/flashday");
		return mv;
	}

	/**
	 * 导出闪信日统计
	 *
	 * @param request
	 * @param mv
	 * @param session
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/exportFlashDayStat")
	@ResponseBody
	public ResultVO exportFlashDayStat(HttpServletRequest request, ModelAndView mv, HttpSession session)
			throws Exception {
		ResultVO result = new ResultVO();

		// 代理商信息
		SmsAccountModelPo accountModel = ServletUtil.getLoginUserInfo(request);
		mv.addObject("accountModel", accountModel);

		Map<String, String> params = ServletUtil.getFormData(request);

		String start_time = params.get("start_time");
		String end_time = params.get("end_time");
		// 设置默认时间
		if (start_time == null || end_time == null) {
			DateTime dt = DateTime.now();
			dt = dt.minusDays(1);
			start_time = dt.minusDays(45).toString("yyyy-MM-dd");
			end_time = dt.toString("yyyy-MM-dd");
			params.put("start_time", start_time);
			params.put("end_time", end_time);
		}
		request.setAttribute("start_time", start_time);
		request.setAttribute("end_time", end_time);

		Map<String, Object> queryParams = new HashMap<>();
		for (Map.Entry e : params.entrySet()) {
			queryParams.put(e.getKey().toString(), e.getValue());
		}

		queryParams.put("clientid", accountModel.getClientId());

		List<Integer> productTypes = new ArrayList<>();
		productTypes.add(Integer.valueOf(Constant.OrderProductType.FLASH.getValue()));
		queryParams.put("productTypes", productTypes);
		queryParams.put("startDate", Integer.valueOf(start_time.replace("-", "")));
		queryParams.put("endDate", Integer.valueOf(end_time.replace("-", "")));

		try {
			queryParams.put("limit", "LIMIT 0 , 60000");
			queryParams.put("isOem", ServletUtil.isOem(accountModel));
			List<Map<String, Object>> data = bussinessService.exportBussinessData(queryParams);
			if (data == null || data.size() < 1) {
				result.setMsg("根据条件查询到的记录数为0，导出Excel文件失败");
				return result;
			}
			StringBuffer fileName = new StringBuffer();
			fileName.append("闪信日统计");
			Excel excel = new Excel();
			fileName.append(queryParams.get("startDate")).append("-").append(queryParams.get("endDate"));
			excel.setTitle(fileName.toString());
			fileName.append(".xls");
			String filePath = ConfigUtils.temp_file_dir + "/" + accountModel.getClientId() + "/" + fileName.toString();
			excel.setFilePath(filePath);
			excel.addHeader(10, "序号", "rownum");
			excel.addHeader(20, "日期", "date");
			excel.addHeader(20, "提交条数", "usersmstotal");
			excel.addHeader(20, "发送条数", "sendTotal");
			excel.addHeader(20, "成功条数", "successTotal");
			excel.addHeader(20, "平台拦截条数", "interceptTotal");
			excel.addHeader(20, "失败条数", "failTotal");
			excel.addHeader(20, "计费条数", "chargeTotal");

			excel.setShowRownum(false);
			int i = 1;
			for (Map da : data) {
				da.put("rownum", i++);
			}
			excel.setDataList(data);
			if (ExcelUtils.exportExcel(excel)) {
				result.setSuccess(true);
				result.setMsg("报表生成成功");
				result.setData(fileName.toString());
				return result;
			}
		} catch (Exception e) {
			logger.error("传统短信日统计报表生成失败", e);
		}
		return result;
	}

	// 短信导出实时检查数据
	@RequestMapping(value = "/checkFlashDayNum")
	@ResponseBody
	public AjaxResult checkFlashDayNum() throws Exception {
		AjaxResult ajaxResult = new AjaxResult();

		Map<String, String> params = ServletUtil.getFormData(request);
		params.put("clientid", ServletUtil.getLoginUserInfo(request).getClientId());
		String start_time = params.get("start_time");
		String end_time = params.get("end_time");

		// 设置默认时间
		if (start_time == null || end_time == null) {
			DateTime dt = DateTime.now();
			dt = dt.minusDays(1);
			start_time = dt.withMillisOfDay(0).toString("yyyy-MM-dd HH:mm");
			end_time = dt.toString("yyyy-MM-dd HH:mm");
			params.put("start_time", start_time);
			params.put("end_time", end_time);
		}

		Map<String, Object> queryParams = new HashMap<>();
		for (Map.Entry e : params.entrySet()) {
			queryParams.put(e.getKey().toString(), e.getValue());
		}
		List<Integer> productTypes = new ArrayList<>();
		productTypes.add(Integer.valueOf(Constant.OrderProductType.FLASH.getValue()));
		queryParams.put("productTypes", productTypes);
		queryParams.put("startDate", Integer.valueOf(start_time.replace("-", "")));
		queryParams.put("endDate", Integer.valueOf(end_time.replace("-", "")));
		// 查询短信记录数量
		if (bussinessService.exportBussinessDataCount(queryParams) == 0) {
			ajaxResult.setIsSuccess(false);
		} else {
			ajaxResult.setIsSuccess(true);
		}

		return ajaxResult;
	}

	/**
	 * 闪信月统计
	 */
	@RequestMapping(value = "/flashmonthly", method = RequestMethod.GET)
	public ModelAndView flashmonthly(HttpServletRequest request, ModelAndView mv, HttpSession session)
			throws Exception {
		SmsAccountModelPo accountModel = ServletUtil.getLoginUserInfo(request);

		// 月发送数据分页查询
		Map<String, String> params = ServletUtil.getFormData(request);
		params.put("clientid", accountModel.getClientId());

		Map<String, Object> queryParams = new HashMap<>();
		for (Map.Entry e : params.entrySet()) {
			queryParams.put(e.getKey().toString(), e.getValue());
		}
		List<Integer> productTypes = new ArrayList<>();
		productTypes.add(Integer.valueOf(Constant.OrderProductType.FLASH.getValue()));
		queryParams.put("productTypes", productTypes);
		queryParams.put("isOem", ServletUtil.isOem(accountModel));
		PageContainer monthStatPage = bussinessService.queryMonthStat(queryParams);
		mv.addObject("monthStatPage", monthStatPage);

		mv.addObject("accountModel", accountModel);
		everyPageShouldHave(mv, request.getServerName(), "/mms/ussdday", request);
		mv.setViewName("/mms/flashmonthly");
		return mv;
	}

	/**
	 * 导出闪信月统计
	 *
	 * @param request
	 * @param mv
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/exportFlashMonthly")
	@ResponseBody
	public ResultVO exportFlashMonthly(HttpServletRequest request, ModelAndView mv) throws Exception {

		ResultVO result = new ResultVO();

		// 代理商信息
		SmsAccountModelPo accountModel = ServletUtil.getLoginUserInfo(request);
		mv.addObject("accountModel", accountModel);

		try {
			Map params = new HashMap();
			params.put("clientid", accountModel.getClientId());
			List<Integer> productTypes = new ArrayList<>();
			productTypes.add(Integer.valueOf(Constant.OrderProductType.FLASH.getValue()));
			params.put("productTypes", productTypes);
			params.put("limit", "LIMIT 0 , 60000");
			params.put("isOem", ServletUtil.isOem(accountModel));
			List<Map<String, Object>> data = bussinessService.exportMonthStat(params);
			if (data == null || data.size() < 1) {
				result.setMsg("根据条件查询到的记录数为0，导出Excel文件失败");
				return result;
			}
			StringBuffer fileName = new StringBuffer();
			fileName.append("闪信月统计");
			Excel excel = new Excel();
			excel.setTitle(fileName.toString());
			fileName.append(".xls");
			String filePath = ConfigUtils.temp_file_dir + "/" + accountModel.getClientId() + "/" + fileName.toString();
			excel.setFilePath(filePath);
			excel.addHeader(10, "序号", "rownum");
			excel.addHeader(20, "日期", "date");
			excel.addHeader(20, "提交条数", "usersmstotal");
			excel.addHeader(20, "发送条数", "sendTotal");
			excel.addHeader(20, "成功条数", "successTotal");
			excel.addHeader(20, "平台拦截条数", "interceptTotal");
			excel.addHeader(20, "失败条数", "failTotal");
			excel.addHeader(20, "计费条数", "chargeTotal");

			excel.setShowRownum(false);
			int i = 1;
			for (Map da : data) {
				da.put("rownum", i++);
			}
			excel.setDataList(data);
			if (ExcelUtils.exportExcel(excel)) {
				result.setSuccess(true);
				result.setMsg("报表生成成功");
				result.setData(fileName.toString());
				return result;
			}
		} catch (Exception e) {
			logger.error("传统短信月统计报表生成失败", e);
		}
		return result;
	}

	@RequestMapping(value = "/template/list")
	@ResponseBody
	public PageContainer list(HttpServletRequest request) {
		SmsAccountModelPo accountModel = (SmsAccountModelPo) request.getSession()
				.getAttribute(Constant.LOGIN_USER_INFO);
		PageContainer pageContainer = new PageContainer();
		Map<Object, Object> queryParams = new HashMap<>();
		Map<String, String> params = ServletUtil.getFormData(request);
		for (Map.Entry e : params.entrySet()) {
			queryParams.put(e.getKey(), e.getValue());
		}
		List<Integer> types = new ArrayList<>();
		if (!StringUtils.isEmpty(params.get("types[]"))) {
			String[] smsType = params.get("types[]").split(",");
			for (String st : smsType) {
				types.add(Integer.valueOf(st));
			}
			queryParams.put("types", types);
		}
		queryParams.put("client_id", accountModel.getClientId());
		try {
			if (queryParams.get("pageRowCount") == null) {
				queryParams.put("pageRowCount", "10");
			}
			pageContainer = templateService.queryList(queryParams);
			return pageContainer;
		} catch (Exception e) {
			logger.error("获取模版列表失败", e);
		}
		return pageContainer;
	}

	@RequestMapping(value = "/template/temporary")
	@ResponseBody
	public PageContainer temporaryList(HttpServletRequest request) {
		SmsAccountModelPo accountModel = (SmsAccountModelPo) request.getSession()
				.getAttribute(Constant.LOGIN_USER_INFO);
		PageContainer pageContainer = new PageContainer();
		Map<Object, Object> queryParams = new HashMap<>();
		Map<String, String> params = ServletUtil.getFormData(request);
		for (Map.Entry e : params.entrySet()) {
			queryParams.put(e.getKey(), e.getValue());
		}
		List<Integer> types = new ArrayList<>();
		if (!StringUtils.isEmpty(params.get("types[]"))) {
			String[] smsType = params.get("types[]").split(",");
			for (String st : smsType) {
				types.add(Integer.valueOf(st));
			}
			queryParams.put("types", types);
		}
		queryParams.put("client_id", accountModel.getClientId());
		try {
			pageContainer = templateService.queryTemporaryList(queryParams);
			for (Map<String, Object> template : pageContainer.getList()) {
				int type = (int) template.get("type");
				int status = (int) template.get("check_status");
				if (status == 0) { // 待审核
					Map<Object, Object> tempParams = new HashMap<>();
					tempParams.put("template_id", template.get("template_id"));
					tempParams.put("check_status", 1); // 审核通过的原模版
					tempParams.put("client_id", accountModel.getClientId()); // 审核通过的原模版
					Template templateHis = templateService.query(tempParams);
					String[] opera = new String[] { "编辑" };
					if (templateHis != null) {
						opera = new String[] { "编辑", "查看历史" };
					}
					template.put("opera", opera);
				}
				if (status == 1 || status == 3) { // 审核通过 或 审核不通过
					String[] opera = new String[] { "修改" };
					if ((status == 1 || status == 2 || status == 3) && (type == 7 || type == 8)) { // 审核通过、转审、审核不通过的USSD模版和闪信模版不能修改
						opera = new String[] { "" };
					}
					template.put("opera", opera);
				}
			}
			return pageContainer;
		} catch (Exception e) {
			logger.error("获取模版列表失败", e);
		}
		return pageContainer;
	}

	@RequestMapping(value = "/template/get")
	@ResponseBody
	public Template get(HttpServletRequest request) {
		SmsAccountModelPo accountModel = (SmsAccountModelPo) request.getSession()
				.getAttribute(Constant.LOGIN_USER_INFO);
		Map<Object, Object> queryParams = new HashMap<>();
		Map<String, String> params = ServletUtil.getFormData(request);
		for (Map.Entry e : params.entrySet()) {
			queryParams.put(e.getKey(), e.getValue());
		}
		queryParams.put("check_status", 1); // 审核通过的原模版
		queryParams.put("client_id", accountModel.getClientId()); // 审核通过的原模版
		Template templateHis = new Template();
		try {
			templateHis = templateService.query(queryParams);
		} catch (Exception e) {
			logger.error("获取模版失败", e);
		}
		return templateHis;
	}

	@RequestMapping(value = "/template/add")
	@ResponseBody
	public ResultVO add(@Valid Template template, BindingResult errorResult, HttpServletRequest request) {
		ResultVO result = new ResultVO();
		if (errorResult.hasErrors()) {
			for (ObjectError error : errorResult.getAllErrors()) {
				result.setFail(true);
				result.setMsg(error.getDefaultMessage());
				return result;
			}
		}
		if ((template.getSign() + template.getContent()).length() > 498) {
			result.setFail(true);
			result.setMsg("短信签名和短信内容总长度不能超过500个字符！");
			return result;
		}
		SmsAccountModelPo accountModel = (SmsAccountModelPo) request.getSession()
				.getAttribute(Constant.LOGIN_USER_INFO);
		template.setClient_id(accountModel.getClientId());
		template.setAgent_id(accountModel.getAgentId());
		result = templateService.save(template);
		return result;
	}

	@RequestMapping(value = "/template/update")
	@ResponseBody
	public ResultVO update(@Valid Template template, BindingResult errorResult, HttpServletRequest request) {
		ResultVO result = new ResultVO();
		if (errorResult.hasErrors()) {
			for (ObjectError error : errorResult.getAllErrors()) {
				result.setFail(true);
				result.setMsg(error.getDefaultMessage());
				return result;
			}
		}
		if ((template.getSign() + template.getContent()).length() > 498) {
			result.setFail(true);
			result.setMsg("短信签名和短信内容总长度不能超过500个字符！");
			return result;
		}
		SmsAccountModelPo accountModel = (SmsAccountModelPo) request.getSession()
				.getAttribute(Constant.LOGIN_USER_INFO);
		template.setClient_id(accountModel.getClientId());
		template.setAgent_id(accountModel.getAgentId());
		try {
			result = templateService.update(template);
		} catch (Exception e) {
			if (e instanceof SmsException) {
				logger.debug("更新失败", e);
				result.setMsg(e.getMessage());
				return result;
			} else {
				logger.error("更新异常", e);
				result.setMsg("更新失败，请刷新页面后重试");
				return result;
			}
		}
		if (result.isSuccess()) {
			Map<Object, Object> queryParams = new HashMap<>();
			queryParams.put("template_id", template.getTemplate_id()); // 审核通过的原模版
			try {
				Template lastest = templateService.queryTemporary(queryParams);
				int status = lastest.getCheck_status();
				if (status == 0) { // 待审核
					Map<Object, Object> tempParams = new HashMap<>();
					tempParams.put("template_id", lastest.getTemplate_id());
					tempParams.put("check_status", 1); // 审核通过的原模版
					tempParams.put("client_id", accountModel.getClientId()); // 审核通过的原模版
					Template templateHis = templateService.query(tempParams);
					String[] opera = new String[] { "编辑" };
					if (templateHis != null) {
						opera = new String[] { "编辑", "查看历史" };
					}
					lastest.setOpera(opera);
				}
				if (status == 1 || status == 3) { // 审核通过 或 审核不通过
					String[] opera = new String[] { "修改" };
					lastest.setOpera(opera);
				}
				result.setData(lastest);
			} catch (Exception e) {
				result.setMsg("更新异常，请刷新页面后重试");
				return result;
			}
		}
		return result;
	}



}
