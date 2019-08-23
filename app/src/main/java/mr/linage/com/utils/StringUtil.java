package mr.linage.com.utils;

/**
 * String Utility Class
 * 
 * @author ZeroOne
 * @version 1.0, 2009.06.25
 * 
 */

public class StringUtil {

	protected StringUtil() {
		
	}

	/**
	 * 입력값이 null인지를 체크하여 "" 을 돌려줌
	 * 
	 * @param str
	 * @return
	 */
	public static String nvl(String str) {
		String nv = "";
		try {
			if (str == null || str.length() == 0 || str.equals("null")
					|| str.equals("NULL"))
				nv = "";
			else
				nv = str;
		} catch (Exception e) {
			System.out.println("Utilb.nvl" + e.toString());
		}
		return nv;
	}
	
	public static String nvl(String src, String s) {
		src = nvl(src);
		if ("".equals(src))
			return s;
		else
			return src;
	}
	
}