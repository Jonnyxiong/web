package com.ucpaas.sms.init;

import com.ucpaas.sms.constant.Constant;
import com.ucpaas.sms.service.OrderManagerService;
import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.util.DateUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class InitDataListener implements InitializingBean {

	@Autowired
	private OrderManagerService orderManagerService;

	@Override
	public void afterPropertiesSet() throws Exception {

		Date date = new Date();
		int num = 0;
		String orderIdPre = DateUtil.dateToStr(date, "yyMMdd") + DateUtil.dateToStr(date, "HHmm")
				+ ConfigUtils.platform_order_identify; // 0 ,
														// platform_order_identify区分各个平台(客户与OEM)
		Map<String, Object> sqlParams = new HashMap<>();
		sqlParams.put("orderIdPre", orderIdPre);
		String numStr = orderManagerService.getTheMostNumForMinute(orderIdPre);
		if (numStr == null) {
			num = 1;
		} else {
			num = Integer.valueOf(numStr) + 1;
		}

		Constant.ORDERID_PRE = orderIdPre;
		Constant.ORDER_NUM = num;
	}

}
