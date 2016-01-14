package weixin.tool;

public class AccessToken {

	// 获取到的凭证
	private String token;
	// 凭证有效时间，单位：豪秒
	private int expiresIn;
	// 创建日期 毫秒值
	private long createDate;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public int getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(int expiresIn) {
		this.expiresIn = expiresIn;
	}

	public long getCreateDate() {
		return createDate;
	}

	public void setCreateDate(long createDate) {
		this.createDate = createDate;
	}
}
