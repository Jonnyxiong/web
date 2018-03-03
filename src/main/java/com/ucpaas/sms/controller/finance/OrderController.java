package com.ucpaas.sms.controller.finance;

import com.jsmsframework.common.dto.Result;
import com.jsmsframework.common.enums.ClientOrderType;
import com.jsmsframework.order.product.service.JsmsClientOrderProductService;
import com.ucpaas.sms.common.AjaxResult;
import com.ucpaas.sms.constant.Constant;
import com.ucpaas.sms.controller.BaseController;
import com.ucpaas.sms.model.Excel;
import com.ucpaas.sms.model.PageBean;
import com.ucpaas.sms.model.SmsAccountModel;
import com.ucpaas.sms.model.param.GetOrderParam;
import com.ucpaas.sms.model.param.PageParam;
import com.ucpaas.sms.model.param.PlaceOrderParam;
import com.ucpaas.sms.model.po.SmsAccountModelPo;
import com.ucpaas.sms.model.po.SmsClientOrderPo;
import com.ucpaas.sms.model.po.SmsProductInfoPo;
import com.ucpaas.sms.service.OrderManagerService;
import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.util.DateUtil;
import com.ucpaas.sms.util.JsonUtils;
import com.ucpaas.sms.util.web.ServletUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * Created by csy on 2017/9/4.
 */
@Controller
@RequestMapping("/finance/order")
public class OrderController extends BaseController {
	private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

	@Autowired
	private OrderManagerService orderManagerService;

	@Autowired
	private JsmsClientOrderProductService jsmsClientOrderProductService;

	@RequestMapping(value = "/createOrder", method = RequestMethod.POST)
	@ResponseBody
	public AjaxResult createOrder(HttpServletRequest request, String placeOrderParamStr) {
		PlaceOrderParam placeOrderParam = JsonUtils.toObject(placeOrderParamStr, PlaceOrderParam.class);
		SmsAccountModelPo accountModel = (SmsAccountModelPo) request.getSession()
				.getAttribute(Constant.LOGIN_USER_INFO);
		placeOrderParam.setAgentId(accountModel.getAgentId());
		List<String> productCodes = new ArrayList<>();
		List<Integer> purchaseNums = new ArrayList<>();
		List<Long> orderIds = new ArrayList<>();
		for (SmsProductInfoPo smsProductInfoPo : placeOrderParam.getProductInfoPoList()) {
			productCodes.add(smsProductInfoPo.getProductCode());
			purchaseNums.add(smsProductInfoPo.getProductNum().intValue());
			orderIds.add(orderManagerService.getOrderId());
		}
		AjaxResult ajaxResult = new AjaxResult();
		try {
			Result result = jsmsClientOrderProductService.createOrder(productCodes, purchaseNums, orderIds,
					ClientOrderType.客户购买, accountModel.getAgentId(), accountModel.getClientId());
			ajaxResult.setIsSuccess(result.isSuccess());
			ajaxResult.setMessage(result.getMsg());
		} catch (Exception e) {
			logger.error("创建订单失败", e);
			String clazz = e.getClass().toString();
			ajaxResult.setIsSuccess(false);
			if (clazz.contains("jsms") || clazz.contains("Jsms")) {
				ajaxResult.setMessage(e.getMessage());
			} else {
				ajaxResult.setMessage("创建订单失败");
			}
			return ajaxResult;
		}
		return ajaxResult;
	}

	/**
	 * 我的订单
	 *
	 * @param request
	 * @param getOrderParam
	 * @param pageParam
	 * @param mv
	 * @return
	 */
	@RequestMapping(value = "/list")
	public ModelAndView list(HttpServletRequest request, GetOrderParam getOrderParam, PageParam pageParam,
			ModelAndView mv, String state) throws Exception {
		if (StringUtils.isNotBlank(state))
		{
			getOrderParam.setStatus(state);
		}
		SmsAccountModel accountModel = ServletUtil.getLoginUserInfo(request);
		getOrderParam.setClientId(accountModel.getClientId());
		PageBean pageBean = orderManagerService.getOrderListInfo(getOrderParam, pageParam);
		everyPageShouldHave(mv, request.getServerName(), "/finance/order/list", request);
		mv.addObject("pageBean", pageBean);
		mv.addObject("accountModel", accountModel);
		mv.addObject("getOrderParam", getOrderParam);

		mv.setViewName("finance/order/list");
		return mv;
	}

	@RequestMapping(value = "/getOrderDetail")
	public ModelAndView getOrderDetailBySubId(HttpServletRequest request, ModelAndView mv, String subId)
			throws Exception {
		SmsClientOrderPo smsClientOrderPo = orderManagerService.getOrderDetailBySubId(subId);
		mv.addObject("orderDetial", smsClientOrderPo);
		everyPageShouldHave(mv, request.getServerName(), "/finance/order/getOrderDetail", request);
		mv.addObject("accountModel", ServletUtil.getLoginUserInfo(request));
		mv.setViewName("finance/order/orderDetail");
		return mv;
	}

	@RequestMapping(value = "/cancelOrder")
	@ResponseBody
	public AjaxResult cancelOrder(String subId) {
		return orderManagerService.cancelOrder(subId);
	}

