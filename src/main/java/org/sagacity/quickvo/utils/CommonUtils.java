package org.sagacity.quickvo.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @TODO 提供quickvo的一些工具类
 * @author zhongxuchen
 *
 */
public class CommonUtils {
	/**
	 * @todo 判断字符串是否为数字
	 * @param numberStr
	 * @return
	 */
	public static boolean isNumber(String numberStr) {
		return StringUtil.matches(numberStr, "^[+-]?[\\d]+(\\.\\d+)?$");
	}

	/**
	 * @todo 格式化日期
	 * @param dt
	 * @param fmt
	 * @return
	 */
	public static String formatDate(Date dt, String fmt) {
		DateFormat df = new SimpleDateFormat(fmt);
		return df.format(dt);
	}

	public static Date getNowTime() {
		return Calendar.getInstance().getTime();
	}
}
