package com.ucpaas.sms.util;

import com.ucpaas.sms.model.Menu;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MenuUtil {
	private static final Logger logger = LoggerFactory.getLogger(MenuUtil.class);
	private static List<Menu> topMenus = new ArrayList<>();

	public static List<Menu> getTopMenus() {
		try {
			return deepCopy(topMenus);
		} catch (ClassNotFoundException | IOException e) {
			logger.error("菜单复制失败", e);
		}
		return topMenus;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void initMenu() throws DocumentException {
		logger.debug("初始化菜单");
		SAXReader reader = new SAXReader();
		// Document document = reader.read(new File("menu.xml"));
		Document document = reader.read(MenuUtil.class.getResourceAsStream("/menu.xml"));
		Element root = document.getRootElement();
		List menu = root.elements("menu");
		for (Iterator it = menu.iterator(); it.hasNext();) {
			Element elm = (Element) it.next();
			Menu top = new Menu();
			top.setName(elm.elementText("name"));
			top.setUrl(elm.elementText("url"));
			top.setClassName(elm.elementText("className"));

			List subMenusElms = elm.elements("subMenus");
			if (subMenusElms != null & subMenusElms.size() > 0) {
				List subMenus = new ArrayList<>();
				for (Iterator subIt = subMenusElms.iterator(); subIt.hasNext();) {
					Element subMnuesElm = (Element) subIt.next();
					List menusElms = subMnuesElm.elements("menu");

					for (Iterator subMenuElmIt = menusElms.iterator(); subMenuElmIt.hasNext();) {
						Element subMenuElm = (Element) subMenuElmIt.next();
						// System.out.println("----"+subMenuElm.getName());
						Menu subMenu = new Menu();
						subMenu.setName(subMenuElm.elementText("name"));
						subMenu.setUrl(subMenuElm.elementText("url"));
						subMenu.setClassName(subMenuElm.elementText("className"));
						subMenus.add(subMenu);
					}
				}
				top.setSubMenus(subMenus);
			}
			topMenus.add(top);
		}

		logger.debug("初始化结束，菜单{}", JsonUtils.toJson(topMenus));
	}

	public static <T> List<T> deepCopy(List<T> src) throws IOException, ClassNotFoundException {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(byteOut);
		out.writeObject(src);

		ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
		ObjectInputStream in = new ObjectInputStream(byteIn);
		@SuppressWarnings("unchecked")
		List<T> dest = (List<T>) in.readObject();
		return dest;
	}

	public static void main(String[] args) throws DocumentException {
		initMenu();

		for (Menu menu : topMenus) {
			System.out.println(menu);
		}
	}
}
