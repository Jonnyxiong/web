package com.ucpaas.sms.model;

import java.io.Serializable;
import java.util.List;

public class Menu implements Serializable {
	private String name;
	private String url;
	private List<Menu> subMenus;
	private String className;
	private String style;
	private Integer show;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<Menu> getSubMenus() {
		return subMenus;
	}

	public void setSubMenus(List<Menu> subMenus) {
		this.subMenus = subMenus;
	}

	@Override
	public String toString() {
		return "Menu [name=" + name + ", url=" + url + ", subMenus=" + subMenus + "]";
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public Integer getShow() {
		return show;
	}

	public void setShow(Integer show) {
		this.show = show;
	}
}
