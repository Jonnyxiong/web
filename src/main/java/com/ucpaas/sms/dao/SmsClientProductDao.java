package com.ucpaas.sms.dao;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ucpaas.sms.model.po.SmsClientProductPo;


@Repository
public class SmsClientProductDao {
	
	@Autowired
	private MasterDao masterDao;
	
	
	public List<SmsClientProductPo> getSmsClientProductList(Map<String,Object> sqlParams){
		return masterDao.selectList("order.SmsClientProductMapper.getSmsClientProductList", sqlParams);
	}
	
}
