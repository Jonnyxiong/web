package com.ucpaas.sms.service;

import com.ucpaas.sms.common.AjaxResult;
import com.ucpaas.sms.model.PageBean;
import com.ucpaas.sms.model.SmsAccountModel;
import com.ucpaas.sms.model.param.GetOrderParam;
import com.ucpaas.sms.model.param.PageParam;
import com.ucpaas.sms.model.param.PlaceOrderParam;
import com.ucpaas.sms.model.po.SmsClientOrderPo;

import java.util.List;
import java.util.Map;

public interface OrderManagerService {
	
	PageBean getProductListByClientId(String clientId,PageParam pageParam);
	
	AjaxResult createOrder(PlaceOrderParam placeOrderParam);
	
	PageBean getOrderListInfo(GetOrderParam getOrderParam,PageParam pageParam);
	
	SmsClientOrderPo getOrderDetailBySubId(String subId);
	
	void updateOrderStatus(String status,String subId);

	AjaxResult cancelOrder(String subId);
	
	String getTheMostNumForMinute(String orderIdPre);

	List<Map> getOrderRemainQuantity(SmsAccountModel account);

	Map<String,Integer> groupOrderListByProductType(Map<String, Object> orderParam);

	PageBean getOrderListByProductType(Map<String, Object> orderParam, PageParam pageParam);

	PageBean listRemain(Map<String, Object> orderParam, PageParam pageParam);

	List<Integer> getProductTypes(Map<String, Object> orderParam);

	PageBean getProductListByClientId(String clientId, PageParam pageParam, List<Integer> productTypes);

	List<Map<String,Object>> exportOrder(GetOrderParam getOrderParam);

	Long getOrderId();

}
