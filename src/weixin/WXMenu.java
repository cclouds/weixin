package weixin;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;

import weixin.menu.Button;
import weixin.menu.ComplexButton;
import weixin.menu.Menu;
import weixin.menu.ViewButton;

public class WXMenu {

	public static Logger logger = Logger.getLogger(WXMenu.class);

	public static void main(String[] args) {
		String json = JSONObject.toJSON(getMenu()).toString();
		System.out.println(json);
		int result = WXApi.createMenu(json, WXApi.getAccessToken());
		if (0 == result) {
			logger.info("success");
		} else {
			logger.info("failure：" + result);
		}
	}

	/**
	 * 组装菜单数据
	 * 
	 * @return
	 */
	private static Menu getMenu() {

		ViewButton btn12 = new ViewButton();
		btn12.setName("");
		btn12.setType("view");
		btn12.setUrl("");
		// ----------------------------------------------
		
		ViewButton btn21 = new ViewButton();
		btn21.setName("");
		btn21.setType("view");
		btn21.setUrl("");

		ViewButton btn22 = new ViewButton();
		btn22.setName("");
		btn22.setType("view");
		btn22.setUrl("");

		// ----------------------------------------------

		ComplexButton mainBtn1 = new ComplexButton();
		mainBtn1.setName("");
		mainBtn1.setSub_button(new Button[] {btn12 });

		ComplexButton mainBtn2 = new ComplexButton();
		mainBtn2.setName("");
		mainBtn2.setSub_button(new Button[] { btn21, btn22 });

		ViewButton mainBtn3 = new ViewButton();
		mainBtn3.setName("");
		mainBtn3.setType("");
		mainBtn3.setUrl("");
		
		Menu menu = new Menu();
		menu.setButton(new Button[] { mainBtn3, mainBtn1, mainBtn2 });

		return menu;
	}
}
