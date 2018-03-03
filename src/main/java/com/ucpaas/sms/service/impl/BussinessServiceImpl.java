package com.ucpaas.sms.service.impl;

import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.enums.WebId;
import com.jsmsframework.sms.send.service.JsmsTimerSendTaskService;
import com.ucpaas.sms.constant.Constant.DbTablePrefix;
import com.ucpaas.sms.dao.BussinessDao;
import com.ucpaas.sms.dao.AccessDao;
import com.ucpaas.sms.exception.OperationException;
import com.ucpaas.sms.model.PageBean;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.model.param.PageParam;
import com.ucpaas.sms.service.BussinessService;
import com.ucpaas.sms.service.CommonService;
import com.ucpaas.sms.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional
public class BussinessServiceImpl implements BussinessService {

	@Autowired
	private CommonService commonService;

	@Autowired
	private BussinessDao bussinessDao;
	@Autowired
	private JsmsTimerSendTaskService jsmsTimerSendTaskService;
	@Autowired
	private AccessDao accessDao;

	private boolean isOem(Map<String, Object> params) {
		Object obj = params.get("isOem");
		if (obj == null) {
			throw new OperationException("是否为OEM为空");
		}

		return (Boolean) obj;
	}
	private boolean isZk(Map<String, Object> params) {
		Object obj = params.get("isZk");
		if (obj == null) {
			throw new OperationException("是否为直客为空");
		}

		return (Boolean) obj;
	}
	private boolean isBr(Map<String, Object> params) {
		Object obj = params.get("isBr");
		if (obj == null) {
			throw new OperationException("是否为品牌为空");
		}

		return (Boolean) obj;
	}

	@Override
	public PageContainer queryDayStat(Map<String, Object> params) throws Exception {
		return isOem(params) ? bussinessDao.queryDayStatOem(params) : bussinessDao.queryDayStatBr(params);
	}

	@Override
	public PageContainer queryMonthStat(Map<String, Object> params) throws Exception {
		return isOem(params) ? bussinessDao.queryMonthStatOem(params) : bussinessDao.queryMonthStatBr(params);
	}

	@Override
	public PageContainer querySmsSendRecord(Map<String, Object> params) throws Exception {
		String start_time = (String) params.get("start_time");
		String end_time = (String) params.get("end_time");
		List<String> tableList = new ArrayList<>();

		List<String> tableListTemp_old = commonService.getExistTable(DbTablePrefix.T_SMS_ACCESS_YYYYMMDD, start_time,
				end_time);
		List<String> tableListTemp_new = commonService.getExistTable(DbTablePrefix.T_SMS_ACCESS_YYYYMMDD, start_time,
				end_time, params.get("clientid").toString());
		tableList.addAll(tableListTemp_old);
		tableList.addAll(tableListTemp_new);

		// ==============================分表需求-结束==================================
		if (tableList.size() > 0) {
			Map<String, Object> p = new HashMap<String, Object>();
			p.putAll(params);
			p.put("table_list", tableList);
			return isOem(params) ? bussinessDao.querySmsRecordPageOem(p)
					: bussinessDao.querySmsRecordPageBr(p);
		}

		return new PageContainer();
	}

	@Override
	public List<Map<String, Object>> querySmsSendRecordCount(Map<String, Object> params) throws Exception {
		String start_time = params.get("start_time").toString();
		String end_time = params.get("end_time").toString();
		List<String> tableList = new ArrayList<>();

		List<String> tableListTemp_old = commonService.getExistTable(DbTablePrefix.T_SMS_ACCESS_YYYYMMDD, start_time,
				end_time);
		List<String> tableListTemp_new = commonService.getExistTable(DbTablePrefix.T_SMS_ACCESS_YYYYMMDD, start_time,
				end_time, params.get("clientid").toString());
		tableList.addAll(tableListTemp_old);
		tableList.addAll(tableListTemp_new);

		// ==============================分表需求-结束==================================

		if (tableList.size() > 0) {
			Map<String, Object> p = new HashMap<String, Object>();
			p.putAll(params);
			p.put("table_list", tableList);
			return bussinessDao.querySmsSendRecordCount(p);
		}
		return new ArrayList<Map<String, Object>>();
	}

	@Override
	public PageBean queryInterShortMessage(PageParam pageParam, String countryInfo) {
		Map<String, Object> sqlParams = new HashMap<>();
		sqlParams.put("countryInfo", countryInfo);
		sqlParams.put("startIndex", pageParam.getStartIndex());
		sqlParams.put("pageSize", pageParam.getPageSize());
		List<Map<String, Object>> tariffList = bussinessDao.querySmsClientTariffMapList(sqlParams);
		int totalRows = bussinessDao.querySmsClientTariffListCount(sqlParams);

		PageBean pageBean = new PageBean(pageParam.getPageSize(), pageParam.getGoalPage(), totalRows);
		pageBean.setList(tariffList);
		return pageBean;
	}

	@Override
	public List<Map<String, Object>> querySmsRecord4Excel(Map<String, Object> params) throws Exception {
		String start_time = params.get("start_time").toString();
		String end_time = params.get("end_time").toString();
		List<String> tableList = new ArrayList<>();

		List<String> tableListTemp_old = commonService.getExistTable(DbTablePrefix.T_SMS_ACCESS_YYYYMMDD, start_time,
				end_time);
		List<String> tableListTemp_new = commonService.getExistTable(DbTablePrefix.T_SMS_ACCESS_YYYYMMDD, start_time,
				end_time, params.get("clientid").toString());
		tableList.addAll(tableListTemp_old);
		tableList.addAll(tableListTemp_new);

		// ==============================分表需求-结束==================================
		if (tableList.size() > 0) {
			Map<String, Object> p = new HashMap<String, Object>();
			p.putAll(params);
			p.put("table_list", tableList);
			return isOem(params) ? bussinessDao.querySmsRecord4ExcelOem(p) : bussinessDao.querySmsRecord4ExcelBr(p);
		}
		return new ArrayList<>();
	}

