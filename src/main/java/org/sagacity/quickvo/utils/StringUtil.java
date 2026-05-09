/**
 * @Copyright 2007 版权归陈仁飞，不要肆意侵权抄袭，如引用请注明出处保留作者信息。
 */
package org.sagacity.quickvo.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @project sagacity-quickvo
 * @description 字符串处理常用功能
 * @author zhongxuchen
 * @version v1.0,Date:2007-10-19
 */
public class StringUtil {
	/**
	 * private constructor,cann't be instantiated by other class 私有构造函数方法防止被实例化
	 */
	private StringUtil() {
	}

	// 数据库注释转义映射表（核心规则：仅转义必要字符，避免重复转义）
	private static final Map<Character, String> DEFAULT_ESCAPE_MAP;

	static {
		Map<Character, String> tempMap = new HashMap<>();
		// 核心规则：仅添加1个反斜杠（避免框架二次转义导致冗余）
		tempMap.put('\\', "\\"); // 反斜杠：保留1个，防止重复转义
		tempMap.put('$', "\\$"); // 美元符：$ → \$（避免EL表达式解析）
		tempMap.put('{', "\\{"); // 左花括号：{ → \{
		tempMap.put('}', "\\}"); // 右花括号：} → \}
		tempMap.put('"', "\\\""); // 双引号：" → \"（适配数据库字符串解析）
		tempMap.put('\'', "\\'"); // 单引号：' → \'（适配数据库字符串解析）
		tempMap.put('%', "\\%"); // 百分号：% → \%（避免MySQL通配符解析）
		tempMap.put('_', "\\_"); // 下划线：_ → \_（避免MySQL通配符解析）
		// 包装为不可变Map，防止规则被篡改
		DEFAULT_ESCAPE_MAP = Collections.unmodifiableMap(tempMap);
	}

