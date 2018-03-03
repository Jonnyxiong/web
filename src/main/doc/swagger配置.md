### 引入swagger包
```xml
		<!-- api doc swagger -->
		<dependency>
			<groupId>io.springfox</groupId>
			<artifactId>springfox-swagger2</artifactId>
			<version>2.4.0</version>
		</dependency>
		<dependency>
			<groupId>io.springfox</groupId>
			<artifactId>springfox-swagger-ui</artifactId>
			<version>2.4.0</version>
		</dependency>
```

### 创建类ApplicationSwaggerConfig
```java
package com.ucpaas.sms.common.swagger;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
public class ApplicationSwaggerConfig {      
}
```

### 配置springmvc
```xml

	<!-- swagger配置 begin-->
    <!-- Include a swagger configuration-->
    <bean name="/applicationSwaggerConfig" class="com.ucpaas.sms.common.swagger.ApplicationSwaggerConfig"/>
    <mvc:resources location="classpath:/META-INF/resources/" mapping="swagger-ui.html"/>
    <mvc:resources location="classpath:/META-INF/resources/webjars/" mapping="/webjars/**" />
   
	<!-- swagger配置 end-->
```


### 权限管理，开发环境才可以访问http://localhost:8080/swagger-ui.html
#### 在system.properties配置:
```properties
#api是否开启,地址 http://localhost:8080/swagger-ui.html
#true为开启，其他为关闭
swagger_switch=true
```

#### 修改filter，添加方法
```java
	private static List<String> swaggerList = new ArrayList<String>();
	
	{ 
		//swagger不拦截begin
		swaggerList.add("/swagger-ui.html"); 
		swaggerList.add("/v2/api-docs"); 
		swaggerList.add("/configuration/ui");  
		swaggerList.add("/swagger-resources");  
		//swagger不拦截end
	}
	
	private boolean isSwagger(String reqURI){
		if(swaggerList.contains(reqURI)){
			return true;
		}

		//swagger不拦截 begin
		if(reqURI!=null && reqURI.contains("springfox-swagger-ui")){
			
			return true;
		}
		
		return false;
	}
```
修改方法doFilter
```java
		if(isSwagger(reqURI)){ //swagger网页
			if("true".equals(ConfigUtils.swagger_switch)){ //开发环境和测试环境开启
				filterChain.doFilter(request, response);
			}else{
				response.sendRedirect(request.getContextPath() + "/login");
			}
			return;
		}
```
