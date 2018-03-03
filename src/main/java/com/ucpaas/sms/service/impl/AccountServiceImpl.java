/**    
 * @Title: AccountServiceImpl.java  
 * @Package: com.ucpaas.sms.service.account.impl
 * @Description: TODO
 * @author: Niu.T    
 * @date: 2016年9月23日 下午2:47:12  
 * @version: V1.0    
 */
package com.ucpaas.sms.service.impl;

import com.ucpaas.sms.common.util.SecurityUtils;
import com.ucpaas.sms.dao.AccountDao;
import com.ucpaas.sms.model.SmsAccountModel;
import com.ucpaas.sms.model.SmsOauthPic;
import com.ucpaas.sms.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**  
 * @ClassName: AccountServiceImpl  
 * @Description: 账户管理Service:(1)账户资料(2)账户安全
 * @author: Niu.T 
 * @date: 2016年9月23日 下午2:47:12  
 */
@Service
@Transactional
public class AccountServiceImpl implements AccountService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private AccountDao accountDao;
	
	/**
	 * 获取用户账户的基本信息(根据clientId)
	 */
	@Override
	public SmsAccountModel getAccountInfo(String clientId) {
		
		return accountDao.getAccountInfo(clientId);
	}


	/**
	 * 修改账户的基本信息,返回结果信息(map)
	 */
	@Override
	public Map<String, Object> updateAccInfo(SmsAccountModel smsAccountModel) {
		Map<String,Object> data = new HashMap<String,Object>();
        if(!StringUtils.isEmpty(smsAccountModel.getEmail())&& !StringUtils.isEmpty(smsAccountModel.getMobile())){
			List<Map> mapList = accountDao.checkDuplicate(smsAccountModel);
			if(mapList.size() > 0){
				String msg = "";
				int emailFlag = 0;
				int mobileFlag = 0;
				for(Map map: mapList){
					if(emailFlag == 0 && map.get("email").equals(smsAccountModel.getEmail())){
						emailFlag = 1;
						msg += "邮箱已经存在<br>";
					}
					if(mobileFlag == 0 && map.get("mobile").equals(smsAccountModel.getMobile())){
						mobileFlag = 1;
						msg += "手机号码已经存在<br>";
					}
					if(mobileFlag == 1 && emailFlag == 1){
						break;
					}
				}
				data.put("result", "fail");
				data.put("msg", msg);
				return data;
			}
		}
        smsAccountModel.setUpdateTime(new Date());//添加更新时间
		int result = accountDao.updateAccInfo(smsAccountModel);
		data.put("result", result > 0 ? "success" : "fail");
		data.put("msg", result > 0 ? "修改成功" : "操作失败");
		logger.debug("修改账户信息 ---->{} /n 参数 ---->{}",data);
		return data;
	}

	/**
	 * 获取用户上传的资质信息(根据clientId)
	 */
	@Override
	public SmsOauthPic getCerInfo(String clientId) {
		//1. 获取客户资质信息
		SmsOauthPic cerInfo = accountDao.getCerInfo(clientId);
		//2. 加密图片路径
		String imgUrl = cerInfo.getImgUrl();
		if(imgUrl != null && !"".equals(imgUrl)){
			cerInfo.setImgUrl(SecurityUtils.encodeDes3(imgUrl));
		}
		//3. 判断用户资质认证状态 (若认证未通过则需要查询未通过的原因)
		if(cerInfo.getOauthStatus() == 4){//认证状态，2：待认证 ，3：证件已认证(正常)，4：认证不通过
			String notPassRemark = accountDao.getNotPassRemark(clientId);
			cerInfo.setReason(notPassRemark);
		}
		logger.debug("获取用户资质信息 --------->{}",cerInfo);
		return cerInfo;
	}

	/**
	 * 添加用户资质信息,返回结果信息(map)
	 */
	@Override
	public Map<String, Object> addCerInfo(SmsOauthPic smsOauthPic) throws Exception{
		Map<String,Object> data = new HashMap<String,Object>();
//		if(smsOauthPic.getImgUrl() != null){
//			String uploadPath = ConfigUtils.client_oauth_pic;// 客户资质图片存放文件夹
//			if(!uploadPath.endsWith("/")) uploadPath += "/";
//			String imgUrl = smsOauthPic.getImgUrl();
//			String datePath = imgUrl.substring(imgUrl.lastIndexOf("/") - 10, imgUrl.lastIndexOf("/") + 1);
//			String datePath = new SimpleDateFormat("yyyy/MM/dd/").format(new Date()); 
//			smsOauthPic.setImgUrl(uploadPath+datePath+imgUrl.substring(imgUrl.lastIndexOf("$$") + 2));
//			data.put("datePath", datePath);
//		}
		if(StringUtils.isEmpty(smsOauthPic.getImgUrl())){
			data.put("result", "fail");
			data.put("msg", "请上传图片");
			return data;
		}
		smsOauthPic.setUpdateDate(new Date());// 添加资质更新时间
		smsOauthPic.setOauthType(2);//认证类型： 1、代理商资质认证 2、客户资质认证
		int addCer = accountDao.addCerInfo(smsOauthPic); // 添加用户信息
		int updateAcc = accountDao.updateAccWithCer(smsOauthPic);// 更新客户状态
		if(addCer > 0 && updateAcc > 0){
			data.put("result", "success");
			data.put("msg", "添加成功");
			logger.debug("添加客户资质: 成功");
		}else if(addCer == 0 && updateAcc == 0){
			data.put("result", "fail");
			data.put("msg", "添加失败");
			logger.debug("添加客户资质: 失败");
		}else{
//			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();//事务回滚待测试 
			throw new RuntimeException("添加客户资质信息:同步更新数据异常");
		}
		return data;
	}

	/**
	 * 修改用户资质信息,返回结果信息(map)
	 */
	@Override
	public Map<String, Object> updateCerInfo(SmsOauthPic smsOauthPic) throws Exception{
		Map<String,Object> data = new HashMap<String,Object>();


		if(StringUtils.isEmpty(smsOauthPic.getImgUrl())){
			data.put("result", "fail");
			data.put("msg", "请上传图片");
			return data;
		}
		smsOauthPic.setUpdateDate(new Date());// 添加资质更新时间
//		smsAccountModel.setUpdateTime(curDate);// 更新客户信息更新时间
//		smsAccountModel.setOauthStatus(2);//认证状态，2：待认证 ，3：证件已认证(正常)，4：认证不通过
		
		int updateCer = accountDao.updateCerInfo(smsOauthPic); // 添加用户信息
		int updateAcc = accountDao.updateAccWithCer(smsOauthPic);// 更新客户状态
		if(updateCer > 0 && updateAcc > 0){
			data.put("result", "success");
			data.put("msg", "更新成功");
			logger.debug("更新客户资质: 成功! 修改账户信息 -->{}条, 修改图片信息 -->{}条",updateAcc ,updateCer);
		}else if(updateCer == 0 && updateAcc == 0){
			data.put("result", "fail");
			data.put("msg", "更新失败");
			logger.debug("更新客户资质: 失败");
		}else{
			throw new RuntimeException("更改客户资质信息(重新提交):同步更新数据异常");
		}
		return data;
	}

	/**
	 * 验证客户是否密码(根据Id)
	 */
	@Override
	public boolean checkPasswordById(SmsAccountModel accountModel) {
		return accountModel.getPassword().equals(accountDao.checkPassword(accountModel));
	}
		
	/**
	 * 修改用户账户密码,返回结果信息(map)
	 */
	@Override
	public Map<String, Object> updatePassword(SmsAccountModel smsAccountModel) {
		Map<String,Object> data = new HashMap<String,Object>();
		int updateNum = accountDao.updatePassword(smsAccountModel);
		data.put("result", updateNum > 0 ? "success" :"fail");
		data.put("msg", updateNum > 0 ? "修改成功,请重新登入" :"修改失败");
		logger.debug("修改账户密码 --------> {}",data);
		return data;
	}


	

}