	/**
	 * 转义注释中的特殊字符（修复：逐字符检测，避免重复转义+部分转义遗漏）
	 *
	 * @param commentStr 原始注释字符串（可为null/空）
	 * @return 转义后的字符串，null/空输入返回原值
	 */
	public static String escapeComment(String commentStr) {
		// 1. 空值安全处理：null/空字符串直接返回
		if (commentStr == null || commentStr.isEmpty()) {
			return commentStr;
		}

		// 2. 逐字符处理：检测当前字符是否已被转义，仅处理未转义的特殊字符
		StringBuilder sb = new StringBuilder((int) (commentStr.length() * 1.2));
		char[] chars = commentStr.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			// 关键：检测当前字符的前一个字符是否是反斜杠（即当前字符已被转义）
			boolean isEscaped = (i > 0 && chars[i - 1] == '\\');

			// 仅对「未被转义」且「在转义映射表中」的字符进行转义
			if (!isEscaped && DEFAULT_ESCAPE_MAP.containsKey(c)) {
				sb.append(DEFAULT_ESCAPE_MAP.get(c));
			} else {
				// 已转义的字符/普通字符：直接保留
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * @todo 判断字符串是空或者空白
	 * @param str
	 * @return
	 */
	public static boolean isNotBlank(Object str) {
		return !isBlank(str);
	}

	public static boolean isBlank(Object str) {
		if (null == str || str.toString().trim().equals("")) {
			return true;
		}
		return false;
	}

	public static String trim(String str) {
		if (null == str) {
			return null;
		}
		return str.trim();
	}

	/**
	 * @todo 替换换行、回车、tab符号;\r 换行、\t tab符合、\n 回车
	 * @param source
	 * @param target
	 * @return
	 */
	public static String clearMistyChars(String source, String target) {
		if (null == source) {
			return null;
		}
		return source.replaceAll("\t|\r|\n", target);
	}

	/**
	 * @todo 返回第一个字符大写，其余保持不变的字符串
	 * @param sourceStr
	 * @return
	 */
	public static String firstToUpperCase(String sourceStr) {
		if (isBlank(sourceStr)) {
			return sourceStr;
		}
		if (sourceStr.length() == 1) {
			return sourceStr.toUpperCase();
		}
		return sourceStr.substring(0, 1).toUpperCase().concat(sourceStr.substring(1));
	}

	/**
	 * @todo 返回第一个字符小写，其余保持不变的字符串
	 * @param sourceStr
	 * @return
	 */
	public static String firstToLowerCase(String sourceStr) {
		if (isBlank(sourceStr)) {
			return sourceStr;
		}
		if (sourceStr.length() == 1) {
			return sourceStr.toLowerCase();
		}
		return sourceStr.substring(0, 1).toLowerCase().concat(sourceStr.substring(1));
	}

	/**
	 * @todo 返回第一个字符大写，其余保持不变的字符串
	 * @param sourceStr
	 * @return
	 */
	public static String firstToUpperOtherToLower(String sourceStr) {
		if (isBlank(sourceStr)) {
			return sourceStr;
		}
		if (sourceStr.length() == 1) {
			return sourceStr.toUpperCase();
		}
		return sourceStr.substring(0, 1).toUpperCase().concat(sourceStr.substring(1).toLowerCase());
	}

	/**
	 * @todo 在不分大小写情况下字符所在位置
	 * @param source
	 * @param pattern
	 * @return
	 */
	public static int indexOfIgnoreCase(String source, String pattern) {
		if (null == source || null == pattern) {
			return -1;
		}
		return source.toLowerCase().indexOf(pattern.toLowerCase());
	}

	/**
	 * @todo 针对jdk1.4 replace(char,char)提供jdk1.5中replace(String,String)的功能
	 * @param source
	 * @param template
	 * @param target
	 * @return
	 */
	public static String replaceStr(String source, String template, String target) {
		return replaceStr(source, template, target, 0);
	}

	public static String replaceStr(String source, String template, String target, int fromIndex) {
		if (null == source) {
			return null;
		}
		if (null == template) {
			return source;
		}
		if (fromIndex >= source.length() - 1) {
			return source;
		}
		int index = source.indexOf(template, fromIndex);
		if (index != -1) {
			source = source.substring(0, index).concat(target).concat(source.substring(index + template.length()));
		}
		return source;
	}

	/**
	 * @todo 针对jdk1.4 replace(char,char)提供jdk1.5中replace(String,String)的功能
	 * @param source
	 * @param template
	 * @param target
	 * @return
	 */
	public static String replaceAllStr(String source, String template, String target) {
		return replaceAllStr(source, template, target, 0);
	}

	public static String replaceAllStr(String source, String template, String target, int fromIndex) {
		if (source == null || template.equals(target)) {
			return source;
		}
		int index = source.indexOf(template, fromIndex);
		int subLength = target.length() - template.length();
		int begin = index - 1;
		while (index != -1 && index >= begin) {
			source = source.substring(0, index).concat(target).concat(source.substring(index + template.length()));
			begin = index + subLength + 1;
			index = source.indexOf(template, begin);
		}
		return source;
	}

	/**
	 * @todo 通过正则表达式判断是否匹配
	 * @param source
	 * @param regex
	 * @return
	 */
	public static boolean matches(String source, String regex) {
		return matches(source, Pattern.compile(regex));
	}

	/**
	 * @todo 通过正则表达式判断是否匹配
	 * @param source
	 * @param p
	 * @return
	 */
	public static boolean matches(String source, Pattern p) {
		return p.matcher(source).find();
	}

	/**
	 * @todo 将字符串转换成驼峰形式
	 * @param source
	 * @param firstIsUpperCase
	 * @return
	 */
	public static String toHumpStr(String source, boolean firstIsUpperCase, boolean removeDnderline) {
		if (StringUtil.isBlank(source)) {
			return source;
		}
		String[] humpAry = source.split("\\_");
		String cell;
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < humpAry.length; i++) {
			cell = humpAry[i];
			if (!removeDnderline && i > 0) {
				result.append("_");
			}
			// 全大写或全小写
			if (cell.toUpperCase().equals(cell)) {
				result.append(firstToUpperOtherToLower(cell));
			} else {
				result.append(firstToUpperCase(cell));
			}
		}
		// 首字母变大写
		if (firstIsUpperCase) {
			return firstToUpperCase(result.toString());
		}
		return firstToLowerCase(result.toString());
	}

	public static String subStart(String source, String start) {
		if (isBlank(start)) {
			return source;
		}
		if (source.toLowerCase().startsWith(start.toLowerCase())) {
			return source.substring(start.length());
		}
		return source;
	}

	/**
	 * @todo 提供字符串hash算法,产生vo对象的serialVersionUID值
	 * @param key
	 * @return
	 */
	public static long hash(String key) {
		ByteBuffer buf = ByteBuffer.wrap(key.getBytes());
		int seed = 0x1234ABCD;
		ByteOrder byteOrder = buf.order();
		buf.order(ByteOrder.LITTLE_ENDIAN);
		long m = 0xc6a4a7935bd1e995L;
		int r = 47;
		long h = seed ^ (buf.remaining() * m);
		long k;
		while (buf.remaining() >= 8) {
			k = buf.getLong();
			k *= m;
			k ^= k >>> r;
			k *= m;
			h ^= k;
			h *= m;
		}
		if (buf.remaining() > 0) {
			ByteBuffer finish = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
			// for big-endian version, do this first:
			// finish.position(8-buf.remaining());
			finish.put(buf).rewind();
			h ^= finish.getLong();
			h *= m;
		}
		h ^= h >>> r;
		h *= m;
		h ^= h >>> r;
		buf.order(byteOrder);
		return Math.abs(h);
	}

	/**
	 * @todo 获取匹配成功的个数
	 * @param source
	 * @param regex
	 * @return
	 */
	public static int matchCnt(String source, String regex) {
		return matchCnt(source, Pattern.compile(regex));
	}

	/**
	 * @todo 获取匹配成功的个数
	 * @param Pattern
	 * @param source
	 * @return
	 */
	public static int matchCnt(String source, Pattern p) {
		Matcher m = p.matcher(source);
		int count = 0;
		while (m.find()) {
			count++;
		}
		return count;
	}

	/**
	 * @todo 获取匹配成功的个数
	 * @param source
	 * @param regex
	 * @param beginIndex
	 * @param endIndex
	 * @return
	 */
	public static int matchCnt(String source, String regex, int beginIndex, int endIndex) {
		return matchCnt(source.substring(beginIndex, endIndex), Pattern.compile(regex));
	}
}
