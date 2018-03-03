package com.ucpaas.sms.controller.finance;

import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.dto.Result;
import com.jsmsframework.common.enums.CodeEnum;
import com.jsmsframework.common.util.StringUtils;
import com.jsmsframework.order.dto.ClientConsumerListTotal;
import com.jsmsframework.order.dto.OemClientOrderListTotal;
import com.jsmsframework.order.service.JsmsClientConsumerService;
import com.jsmsframework.order.service.JsmsOemClientOrderService;
import com.ucpaas.sms.constant.Constant;
import com.ucpaas.sms.controller.BaseController;
import com.ucpaas.sms.model.po.SmsAccountModelPo;
import com.ucpaas.sms.util.web.ServletUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/finance/recharge")
public class RechangeController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(RechangeController.class);

	@Autowired
	private JsmsOemClientOrderService jsmsOemClientOrderService;
    @Autowired
	private JsmsClientConsumerService  jsmsClientConsumerService;

	/**
	 * 充值消费
	 */
	@RequestMapping(value = "/myConsumption", method = RequestMethod.GET)
	public ModelAndView myConsumption(HttpServletRequest request, ModelAndView mv, HttpSession session)
			throws Exception {
		SmsAccountModelPo accountModel = (SmsAccountModelPo) session.getAttribute(Constant.LOGIN_USER_INFO);
		mv.addObject("accountModel", accountModel);
		if(String.valueOf(session.getAttribute("isOEM")).equals("true")){
			mv.addObject("property","isOEM");
		}else{
			mv.addObject("property","isBR");
		}
		everyPageShouldHave(mv, request.getServerName(), "/finance/recharge/myConsumption", request);
		mv.setViewName("finance/recharge/myConsumption");
		return mv;
	}

	/**
	 * 充值消费接口
	 * 
	 * @param page
	 * @param orderType
	 *            订单类型，1：充值，2：回退,0:条数返还
	 * @param productType
	 *            产品类型 0：行业，1：营销，2：国际
	 * @param beginCreateTime
	 *            订单创建开始时间
	 * @param endCreateTime
	 *            订单创建结束时间
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/oemClientOrder/list", method = RequestMethod.POST)
	@ResponseBody
	public JsmsPage oemClientOrderList(JsmsPage page, Integer pageNo, Integer pageSize, String orderType,
			String productType, String beginCreateTime, String endCreateTime, HttpSession session) throws Exception {
		SmsAccountModelPo accountModel = (SmsAccountModelPo) session.getAttribute(Constant.LOGIN_USER_INFO);
		page.setPage(pageNo);
		page.setRows(pageSize);
		if(String.valueOf(session.getAttribute("isOEM")).equals("false")){
			orderType = "0";
		}
		page.getParams().put("orderType", StringUtils.isBlank(orderType) ? null : orderType);
		page.getParams().put("productType", StringUtils.isBlank(productType) ? null : productType);
		page.getParams().put("beginCreateTime", StringUtils.isBlank(beginCreateTime) ? null : beginCreateTime);
		page.getParams().put("endCreateTime", StringUtils.isBlank(endCreateTime) ? null : endCreateTime);
		page.getParams().put("clientId", accountModel.getClientId());
		if(String.valueOf(session.getAttribute("isOEM")).equals("false")){

			page = jsmsClientConsumerService.queryClientOrderDTO(page);
		}else{
			page = jsmsOemClientOrderService.queryOemClientOrderDTO(page);
		}
		return page;
	}

	/**
	 * 充值消费合计
	 * 
	 * @param orderType
	 *            订单类型，1：充值，2：回退,0:条数返还
	 * @param productType
	 *            产品类型 0：行业，1：营销，2：国际
	 * @param session
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/oemClientOrder/list/total")
	@ResponseBody
	public Result<OemClientOrderListTotal> oemClientOrderListTotal(String orderType, String productType,
			String beginCreateTime, String endCreateTime, HttpSession session) throws Exception {
		SmsAccountModelPo accountModel = (SmsAccountModelPo) session.getAttribute(Constant.LOGIN_USER_INFO);
		OemClientOrderListTotal oemOrderListTotal = new OemClientOrderListTotal();
		ClientConsumerListTotal clientConsumerListTotal = new ClientConsumerListTotal();
		Map params = new HashMap();
		if(String.valueOf(session.getAttribute("isOEM")).equals("false")){
			orderType = "0";
		}
		params.put("orderType", StringUtils.isBlank(orderType) ? null : orderType);
		params.put("productType", StringUtils.isBlank(productType) ? null : productType);
		params.put("beginCreateTime", StringUtils.isBlank(beginCreateTime) ? null : beginCreateTime);
		params.put("endCreateTime", StringUtils.isBlank(endCreateTime) ? null : endCreateTime);
		params.put("clientId", accountModel.getClientId());
		if(String.valueOf(session.getAttribute("isOEM")).equals("false")){
			clientConsumerListTotal = jsmsClientConsumerService.queryClientOrderListTotal(params);
			return new Result(true, CodeEnum.SUCCESS, clientConsumerListTotal, "操作成功");
		}else{
			oemOrderListTotal = jsmsOemClientOrderService.queryOemClientOrderListTotal(params);
			return new Result(true, CodeEnum.SUCCESS, oemOrderListTotal, "操作成功");
		}
	}
}
