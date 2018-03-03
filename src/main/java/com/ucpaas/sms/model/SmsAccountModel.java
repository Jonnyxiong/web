package com.ucpaas.sms.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

@ApiModel(value = "账户信息传递对象", description = "账户信息传递对象")
public class SmsAccountModel implements Serializable {

	private static final long serialVersionUID = -5201698292604461357L;

	// 主键id,UUID
	@ApiModelProperty(value = "主键id", required = true)
	private String id;

	// 用户帐号（子帐号）
	@ApiModelProperty(value = "用户帐号（子帐号）", required = true)
	private String clientId;

	// 用户密码
	@ApiModelProperty(value = "修改密码时使用", required = false)
	private String password;

	// 用户名称
	@ApiModelProperty(value = "用户名称", required = true)
	private String name;

	// 平台帐号
	@ApiModelProperty(value = "平台帐号", required = false)
	private String sid;
	
	//帐户状态，0：注册未激活，1：注册完成，5：锁定，6：注销
	private Integer status;
	
	//代理商id
	private Integer agentId;
	
	//认证状态，2：待认证 ，3：证件已认证(正常)，4：认证不通过
	private Integer oauthStatus;
	
	//认证时间
	private Date oauthDate;
	
	//手机号码
	private String mobile;
	
	//邮箱地址
	private String email;
	
	//个人地址/公司地址
	private String address;
	
	//个人姓名/公司名称
	private String realname;
	
	//用户等级，1：普通客户（6－8位用户扩展），2：中小企业大型企业（4－5位用户扩展），3：大型企业（2－3位用户扩展）
	private Integer clientLevel;
	
	//用户类型，1：个人用户，2：企业用户
	private Integer clientType;
	
	//是否需要状态报告，0：不需要，1：需要简单状态报告，2：需要透传状态报告
	private Integer needreport;
	
	//是否需要上行，0：不需要，1：需要
	private Integer needmo;
	
	//是否需要审核，0：不需要，1：需要
	private Integer needaudit;

	//创建时间
	private Date createtime;

	//验证IP（可以有多个，用逗号分隔：192.168.0.*，*，192.168.0.0/16
	private String ip;

	//SGIP协议接入客户提供的上行IP
	private String moip;

	//SGIP协议接入客户提供的上行端口
	private String moport;

	//提供给SGIP协议接入客户的节点编码
	private Long nodeid;
	
	//状态报告回调地址
	private String deliveryurl;
	
	//上行回调地址
	private String mourl;
	
	// 模板审核通知回调地址
	private String noticeurl;

	// 连接节点数
	private Integer nodenum;
	
	//付费类型，0：预付费，1：后付费
	private Integer paytype;
	
	//是否支持自扩展，0：不支持，1：支持
	private Integer needextend;
	
	//是否支持签名对应签名端口，0：不支持，1：支持
	private Integer signextend;
	
	//备注
	private String remarks;
	
	//短信类型，0：通知短信，4：验证码短信，5：营销短信（适用于标准协议）
	private Integer smstype;
	
	//短信协议类型，0为rest协议(rest)，1为vmsp协议（vmsp），2为SMPP协议（access），3为CMPP协议（access）,4为SGIP协议（access），5为SMGP协议（access），6为HTTPS协议
	private Integer smsfrom;

	private int httpProtocolType;
	
	//创建时间，默认为系统时间
	private Date createTime;
	
	//更新时间
	private Date updateTime;
	
	//省
	private String province;
	
	//市
	private String city;
	
	//区县
	private String area;
	
	//access流水表id
	private Integer identify;

	private Integer agentType;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getAgentId() {
		return agentId;
	}

	public void setAgentId(Integer agentId) {
		this.agentId = agentId;
	}

	public Integer getOauthStatus() {
		return oauthStatus;
	}

	public void setOauthStatus(Integer oauthStatus) {
		this.oauthStatus = oauthStatus;
	}

	public Date getOauthDate() {
		return oauthDate;
	}

