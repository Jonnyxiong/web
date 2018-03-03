package com.ucpaas.sms.controller;

import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.enums.ProductType;
import com.jsmsframework.common.util.BeanUtil;
import com.jsmsframework.product.dto.JsmsProductInfoDTO;
import com.jsmsframework.product.entity.JsmsProductInfo;
import com.jsmsframework.product.service.JsmsProductInfoService;
import com.ucpaas.sms.common.AjaxResult;
import com.ucpaas.sms.common.util.StringUtils;
import com.ucpaas.sms.constant.Constant;
import com.ucpaas.sms.entity.message.ClientBalanceAlarm;
import com.ucpaas.sms.model.PageBean;
import com.ucpaas.sms.model.SmsAccountModel;
import com.ucpaas.sms.model.param.PageParam;
import com.ucpaas.sms.model.po.SmsAccountModelPo;
import com.ucpaas.sms.service.ClientBalanceAlarmService;
import com.ucpaas.sms.service.LoginService;
import com.ucpaas.sms.service.OrderManagerService;
import com.ucpaas.sms.util.DateUtil;
import com.ucpaas.sms.util.web.ServletUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("/console")
public class ConsoleController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(ConsoleController.class);

	@Autowired
	private ClientBalanceAlarmService clientBalanceAlarmService;

	@Autowired
	private LoginService loginService;

	@Autowired
	private JsmsProductInfoService jsmsProductInfoService;

	@Autowired
	private OrderManagerService orderManagerService;

	private Map<String, Object> getOemConsoleData(SmsAccountModelPo accountModel) {
		Map<String, Object> result = new HashMap<>();
		List<Map<String, Object>> productTypes = masterDao.getSearchList("smsOemProduct.queryProductTypeList", null);

		// 产品类型
		result.put("productTypes", productTypes);

		String client_id = accountModel.getClientId();
		List<Map<String, Object>> remains = masterDao.getSearchList("smsClientPool.getRemainNumber", client_id);
		for (Map<String, Object> ramainMap : remains) {
			int productInt = (int) ramainMap.get("product_type");
			// 行业
			if (productInt == 0) {
				result.put("remainVerify", ramainMap.get("remain_number"));
			}
			// 营销
			if (productInt == 1) {
				result.put("remainMarket", ramainMap.get("remain_number"));
			}
			// 国际
			if (productInt == 2) {
				result.put("remainIsms", ramainMap.get("remain_amount").toString());
			}
			// 验证码
			if (productInt == 3) {
				result.put("remainCode", ramainMap.get("remain_number"));
			}
			// 通知
			if (productInt == 4) {
				result.put("remainNote", ramainMap.get("remain_number"));
			}
		}

		// 查询到期的短信
		Map<String, Object> dueParams = new HashMap<>();
		dueParams.put("due_date", DateUtil.getDateFromToday(30));
		dueParams.put("client_id", client_id);
		List<Map<String, Object>> dues = masterDao.getSearchList("smsClientPool.getDueNumber", dueParams);
		for (Map<String, Object> dueMap : dues) {
			int productInt = (int) dueMap.get("product_type");
			// 行业
			if (productInt == 0) {
				result.put("dueVerify", dueMap.get("due_number"));
			}
			// 营销
			if (productInt == 1) {
				result.put("dueMarket", dueMap.get("due_number"));
			}
			// 国际
			if (productInt == 2) {
				result.put("dueIsms", dueMap.get("due_amount"));
			}
			// 验证码
			if (productInt == 3) {
				result.put("dueCode", dueMap.get("due_number"));
			}
			// 通知
			if (productInt == 4) {
				result.put("dueNote", dueMap.get("due_number"));
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getBrConsoleData(SmsAccountModelPo accountModel, PageParam pageParam) {
		Map<String, Object> result = new HashMap<>();

		// 查询当前帐号最新的信息
		SmsAccountModel dbAccount = loginService.getLoginClientInfo(accountModel.getClientId());

		List<Integer> productTypes;
		// 2为SMPP协议，3为CMPP协议，4为SGIP协议，5为SMGP协议，6为HTTPS协议
		if (dbAccount.getSmsfrom() == 6) {
			productTypes = Arrays.asList(ProductType.行业.getValue(), ProductType.营销.getValue(),
					ProductType.国际.getValue(), ProductType.验证码.getValue(), ProductType.通知.getValue(),
					ProductType.USSD.getValue(), ProductType.闪信.getValue());
		} else {
			// 通知短信(0, "通知短信"), 验证码短信(4, "验证码短信"), 营销短信(5, "营销短信"), 告警短信(6,
			// "告警短信"), USSD(7, "USSD"), 闪信(8, "闪信");
			if (dbAccount.getSmstype() == 0 || dbAccount.getSmstype() == 4) {
				productTypes = Arrays.asList(ProductType.行业.getValue(), ProductType.国际.getValue(),
						ProductType.验证码.getValue(), ProductType.通知.getValue());
			} else {
				productTypes = Arrays.asList(ProductType.营销.getValue(), ProductType.国际.getValue());
			}
		}

		JsmsPage jsmsPage = new JsmsPage();
		jsmsPage.setRows(pageParam.getPageSize());
		jsmsPage.setPage(pageParam.getGoalPage());
		jsmsPage.getParams().put("productTypes", productTypes);
		jsmsPage = jsmsProductInfoService.queryProxiedList(jsmsPage, accountModel.getAgentId(),
				accountModel.getClientId());

		List<JsmsProductInfoDTO> jsmsProductInfoDTOS = new ArrayList<>();
		if (jsmsPage.getData() != null && jsmsPage.getData().size() > 0) {
			List<JsmsProductInfo> jsmsProductInfos = jsmsPage.getData();
			for (JsmsProductInfo jsmsProductInfo : jsmsProductInfos) {
				JsmsProductInfoDTO dto = new JsmsProductInfoDTO();
				BeanUtil.copyProperties(jsmsProductInfo, dto);
				if (dto.getProductType().intValue() == 2){
					dto.setProductPrice(dto.getProductPrice().setScale(4, 4));
					dto.setTotalPriceStr(dto.getQuantity().multiply(dto.getProductPrice()).setScale(4, 4).toString());
				}
				jsmsProductInfoDTOS.add(dto);
			}
		}

		jsmsPage.setData(jsmsProductInfoDTOS);
		accountModel.setOauthStatus(dbAccount.getOauthStatus());

		List<Map> remainQuantityList = orderManagerService.getOrderRemainQuantity(dbAccount);
		for (Map map : remainQuantityList) {
			// 产品类型，0：行业，1：营销，2：国际，7：USSD，8：闪信，9：挂机短信，其中0和1为普通短信，2为国际短信
			int productType = (int) map.get("productType");
			if (productType == ProductType.行业.getValue().intValue()) {
				result.put("remainHangYe", map.get("remainQuantity"));
			} else if (productType == ProductType.验证码.getValue().intValue()) {
				result.put("remainYanZhengMa", map.get("remainQuantity"));
			} else if (productType == ProductType.通知.getValue().intValue()) {
				result.put("remainTongZhi", map.get("remainQuantity"));
			} else if (productType == ProductType.营销.getValue().intValue()) {
				result.put("remainYingXiao", map.get("remainQuantity"));
			} else if (productType == ProductType.国际.getValue().intValue()) {
				result.put("remainGuoJi", map.get("remainQuantity"));
			} else if (productType == ProductType.USSD.getValue().intValue()) {
				result.put("remainUSSD", map.get("remainQuantity"));
			} else if (productType == ProductType.闪信.getValue().intValue()) {
				result.put("remainShanXin", map.get("remainQuantity"));
			}
		}

		result.put("jsmsPage", jsmsPage);
		return result;
	}

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView index(ModelAndView mv, HttpServletRequest request, PageParam pageParam) throws Exception {
		SmsAccountModelPo accountModel = ServletUtil.getLoginUserInfo(request);
		mv.addObject("accountModel", accountModel);

		// 后付费跳转至统计页面
		if (!ServletUtil.isYuFu(accountModel)) {
			return new ModelAndView("redirect:/bussiness/statistics");
		}

		// 返回主页
		everyPageShouldHave(mv, request.getServerName(), "/console", request);
		mv.setViewName("console");

		boolean isOem = ServletUtil.isOem(accountModel);
		if (isOem) {
			mv.addAllObjects(getOemConsoleData(accountModel));
		} else {
			mv.addAllObjects(getBrConsoleData(accountModel, pageParam));
		}

		mv.addObject("isOem", isOem);
		return mv;
	}

	@RequestMapping(value = "/quit", method = RequestMethod.GET)
	public void quit(HttpServletRequest request, HttpServletResponse response) throws Exception {
		request.getSession().setAttribute(Constant.LOGIN_USER_INFO, null);
		try {
			response.sendRedirect("/login");
		} catch (IOException e) {
			logger.error("springmvc转发/login失败", e);
			throw e;
		}
	}

	@RequestMapping("/isSessionValid")
	@ResponseBody
	public AjaxResult isSessionValid() {
		AjaxResult result = new AjaxResult();
		result.setIsSuccess(true);
		return result;
	}

	@RequestMapping("/remainList")
	public ModelAndView remainList(HttpServletRequest request, ModelAndView mv,
			@RequestParam(required = false) Integer productType, @RequestParam(required = false) Integer goalPage,
			@RequestParam(required = false) Integer pageSize) throws Exception {

		SmsAccountModel accountModel = ServletUtil.getLoginUserInfo(request);
		if (productType == null)
			productType = -1;
		if (goalPage == null)
			goalPage = 1;
		if (pageSize == null)
			pageSize = 10;
		switch (productType) {
		case 0:
			mv.addObject("title", "行业余额");
			break;
		case 1:
			mv.addObject("title", "营销短信余额");
			break;
		case 2:
			mv.addObject("title", "国际短信余额");
			break;
		case 3:
			mv.addObject("title", "验证码短信余额");
			break;
		case 4:
			mv.addObject("title", "通知短信余额");
			break;
		case 7:
			mv.addObject("title", "USSD短信余额");
			break;
		case 8:
			mv.addObject("title", "闪信短信余额");
			break;
		case 9:
			mv.addObject("title", "挂机短信余额");
			break;
		default:
			mv.addObject("title", "短信余额");
			break;
		}

		PageParam pageParam = new PageParam();
		pageParam.setGoalPage(goalPage);
		pageParam.setPageSize(pageSize);
		Map<String, Object> orderParam = new HashMap<>();
		orderParam.put("clientId", accountModel.getClientId());
		orderParam.put("productType", productType);
		PageBean pageBean = orderManagerService.listRemain(orderParam, pageParam);
		mv.addObject("accountModel", accountModel);
		mv.addObject("pageBean", pageBean);
		mv.addObject("productType", productType);
		everyPageShouldHave(mv, request.getServerName(), "/console", request);
		mv.setViewName("console/remainList");
		return mv;
	}

	@RequestMapping(value = "/expire")
	public ModelAndView expire(String productType, HttpSession session, ModelAndView mv, HttpServletRequest request,
			PageParam pageParam) throws Exception {
		SmsAccountModelPo accountModel = (SmsAccountModelPo) session.getAttribute(Constant.LOGIN_USER_INFO);
		String clientId = accountModel.getClientId();
		mv.addObject("accountModel", accountModel);

		Map<String, Object> sqlParams = new HashMap<>();
		sqlParams.put("client_id", clientId);
		sqlParams.put("startIndex", pageParam.getStartIndex());
		sqlParams.put("pageSize", pageParam.getPageSize());
		sqlParams.put("productType", productType);
		int totalRows = masterDao.selectOne("smsClientPool.queryExpireCount", sqlParams);
		PageBean pageBean = new PageBean(pageParam.getPageSize(), pageParam.getGoalPage(), totalRows);
		if (totalRows == 0) {
			pageBean.setList(new ArrayList<>());
		} else {
			List<Map<String, Object>> poolList = masterDao.getSearchList("smsClientPool.queryExpirePage", sqlParams);
			pageBean.setList(poolList);
		}

		everyPageShouldHave(mv, request.getServerName(), "/console", request);

		mv.addObject("pageBean", pageBean);
		mv.setViewName("expire");
		return mv;
	}

	@RequestMapping(value = "/balancealarm/save", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> saveBalanceAlarm(@RequestBody ClientBalanceAlarm clientBalanceAlarm) {
		try {
			return clientBalanceAlarmService.saveClientBalanceAlarm(clientBalanceAlarm);
		} catch (Exception e) {
			logger.error("预付费客户余额预警信息设置失败{}", e);
			Map<String, Object> result = new HashMap<>();
			result.put("code", 500);
			result.put("msg", "设置失败！");
			return result;
		}
	}

	@RequestMapping(value = "/balancealarm/info", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getBalanceAlarm(String clientid) {
		try {
			if (StringUtils.isBlank(clientid)) {
				Map<String, Object> result = new HashMap<>();
				result.put("code", 500);
				result.put("msg", "客户ID不能为空！");
				return result;
			}

			ClientBalanceAlarm info = clientBalanceAlarmService.getByClientId(clientid);
			Map<String, Object> result = new HashMap<>();
			result.put("code", 0);
			result.put("msg", "查询成功！");
			result.put("data", info);
			return result;

		} catch (Exception e) {
			logger.error("预付费客户余额预警信息查询失败{}", e);
			Map<String, Object> result = new HashMap<>();
			result.put("code", 500);
			result.put("msg", "查询失败！");
			return result;
		}
	}

	@RequestMapping(value = "/groupOrderListByProductTypeAll")
	@ResponseBody
	public Map groupOrderListByProductType(HttpServletRequest request, ModelAndView mv) {
		SmsAccountModel accountModel = (SmsAccountModel) request.getSession().getAttribute(Constant.LOGIN_USER_INFO);
		Map result = new HashMap();
		Map<String, Object> orderParam = new HashMap<>();
		orderParam.put("clientId", accountModel.getClientId());
		List<Integer> productTypes = new ArrayList<>();

		SmsAccountModel dbAccount = loginService.getLoginClientInfo(accountModel.getClientId());
		if (dbAccount.getSmsfrom() == 6) {
			productTypes = Arrays.asList(ProductType.行业.getValue(), ProductType.营销.getValue(),
					ProductType.国际.getValue(), ProductType.验证码.getValue(), ProductType.通知.getValue(),
					ProductType.USSD.getValue(), ProductType.闪信.getValue());
		} else {
			// 通知短信(0, "通知短信"), 验证码短信(4, "验证码短信"), 营销短信(5, "营销短信"), 告警短信(6,
			// "告警短信"), USSD(7, "USSD"), 闪信(8, "闪信");
			if (dbAccount.getSmstype() == 0 || dbAccount.getSmstype() == 4) {
				productTypes = Arrays.asList(ProductType.行业.getValue(), ProductType.国际.getValue(),
						ProductType.验证码.getValue(), ProductType.通知.getValue());
			} else {
				productTypes = Arrays.asList(ProductType.营销.getValue(), ProductType.国际.getValue());
			}
		}

		if (productTypes == null || productTypes.size() == 0)
			return result;

		List<Map> remainQuantityList = orderManagerService.getOrderRemainQuantity(dbAccount);
		for (Map map : remainQuantityList) {
			// 产品类型，0：行业，1：营销，2：国际，7：USSD，8：闪信，9：挂机短信，其中0和1为普通短信，2为国际短信
			int productType = (int) map.get("productType");
			if (productType == ProductType.行业.getValue().intValue()) {
				result.put("remainHangYe", map.get("remainQuantity"));
			} else if (productType == ProductType.验证码.getValue().intValue()) {
				result.put("remainYanZhengMa", map.get("remainQuantity"));
			} else if (productType == ProductType.通知.getValue().intValue()) {
				result.put("remainTongZhi", map.get("remainQuantity"));
			} else if (productType == ProductType.营销.getValue().intValue()) {
				result.put("remainYingXiao", map.get("remainQuantity"));
			} else if (productType == ProductType.国际.getValue().intValue()) {
				result.put("remainGuoJi", map.get("remainQuantity").toString());
			} else if (productType == ProductType.USSD.getValue().intValue()) {
				result.put("remainUSSD", map.get("remainQuantity"));
			} else if (productType == ProductType.闪信.getValue().intValue()) {
				result.put("remainShanXin", map.get("remainQuantity"));
			}
		}

		boolean isHttp = dbAccount.getSmsfrom() == 6;
		if (isHttp) {
			if (result.get("remainHangYe") == null) {
				result.put("remainHangYe", 0);
			}

			if (result.get("remainYanZhengMa") == null) {
				result.put("remainYanZhengMa", 0);
			}

			if (result.get("remainTongZhi") == null) {
				result.put("remainTongZhi", 0);
			}

			if (result.get("remainYingXiao") == null) {
				result.put("remainYingXiao", 0);
			}

			if (result.get("remainUSSD") == null) {
				result.put("remainUSSD", 0);
			}

			if (result.get("remainShanXin") == null) {
				result.put("remainShanXin", 0);
			}
		} else {
			if (dbAccount.getSmstype() == 0) {
				if (result.get("remainHangYe") == null) {
					result.put("remainHangYe", 0);
				}

				if (result.get("remainTongZhi") == null) {
					result.put("remainTongZhi", 0);
				}

				if (result.get("remainYanZhengMa") != null)
					result.remove("remainYanZhengMa");

				if (result.get("remainYingXiao") != null)
					result.remove("remainYingXiao");

				if (result.get("remainUSSD") != null)
					result.remove("remainUSSD");

				if (result.get("remainShanXin") != null)
					result.remove("remainShanXin");

			} else if (dbAccount.getSmstype() == 4) {
				if (result.get("remainHangYe") == null) {
					result.put("remainHangYe", 0);
				}
				if (result.get("remainYanZhengMa") == null) {
					result.put("remainYanZhengMa", 0);
				}

				if (result.get("remainTongZhi") != null)
					result.remove("remainTongZhi");

				if (result.get("remainYingXiao") != null)
					result.remove("remainYingXiao");

				if (result.get("remainUSSD") != null)
					result.remove("remainUSSD");

				if (result.get("remainShanXin") != null)
					result.remove("remainShanXin");

			} else if (dbAccount.getSmstype() == 5) {
				if (result.get("remainYingXiao") == null) {
					result.put("remainYingXiao", 0);
				}

				if (result.get("remainHangYe") != null)
					result.remove("remainHangYe");

				if (result.get("remainYanZhengMa") != null)
					result.remove("remainYanZhengMa");

				if (result.get("remainTongZhi") != null)
					result.remove("remainTongZhi");

				if (result.get("remainUSSD") != null)
					result.remove("remainUSSD");

				if (result.get("remainShanXin") != null)
					result.remove("remainShanXin");
			} else {
				if (result.get("remainHangYe") != null)
					result.remove("remainHangYe");

				if (result.get("remainYanZhengMa") != null)
					result.remove("remainYanZhengMa");

				if (result.get("remainTongZhi") != null)
					result.remove("remainTongZhi");

				if (result.get("remainYingXiao") != null)
					result.remove("remainYingXiao");

				if (result.get("remainUSSD") != null)
					result.remove("remainUSSD");

				if (result.get("remainShanXin") != null)
					result.remove("remainShanXin");
			}
		}

		if (result.get("remainGuoJi") == null) {
			result.put("remainGuoJi", "0.0000");
		}

		return result;
	}
}