	@RequestMapping(value = "/listByProductType")
	@ResponseBody
	public PageBean listByProductType(HttpServletRequest request, String productType, Integer goalPage,
			Integer pageSize) {
		SmsAccountModel accountModel = ServletUtil.getLoginUserInfo(request);
		PageParam pageParam = new PageParam();
		pageParam.setGoalPage(goalPage);
		pageParam.setPageSize(pageSize);
		Map<String, Object> orderParam = new HashMap<>();
		orderParam.put("clientId", accountModel.getClientId());
		orderParam.put("productType", productType);
		return orderManagerService.getOrderListByProductType(orderParam, pageParam);
	}

	@RequestMapping(value = "/listRemain")
	@ResponseBody
	public PageBean listRemain(HttpServletRequest request, String productType, Integer goalPage, Integer pageSize) {
		SmsAccountModel accountModel = ServletUtil.getLoginUserInfo(request);

		PageParam pageParam = new PageParam();
		pageParam.setGoalPage(goalPage);
		pageParam.setPageSize(pageSize);
		Map<String, Object> orderParam = new HashMap<>();
		orderParam.put("clientId", accountModel.getClientId());
		orderParam.put("productType", productType);
		return orderManagerService.listRemain(orderParam, pageParam);
	}

//	@RequestMapping(value = "/exportOrder")
//	public void exportOrder(HttpServletRequest request, GetOrderParam getOrderParam, HttpServletResponse resp) throws Exception {
//		SmsAccountModelPo accountModel = ServletUtil.getLoginUserInfo(request);
//		getOrderParam.setClientId(accountModel.getClientId());
//		try {
//			List<Map<String, Object>> data = orderManagerService.exportOrder(getOrderParam);
//			if (data == null || data.size() < 1) {
//				return;
//			}
//
//			StringBuffer fileName = new StringBuffer();
//			fileName.append("我的订单");
//
//			Excel excel = new Excel();
//			excel.setTitle(fileName.toString());
//			fileName.append(DateUtil.dateToStr(new Date(), "yyyyMMddHHmmss"));
//
//			fileName.append(".xls");
//			String filePath = ConfigUtils.temp_file_dir + "/" + accountModel.getClientId() + "/" + fileName.toString();
//			excel.setFilePath(filePath);
//			excel.addHeader(10, "序号", "rownum");
//			excel.addHeader(20, "订单号", "order_id");
//			excel.addHeader(20, "产品代码", "product_code");
//			excel.addHeader(20, "产品名称", "product_name");
//			excel.addHeader(20, "产品类型", "product_type_name");
//			excel.addHeader(20, "运营商", "operator_codename");
//			excel.addHeader(20, "区域", "area_codename");
//			excel.addHeader(20, "总数量", "quantity_str");
//			excel.addHeader(20, "剩余数量", "remain_quantity_str");
//			excel.addHeader(20, "到期时间", "end_time_str");
//			excel.addHeader(20, "订单状态", "status_name");
//
//			excel.setShowRownum(false);
//
//			excel.setDataList(data);
//
//			if (ExcelUtils.exportExcel(excel)) {
//				FileUtils.download(filePath, resp);
//				FileUtils.delete(filePath);
//			} else {
//				String fullContentType = "text/plain;charset=UTF-8";
//				resp.setContentType(fullContentType);
//				try {
//					resp.getWriter().write("我的订单报表生成失败，请联系管理员");
//					resp.getWriter().flush();
//				} catch (IOException e) {
//					logger.error("我的订单报表生成失败", e);
//				}
//			}
//
//		} catch (Exception e) {
//			logger.error("我的订单报表生成失败", e);
//		}
//	}

	@GetMapping(path = "/exportOrder")
	public void export(GetOrderParam getOrderParam, HttpServletResponse responses) {
		SmsAccountModelPo accountModel = ServletUtil.getLoginUserInfo(request);
		getOrderParam.setClientId(accountModel.getClientId());

		List<Map<String, Object>> data = orderManagerService.exportOrder(getOrderParam);
		if (data == null || data.size() < 1) {
			return;
		}

		String filePath = ConfigUtils.temp_file_dir + "/直客信息列表" + DateUtil.dateToStr(new Date(), "yyyyMMddHHmmss") +".xls";

		Excel excel = new Excel();
		excel.setPageRowCount(2000); // 设置每页excel显示2000行
		excel.setFilePath(filePath);
		excel.setTitle("我的订单");

		excel.setFilePath(filePath);
		excel.addHeader(10, "序号", "rownum");
		excel.addHeader(20, "订单号", "order_id");
		excel.addHeader(20, "产品代码", "product_code");
		excel.addHeader(20, "产品名称", "product_name");
		excel.addHeader(20, "产品类型", "product_type_name");
		excel.addHeader(20, "运营商", "operator_codename");
		excel.addHeader(20, "区域", "area_codename");
		excel.addHeader(20, "总数量", "quantity_str");
		excel.addHeader(20, "剩余数量", "remain_quantity_str");
		excel.addHeader(20, "到期时间", "end_time_str");
		excel.addHeader(20, "订单状态", "status_name");

		excel.setDataList(data);

		if (com.ucpaas.sms.common.util.file.ExcelUtils.exportExcel(excel)) {
			com.ucpaas.sms.common.util.file.FileUtils.download(filePath, responses);
			com.ucpaas.sms.common.util.file.FileUtils.delete(filePath);
		} else {
			String fullContentType = "text/plain;charset=UTF-8";
			responses.setContentType(fullContentType);
			try {
				responses.getWriter().write("导出Excel文件失败，请联系管理员");
				responses.getWriter().flush();
			} catch (IOException e) {
				logger.error("导出Excel文件失败", e);
			}
		}
	}
}
