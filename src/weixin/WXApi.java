package weixin;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import weixin.tool.AccessToken;
import weixin.tool.InstanceFactory;
import weixin.tool.MyX509TrustManager;
import weixin.tool.SystemConstant;

public class WXApi {

	public static Logger log = Logger.getLogger(WXApi.class);

	/**
	 * 发起https请求并获取结果
	 * 
	 * @param requestUrl
	 *            请求地址
	 * @param requestMethod
	 *            请求方式（GET、POST）
	 * @param outputStr
	 *            提交的数据
	 * @return JSONObject(通过JSONObject.get(key)的方式获取json对象的属性值)
	 */
	public static JSONObject httpRequest(String requestUrl, String requestMethod, String outputStr) {
		JSONObject jsonObject = null;
		StringBuffer buffer = new StringBuffer();
		try {
			// 创建SSLContext对象，并使用我们指定的信任管理器初始化
			TrustManager[] tm = { new MyX509TrustManager() };
			SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
			sslContext.init(null, tm, new java.security.SecureRandom());
			// 从上述SSLContext对象中得到SSLSocketFactory对象
			SSLSocketFactory ssf = sslContext.getSocketFactory();
			URL url = new URL(requestUrl);
			HttpsURLConnection httpUrlConn = (HttpsURLConnection) url.openConnection();
			httpUrlConn.setSSLSocketFactory(ssf);
			httpUrlConn.setDoOutput(true);
			httpUrlConn.setDoInput(true);
			httpUrlConn.setUseCaches(false);
			// 设置请求方式（GET/POST）
			httpUrlConn.setRequestMethod(requestMethod);
			if ("GET".equalsIgnoreCase(requestMethod))
				httpUrlConn.connect();
			// 当有数据需要提交时
			if (null != outputStr) {
				OutputStream outputStream = httpUrlConn.getOutputStream();
				// 注意编码格式，防止中文乱码
				outputStream.write(outputStr.getBytes("UTF-8"));
				outputStream.close();
			}
			// 将返回的输入流转换成字符串
			InputStream inputStream = httpUrlConn.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String str = null;
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str);
			}
			bufferedReader.close();
			inputStreamReader.close();
			// 释放资源
			inputStream.close();
			inputStream = null;
			httpUrlConn.disconnect();
			jsonObject = JSONObject.parseObject(buffer.toString());
		} catch (ConnectException ce) {
			log.error("weixin server connection timed out");
		} catch (Exception e) {
			log.error(e);
		}
		return jsonObject;
	}
	
//	public static String encryptOpenid(String openid, String dynamicpwd){
//		if(dynamicpwd == null){
//			dynamicpwd = SystemConstant.WEIXIN_KEY;
//		}
//		return AES.encryptToBase64(openid, dynamicpwd) + "~" + AES.encryptToBase64(dynamicpwd.substring(7) + dynamicpwd.substring(0, 7), SystemConstant.WEIXIN_KEY);
//	}
	
//	public static String decryptOpenid(String wid){
//		String[] widArr = wid.split("~");
//		String dynamicpwd = AES.decryptFromBase64(URLDecoder.decode(widArr[1]).replace(" ", "+"), SystemConstant.WEIXIN_KEY);
//		dynamicpwd = dynamicpwd.substring(9) + dynamicpwd.substring(0, 9);
//		return AES.decryptFromBase64(URLDecoder.decode(widArr[0]).replace(" ", "+"), dynamicpwd);
//	}
	
	public static String getAccessToken(){
		return InstanceFactory.getAccessToken().getToken();
	}
	
	/**
	 * 创建菜单
	 */
	public static int createMenu(String json, String accessToken) {
		int result = 0;
		String url = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token="+accessToken;
		JSONObject jsonObject = httpRequest(url, "POST", json);
		if (null != jsonObject) {
			if (0 != jsonObject.getInteger("errcode")) {
				result = jsonObject.getInteger("errcode");
				log.error("failure：" + jsonObject.getString("errmsg"));
			}
		}
		return result;
	}

	/**
	 * 获取access_token，限2000（次/天）
	 * 
	 * @param appid 凭证
	 * @param appsecret 密钥
	 * @return
	 */
	public static AccessToken getAccessTokenFromWX() {
		AccessToken accessToken = null;
		String requestUrl = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + SystemConstant.WEIXIN_APPID + "&secret=" + SystemConstant.WEIXIN_APPSECRET;
		JSONObject jsonObject = httpRequest(requestUrl, "GET", null);
		if (null != jsonObject) {
			try {
				accessToken = new AccessToken();
				accessToken.setToken(jsonObject.getString("access_token"));
				accessToken.setExpiresIn(90 * 60 * 1000);// 微信是120m，7200s，调整为90m，5400s
				accessToken.setCreateDate(System.currentTimeMillis());
			} catch (JSONException e) {
				accessToken = null;
				log.error("获取token失败：" + jsonObject.getString("errmsg"));
			}
		}
		return accessToken;
	}
	
	public static String getOpenid(String code){
		String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+SystemConstant.WEIXIN_APPID+"&secret="+SystemConstant.WEIXIN_APPSECRET+"&code="+code+"&grant_type=authorization_code";
		log.info(url);
		JSONObject json = httpRequest(url, "GET", null);
		log.info(json.toString());
		if (json.containsKey("openid")) {
			String openid = json.get("openid").toString();
			log.info("openid:"+openid);
			return openid;
		}
		return null;
	}
	

	public static JSONObject getUserInfo(String openid) {
		String url = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=" + getAccessToken() + "&openid=" + openid + "&lang=zh_CN";
		return httpRequest(url, "GET", null);
	}

	public static String downloadMedia(String mediaId) {
		String filePath = null;
		// 拼接请求地址
		String requestUrl = "https://api.weixin.qq.com/cgi-bin/media/get?access_token=" + getAccessToken() + "&media_id=" + mediaId;
		try {
			URL url = new URL(requestUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.setRequestMethod("GET");

			String savePath = "/upload/";
			File dir = new File(SystemConstant.IMAGE_LOCATION + savePath);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			// 将mediaId作为文件名
			filePath = savePath + mediaId + ".jpg";
			log.info("fileName:" + filePath);

			BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
			FileOutputStream fos = new FileOutputStream(new File(SystemConstant.IMAGE_LOCATION + filePath));
			byte[] buf = new byte[8096];
			int size = 0;
			while ((size = bis.read(buf)) != -1) {
				fos.write(buf, 0, size);
			}
			fos.close();
			bis.close();

			conn.disconnect();
			log.info("下载媒体文件成功，filePath=" + filePath);
		} catch (Exception e) {
			log.error(e);
		}
		return filePath;
	}
	
	public static JSONObject sendNotify(String accessToken, String templateId, String openid, String dynamicpwd, String toUrl, Map<String, Object> data) {
		String url = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + accessToken;
		Map<String, Object> root = new HashMap<String, Object>();
		root.put("touser", openid);
		root.put("template_id", templateId);
		
		root.put("url", toUrl);
		root.put("data", data);
		return httpRequest(url, "POST", JSONObject.toJSON(root).toString());
	}
	

}