	@Override
	public PageContainer queryStatistic(Map<String, Object> params) throws Exception {
		int startDate = (int) (params.get("startDate") == null ? 0 : params.get("startDate"));
		int endDate = (int) (params.get("endDate") == null ? 0 : params.get("endDate"));
		if (startDate > endDate) {
			return new PageContainer();
		}
		formatStatisticFormData(params, startDate, endDate, true);
		if(isOem(params)){
		return 	bussinessDao.queryStatisticOem(params);
		}else if(isBr(params)){
			return bussinessDao.queryStatisticBr(params);
		}else if(isZk(params)){
			return bussinessDao.queryStatisticZk(params);
		}else{
			return new PageContainer();
		}
	}

	@Override
	public Map queryStatisticTotal(Map<String, Object> formData) throws Exception {
		int startDate = (int) (formData.get("startDate") == null ? 0 : formData.get("startDate"));
		int endDate = (int) (formData.get("endDate") == null ? 0 : formData.get("endDate"));
		Map subtotal =new  HashMap();
		if (startDate > endDate) {
			return zeroMap();
		}
		formatStatisticFormData(formData, startDate, endDate, false);
		if(isOem(formData)){
			subtotal=bussinessDao.queryStatisticTotalOem(formData);
		}else if(isBr(formData)){
			subtotal= bussinessDao.queryStatisticTotalBr(formData);
		}else if(isZk(formData)){
			subtotal= bussinessDao.queryStatisticTotalZk(formData);
		}else{
			return subtotal;
		}
		if (subtotal == null || subtotal.size() == 0) {
			return zeroMap();
		}
		return subtotal;
	}

	private void formatStatisticFormData(Map<String, Object> formData, int startDate, int endDate, boolean addOneday)
			throws Exception {
		int before90Int = DateUtil.getDateFromTodayInInt(-90);
		if (startDate < before90Int) {
			throw new Exception("开始时间不得超过90天之前");
		}

		if (startDate == 0) {
			throw new Exception("请输入开始时间");
		}
		if (endDate == 0) {
			endDate = Integer.valueOf(DateUtil.dateToStr(new Date(), "yyyyMMdd"));
		}

		if (addOneday) {
			Calendar endCal = Calendar.getInstance();
			endCal.set(endDate / 10000, endDate % 10000 / 100 - 1, endDate % 10000 % 100);
			endCal.add(Calendar.DATE, 1);
			endDate = Integer.valueOf(DateUtil.dateToStr(endCal.getTime(), "yyyyMMdd"));
			formData.put("startDate", startDate);
			formData.put("endDate", endDate);
		}
	}

	private Map zeroMap() {
		Map subtotal = new HashMap<>();
		BigDecimal num_all_total = BigDecimal.ZERO;
		BigDecimal num_sucs_total = BigDecimal.ZERO;
		BigDecimal num_fail_total = BigDecimal.ZERO;
		BigDecimal num_known_total = BigDecimal.ZERO;
		BigDecimal num_pending_total = BigDecimal.ZERO;
		BigDecimal chargeTotal_total = BigDecimal.ZERO;
		BigDecimal intercept_total = BigDecimal.ZERO;

		subtotal.put("num_all_total", num_all_total);
		subtotal.put("chargeTotal_total", chargeTotal_total);
		subtotal.put("num_sucs_total", num_sucs_total);
		subtotal.put("num_known_total", num_known_total);
		subtotal.put("num_fail_total", num_fail_total);
		subtotal.put("num_pending_total", num_pending_total);
		subtotal.put("num_intercept_total", intercept_total);

		return subtotal;
	}

	@Override
	public List<Map<String, Object>> exportStatistics(Map<String, Object> formData) throws Exception {
		int startDate = (int) (formData.get("startDate") == null ? 0 : formData.get("startDate"));
		int endDate = (int) (formData.get("endDate") == null ? 0 : formData.get("endDate"));
		if (startDate > endDate) {
			return new ArrayList<>();
		}

		formatStatisticFormData(formData, startDate, endDate, true);

		return isOem(formData) ? accessDao.getSearchList("bussiness.queryStatisticOem", formData) : accessDao.getSearchList("bussiness.queryStatisticBr", formData);
	}

	@Override
	public List<Map<String, Object>> exportBussinessData(Map<String, Object> queryParams) {
		return accessDao.selectList("bussiness.queryDayStatBr", queryParams);
	}

	@Override
	public List<Map<String, Object>> exportMonthStat(Map params) {
		return accessDao.selectList("bussiness.queryMonthStatBr", params);
	}

	@Override
	public int exportBussinessDataCount(Map<String, Object> queryParams) {
		return accessDao.selectOne("bussiness.queryDayStatCountInt", queryParams);
	}

	@Override
	public JsmsPage smsTimerSendQuery(JsmsPage jsmsPage, WebId webId, String agengId) {
		return jsmsTimerSendTaskService.queryPageList(jsmsPage,webId,agengId);
	}
}
