package weixin.tool;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.log4j.Logger;

import org.springframework.stereotype.Service;

import weixin.WXApi;

/**
 * 获取单例实例，需保证线程安全
 */
@Service
public class InstanceFactory {
	
	public static Logger log = Logger.getLogger(InstanceFactory.class);


	
	private static InstanceFactory factory;
	
	private static AccessToken accessToken = null;
	
	
	public synchronized static AccessToken getAccessToken() {
		accessToken = null;
		if (accessToken == null || System.currentTimeMillis() - accessToken.getCreateDate() > accessToken.getExpiresIn()) {
			accessToken = WXApi.getAccessTokenFromWX();
			log.info("get accessToken from weixin：" + accessToken.getToken());
			
			log.info("update Systemdef.ACCESS_TOKEN");
			
			return accessToken;
		}else{
			log.info("get accessToken from cache：" + accessToken.getToken());
			return accessToken;
		}
	}

}
