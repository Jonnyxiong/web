package com.ucpaas.sms.constant;

/**
 * 常量类
 */
public class Constant {

	public static final String LOGIN_USER_ID = "LOGIN_USER_ID";
	public static final String LOGIN_USER_NAME = "LOGIN_USER_NAME";
	public static final String LOGIN_USER_INFO = "LOGIN_USER_INFO";
	public static final String LOGIN_USER_TOKEN = "LOGIN_USER_TOKEN";
	public static final String WEB_ID = "WEB_ID";
	public static final String IS_OEM = "IS_OEM";
	public static final int SMS_SEND_MAX_NUM = 100; // 短信发送最大手机号码数

	// public static final String sms_experience_template =
	// "【云之讯】亲爱的用户，您的短信验证码为[%s]";
	public static final String sms_experience_template = "【云之讯】亲爱的用户，您的短信验证码为[%s]";

	public static final Integer CLIENT_PRE_PAY = 0; // 预付费
	public static final Integer CLIENT_POST_PAY = 1; // 后付费

	// 订单前缀
	public static String ORDERID_PRE = "";

	// 同一分钟内的第几笔订单
	public static int ORDER_NUM = 1;

	/** 通知重试次数 MAX_RETRY_TIMES=3 */
	public static int MAX_RETRY_TIMES = 3;

	/**
	 * 订单产品包类型
	 */
	public enum OrderProductType {
		INDUSTRY("0", "行业"), MARKETING("1", "营销"), INTERNATIONAL("2", "国际"), VERIFICATION_CODE("3", "验证码"), NOTIFY("4",
				"通知"), USSD("7", "USSD"), FLASH("8", "闪信"), GUAJI("9", "挂机");

		private String value;
		private String desc;

		private OrderProductType(String value, String desc) {
			this.value = value;
			this.desc = desc;
		}

		public String getValue() {
			return value;
		}

		public String getDesc() {
			return desc;
		}

		@Override
		public String toString() {
			return this.value;
		}

	}

	/**
	 * 模版类型
	 */
	public enum TemplateType {
		/**
		 * 0, "通知模版"
		 */
		NOTIFY(0, "通知模版"),
		/**
		 * 1, "验证码模版"
		 */
		VERIFICATION_CODE(4, "验证码模版"),
		/**
		 * 5, "营销模版"
		 */
		MARKETING(5, "营销模版"),
		/**
		 * 7, "USSD模版"
		 */
		USSD(7, "USSD模版"),
		/**
		 * 8, "闪信模版"
		 */
		FLASH(8, "闪信模版"),
		/**
		 * 9, "挂机模版"
		 */
		GUAJI(9, "挂机模版");

		private Integer value;
		private String desc;

		private TemplateType(Integer value, String desc) {
			this.value = value;
			this.desc = desc;
		}

		public Integer getValue() {
			return value;
		}

		public String getDesc() {
			return desc;
		}

		@Override
		public String toString() {
			return this.value.toString();
		}
	}

	/**
	 * 数据库流水表前缀
	 */
	public enum DbTablePrefix {

		T_SMS_ACCESS_YYYYMMDD("t_sms_access_", "短信access流水表");

		private String tablePrefix;
		private String desc;

		private DbTablePrefix(String tablePrefix, String desc) {
			this.tablePrefix = tablePrefix;
			this.desc = desc;
		}

		public String getTablePrefix() {
			return tablePrefix;
		}

		public String getDesc() {
			return desc;
		}

	}

	public enum PayType {
		预付(0, "预付"), 后付(1, "后付");

		private Integer value;
		private String desc;

		PayType(Integer value, String desc) {
			this.value = value;
			this.desc = desc;
		}

		public Integer getValue() {
			return value;
		}

		public String getDesc() {
			return desc;
		}

		public static String getDescByValue(Integer value) {
			if (value == null) {
				return null;
			}
			String result = null;
			for (PayType s : PayType.values()) {
				if (value == s.getValue()) {
					result = s.getDesc();
					break;
				}
			}
			return result;
		}
	}

	public enum AgentType {
		销售代理商(1, "销售代理商"), 品牌代理商(2, "品牌代理商"), 资源合作商(3, "资源合作商"), 代理商和资源合作(4, "代理商和资源合作"), OEM代理商(5, "OEM代理商");

		private Integer value;
		private String desc;

		AgentType(Integer value, String desc) {
			this.value = value;
			this.desc = desc;
		}

		public Integer getValue() {
			return value;
		}

		public String getDesc() {
			return desc;
		}

		public static String getDescByValue(Integer value) {
			if (value == null) {
				return null;
			}
			String result = null;
			for (AgentType s : AgentType.values()) {
				if (value == s.getValue()) {
					result = s.getDesc();
					break;
				}
			}
			return result;
		}
	}
}
