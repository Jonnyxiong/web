package com.ucpaas.sms.util;

import com.jsmsframework.sms.send.service.JsmsAsyncSend;
import com.ucpaas.sms.service.CommonService;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 系统配置工具类
 */
@Service
public class ConfigUtils {
	private static final Logger logger = LoggerFactory.getLogger(ConfigUtils.class);

	@Autowired
	private CommonService commonService;
	@Autowired
	private JsmsAsyncSend jsmsAsyncSend;

	// 运行环境：development（开发）、devtest（开发测试）、test（测试）、production（线上）
	// public static String spring_profiles_active;

	// 系统版本，跟pom配置走
	public static String system_version;

	// 系统标识，可以查看数据库配置
	public static String web_id;

	// swagger api是否开启 #true为开启，其他为关闭
	public static String swagger_switch;

	// 配置文件路径
	public static String config_file_path;
	/**
	 * smsp-access短信请求URL , contenttype = json
	 */
	public static String smsp_access_url_json;
	/**
	 * smsp-access短信请求URL , contenttype = form
	 */
	public static String smsp_access_url_form;

	/**
	 * smsp-access_tim 定时短信请求URL
	 */
	public static String smsp_access_tim_url;

	public static String smsp_access_template_url;

	// 短信体验账号密码和次数
	public static String sms_experience_clientid;
	public static String sms_experience_paasword;
	public static Integer sms_experience_mobilecount;
	public static Integer sms_experience_ipcount;

	// 客户认证资料上传地址
	public static String client_oauth_pic;

	// 临时文件保存目录
	public static String temp_file_dir;

	// 图片服务器地址
	public static String smsp_img_url;
	/**
	 * 文件服务器下载接口
	 */
	public static String file_download_url;

	// Excel最大导入数据数量
	public static String excel_max_import_num;
	// //上线时间
	// public static String online_time;

	// OEM平台标志
	public static String platform_order_identify;

	// 默认OEM客户端域名
	public static String oem_domain_name;
	// 默认OEM代理商域名
	public static String oem_agent_domain_name;

	public static String environmentFlag;
	//用户中心
	//导航栏背景色
	public static String nav_backcolor;
	//导航栏文字颜色
	public static String nav_text_color;
	// 默认版权文字
	public static String default_copyright;
	// 默认归属销售
	public static String default_belongSale;

	public static  String current_tomcat_data_dir;

	/**
	 * 初始化
	 */
	@PostConstruct
	public void init() {
		String path = ConfigUtils.class.getClassLoader().getResource("").getPath();
		config_file_path = path + "system.properties";

		initValue();
		this.initAgentIdPre();
		try {
			MenuUtil.initMenu();
		} catch (DocumentException e) {
			logger.error("初始化菜单失败", e);
		}
		this.jsmsAsyncSend.initSendThread(smsp_access_url_json,smsp_access_url_form,current_tomcat_data_dir,null);

		logger.info("\n-------------------------【sms-web-oem-{}服务器成功启动】\n加载配置文件：\n{}\n加载完毕！", system_version,
				config_file_path);

	}

	/**
	 * 初始化配置项的值
	 */
	private void initValue() {
		Field[] fields = ConfigUtils.class.getFields();
		Object fieldValue = null;
		String name = null, value = null, tmp = null;
		Class<?> type = null;
		Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}");
		Matcher matcher = null;
		try {
			Properties properties = new Properties();
			properties.load(new FileInputStream(config_file_path));

			for (Field field : fields) {
				name = field.getName();
				value = properties.getProperty(name);
				if (StringUtils.isNotBlank(value)) {
					matcher = pattern.matcher(value);
					while (matcher.find()) {
						tmp = properties.getProperty(matcher.group(1));
						if (StringUtils.isBlank(tmp)) {
							logger.error("配置{}存在其它配置{}，请检查您的配置文件", name, matcher.group(1));
						}
						value = value.replace(matcher.group(0), tmp);
					}

					type = field.getType();
					if (String.class.equals(type)) {
						fieldValue = value;
					} else if (Integer.class.equals(type)) {
						fieldValue = Integer.valueOf(value);
					} else if (Boolean.class.equals(type)) {
						fieldValue = Boolean.valueOf(value);
					} else {
						fieldValue = value;
					}
					field.set(this, fieldValue);
				}
				logger.info("加载配置：{}={}", name, field.get(this));
			}
		} catch (Throwable e) {
			logger.error("初始化配置项的值失败：" + name + "=" + value, e);
		}
	}

	/**
	 * 初始化代理商id前缀
	 */
	private void initAgentIdPre(){

		Date date = new Date();
		int num = 0;
		String agentIdPre = DateUtil.dateToStr(date, "yyyyMM");

		String numStr = commonService.getMostAgentNumForMonth(agentIdPre);
		if(numStr == null){
			num = 5000;
		}else{
			Integer currentNum = Integer.valueOf(numStr);
			if(currentNum >= 5000&&currentNum<5999){
				num = Integer.valueOf(numStr) + 1;
			}else if(currentNum <5000){
				num = 5000;
			}else{
				num = -1;
				logger.error("初始化代理商ID失败：" + num + "=" + currentNum);
			}
		}
		StaticInitVariable.AGENTID_PRE = agentIdPre;
		StaticInitVariable.AGENT_NUM = num;
	}
}