	public void setOauthDate(Date oauthDate) {
		this.oauthDate = oauthDate;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getRealname() {
		return realname;
	}

	public void setRealname(String realname) {
		this.realname = realname;
	}

	public Integer getClientLevel() {
		return clientLevel;
	}

	public void setClientLevel(Integer clientLevel) {
		this.clientLevel = clientLevel;
	}

	public Integer getClientType() {
		return clientType;
	}

	public void setClientType(Integer clientType) {
		this.clientType = clientType;
	}

	public Integer getNeedreport() {
		return needreport;
	}

	public void setNeedreport(Integer needreport) {
		this.needreport = needreport;
	}

	public Integer getNeedmo() {
		return needmo;
	}

	public void setNeedmo(Integer needmo) {
		this.needmo = needmo;
	}

	public Integer getNeedaudit() {
		return needaudit;
	}

	public void setNeedaudit(Integer needaudit) {
		this.needaudit = needaudit;
	}

	public Date getCreatetime() {
		return createtime;
	}

	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getMoip() {
		return moip;
	}

	public void setMoip(String moip) {
		this.moip = moip;
	}

	public String getMoport() {
		return moport;
	}

	public void setMoport(String moport) {
		this.moport = moport;
	}

	public Long getNodeid() {
		return nodeid;
	}

	public void setNodeid(Long nodeid) {
		this.nodeid = nodeid;
	}

	public String getDeliveryurl() {
		return deliveryurl;
	}

	public void setDeliveryurl(String deliveryurl) {
		this.deliveryurl = deliveryurl;
	}

	public String getMourl() {
		return mourl;
	}

	public void setMourl(String mourl) {
		this.mourl = mourl;
	}

	public String getNoticeurl() {
		return noticeurl;
	}

	public void setNoticeurl(String noticeurl) {
		this.noticeurl = noticeurl;
	}

	public Integer getNodenum() {
		return nodenum;
	}

	public void setNodenum(Integer nodenum) {
		this.nodenum = nodenum;
	}

	public Integer getPaytype() {
		return paytype;
	}

	public void setPaytype(Integer paytype) {
		this.paytype = paytype;
	}

	public Integer getNeedextend() {
		return needextend;
	}

	public void setNeedextend(Integer needextend) {
		this.needextend = needextend;
	}

	public Integer getSignextend() {
		return signextend;
	}

	public void setSignextend(Integer signextend) {
		this.signextend = signextend;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public Integer getSmstype() {
		return smstype;
	}

	public void setSmstype(Integer smstype) {
		this.smstype = smstype;
	}

	public Integer getSmsfrom() {
		return smsfrom;
	}

	public void setSmsfrom(Integer smsfrom) {
		this.smsfrom = smsfrom;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public Integer getIdentify() {
		return identify;
	}

	public void setIdentify(Integer identify) {
		this.identify = identify;
	}

	public Integer getAgentType() {
		return agentType;
	}

	public void setAgentType(Integer agentType) {
		this.agentType = agentType;
	}

	public int getHttpProtocolType() {
		return httpProtocolType;
	}

	public void setHttpProtocolType(int httpProtocolType) {
		this.httpProtocolType = httpProtocolType;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("SmsAccountModel{");
		sb.append("id='").append(id).append('\'');
		sb.append(", clientId='").append(clientId).append('\'');
		sb.append(", password='").append(password).append('\'');
		sb.append(", name='").append(name).append('\'');
		sb.append(", sid='").append(sid).append('\'');
		sb.append(", status=").append(status);
		sb.append(", agentId=").append(agentId);
		sb.append(", oauthStatus=").append(oauthStatus);
		sb.append(", oauthDate=").append(oauthDate);
		sb.append(", mobile='").append(mobile).append('\'');
		sb.append(", email='").append(email).append('\'');
		sb.append(", address='").append(address).append('\'');
		sb.append(", realname='").append(realname).append('\'');
		sb.append(", clientLevel=").append(clientLevel);
		sb.append(", clientType=").append(clientType);
		sb.append(", needreport=").append(needreport);
		sb.append(", needmo=").append(needmo);
		sb.append(", needaudit=").append(needaudit);
		sb.append(", createtime=").append(createtime);
		sb.append(", ip='").append(ip).append('\'');
		sb.append(", moip='").append(moip).append('\'');
		sb.append(", moport='").append(moport).append('\'');
		sb.append(", nodeid=").append(nodeid);
		sb.append(", deliveryurl='").append(deliveryurl).append('\'');
		sb.append(", mourl='").append(mourl).append('\'');
		sb.append(", noticeurl='").append(noticeurl).append('\'');
		sb.append(", nodenum=").append(nodenum);
		sb.append(", paytype=").append(paytype);
		sb.append(", needextend=").append(needextend);
		sb.append(", signextend=").append(signextend);
		sb.append(", remarks='").append(remarks).append('\'');
		sb.append(", smstype=").append(smstype);
		sb.append(", smsfrom=").append(smsfrom);
		sb.append(", httpProtocolType=").append(httpProtocolType);
		sb.append(", createTime=").append(createTime);
		sb.append(", updateTime=").append(updateTime);
		sb.append(", province='").append(province).append('\'');
		sb.append(", city='").append(city).append('\'');
		sb.append(", area='").append(area).append('\'');
		sb.append(", identify=").append(identify);
		sb.append(", agentType=").append(agentType);
		sb.append('}');
		return sb.toString();
	}
}
