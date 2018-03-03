package com.ucpaas.sms.dao;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * ucpaas_message_statistics短信流水库
 */
@Repository
public class AccessDao extends BaseDao {

	@Override
	@Resource(name = "access_sqlSessionTemplate")
	protected void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSessionTemplate = sqlSessionTemplate;
	}

}
