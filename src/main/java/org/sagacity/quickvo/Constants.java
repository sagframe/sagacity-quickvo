/**
 * 
 */
package org.sagacity.quickvo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.sagacity.quickvo.dialect.DefaultConstants;
import org.sagacity.quickvo.dialect.OracleConstants;
import org.sagacity.quickvo.dialect.PostgresConstants;
import org.sagacity.quickvo.model.ColumnTypeMapping;
import org.sagacity.quickvo.utils.DBUtil;
import org.sagacity.quickvo.utils.DBUtil.DBType;
import org.sagacity.quickvo.utils.FileUtil;
import org.sagacity.quickvo.utils.StringUtil;
import org.sagacity.quickvo.utils.YamlUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @project sagacity-quickvo
 * @description quickVO涉及的常量定义
 * @author zhongxuchen
 * @version v1.0,Date:2009-04-18
 */
@SuppressWarnings({ "rawtypes" })
public class Constants implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8594672495773042796L;

	/**
	 * dto 类freemarker模板
	 */
	public static String dtoTempalte = "org/sagacity/quickvo/dto.ftl";
	public static String dtoAbstractTempalte = "org/sagacity/quickvo/dto-abstract.ftl";
	public static String dtoParentTempalte = "org/sagacity/quickvo/dto-parent.ftl";

	/**
	 * entity实体对象类freemarker模板
	 */
	public static String entityTemplate = "org/sagacity/quickvo/entity.ftl";
	public static String entityLombokTemplate = "org/sagacity/quickvo/entity-lombok.ftl";

	public static String abstractEntity = "org/sagacity/quickvo/abstract-entity.ftl";
	public static String parentEntity = "org/sagacity/quickvo/parent-entity.ftl";

	/**
	 * 主键默认生成策略
	 */
	public static String PK_DEFAULT_GENERATOR = "org.sagacity.sqltoy.plugins.id.impl.DefaultIdGenerator";

	/**
	 * uuid主键策略
	 */
	public static String PK_UUID_GENERATOR = "org.sagacity.sqltoy.plugins.id.impl.UUIDGenerator";

	/**
	 * twitter的雪花id算法
	 */
	public static String PK_SNOWFLAKE_GENERATOR = "org.sagacity.sqltoy.plugins.id.impl.SnowflakeIdGenerator";

	/**
	 * 26位纳秒时序ID产生策略
	 */
	public static String PK_NANOTIME_ID_GENERATOR = "org.sagacity.sqltoy.plugins.id.impl.NanoTimeIdGenerator";

	/**
	 * 基于redis产生id
	 */
	public static String PK_REDIS_ID_GENERATOR = "org.sagacity.sqltoy.plugins.id.impl.RedisIdGenerator";

	public static String fieldsBegin = "/*---begin-auto-generate-don't-update-this-area--*/";
	public static String fieldsEnd = "/*---end-auto-generate-don't-update-this-area--*/";
	public static String constructorBegin = "/*---begin-constructor-area---don't-update-this-area--*/";
	public static String constructorEnd = "/*---end-constructor-area---don't-update-this-area--*/";
	public static String pkStructRegs = "\\/[\\*]{1,2}\\s*pk\\s+constructor\\s*\\*\\/";

	/**
	 * 运行时默认路径
	 */
	public static String BASE_LOCATE;

	public static String LOG_FILE_ENCODING = "UTF-8";

	public static String QUICK_CONFIG_FILE = "quickvo.xml";

	private static final String GLOBA_IDENTITY_NAME = "globa.identity";

	private static final String GLOBA_IDENTITY = "##{globa.identity}";

	/**
	 * @TODO 根据数据库类型获取类型匹配
	 * @param dbType
	 * @return
	 */
	public static String[][] getJdbcTypeMapping(int dbType) {
		switch (dbType) {
		case DBType.POSTGRESQL:
		case DBType.GAUSSDB: {
			return PostgresConstants.jdbcTypeMapping;
		}
		default:
			return DefaultConstants.jdbcTypeMapping;
		}
	}

	/**
	 * 原始类型
	 */
	public static final String[][] prototype = { { "int", "1" }, { "short", "1" }, { "long", "1" }, { "float", "1" },
			{ "double", "1" }, { "char", "2" }, { "byte", "2" }, { "boolean", "2" } };

	// public static String[]
	// native type 对应java.sql.Types.xxxx

	/**
	 * 全局常量map
	 */
	private static HashMap<String, String> constantMap = new HashMap<String, String>();

	public static int getMaxScale() {
		String maxScale = getKeyValue("max.scale.length");
		if (maxScale == null) {
			return -1;
		}
		return Integer.parseInt(maxScale);
	}

	/**
	 * 是否存在表中字段重复(多个schema场景出现了隔离问题)
	 */
	public static boolean hasRepeatField = false;

	/**
	 * @todo 加载xml中的参数
	 * @param paramElts
	 * @throws Exception
	 */
	public static void loadProperties(NodeList paramElts) throws Exception {
		String guid = System.getProperty(GLOBA_IDENTITY_NAME);
		if (guid == null) {
			guid = "";
		}
		// 加载任务配置文件中的参数
		if (paramElts != null && paramElts.getLength() > 0) {
			Element elt;
			for (int i = 0; i < paramElts.getLength(); i++) {
				elt = (Element) paramElts.item(i);
				if (elt.hasAttribute("name")) {
					if (elt.hasAttribute("value")) {
						constantMap.put(elt.getAttribute("name"), replaceConstants(
								StringUtil.replaceAllStr(elt.getAttribute("value"), GLOBA_IDENTITY, guid)));
					} else {
						constantMap.put(elt.getAttribute("name"), replaceConstants(
								StringUtil.replaceAllStr(StringUtil.trim(elt.getTextContent()), GLOBA_IDENTITY, guid)));
					}
				} else if (elt.hasAttribute("file")) {
					loadPropertyFile(
							replaceConstants(StringUtil.replaceAllStr(elt.getAttribute("file"), GLOBA_IDENTITY, guid)));
				}
			}
		}
	}

	/**
	 * @todo 替换常量参数
	 * @param target
	 * @return
	 */
	public static String replaceConstants(String target) {
		if (constantMap == null || constantMap.size() < 1 || target == null) {
			return target;
		}
		String result = target;
		// 简化匹配规则
		if (StringUtil.matches(result, "\\$\\{") && StringUtil.matches(result, "\\}")) {
			Iterator iter = constantMap.entrySet().iterator();
			Map.Entry entry;
			String key;
			String value;
			while (iter.hasNext()) {
				entry = (Map.Entry) iter.next();
				key = "${" + entry.getKey() + "}";
				value = (String) entry.getValue();
				result = StringUtil.replaceAllStr(result, key, value);
			}
		}
		return result;
	}

	/**
	 * @TODO 是否忽视主键约束信息提取(主要针对postgresql)
	 * @return
	 */
	public static boolean getSkipPkConstraint() {
		String pkConstraint = getKeyValue("skip.primary.constraint");
		if (StringUtil.isBlank(pkConstraint)) {
			return false;
		}
		return Boolean.parseBoolean(pkConstraint);
	}

	/**
	 * @todo 加载properties文件
	 * @param propertyFile
	 * @throws IOException
	 */
	private static void loadPropertyFile(String propertyFile) throws Exception {
		if (StringUtil.isNotBlank(propertyFile)) {
			File propFile;
			// 判断根路径
			if (FileUtil.isRootPath(propertyFile)) {
				propFile = new File(propertyFile);
			} else {
				propFile = new File(FileUtil.skipPath(Constants.BASE_LOCATE, propertyFile));
			}
			if (!propFile.exists()) {
				throw new Exception("参数文件:" + propertyFile + "不存在,请确认!");
			}
			// yml格式
			if (propertyFile.toLowerCase().endsWith("yml")) {
				YamlUtil.loadYml(constantMap, propFile);
			} else {
				Properties props = new Properties();
				props.load(new FileInputStream(propFile));
				Enumeration en = props.propertyNames();
				String key;
				while (en.hasMoreElements()) {
					key = (String) en.nextElement();
					constantMap.put(key, props.getProperty(key));
				}
			}
		}
	}

	/**
	 * @todo 获取常量信息
	 * @param key
	 * @return
	 */
	public static String getPropertyValue(String key) {
		if (StringUtil.isBlank(key)) {
			return key;
		}
		String realKey = key.trim();
		// 简化匹配规则
		if (realKey.startsWith("${") && realKey.endsWith("}")) {
			return (String) getKeyValue(realKey.substring(2, realKey.length() - 1));
		}
		if (getKeyValue(key) != null) {
			return getKeyValue(key);
		}
		return key;
	}

	/**
	 * @todo 获取常量信息
	 * @param key
	 * @return
	 */
	public static String getKeyValue(String key) {
		if (StringUtil.isBlank(key)) {
			return key;
		}
		String value = (String) constantMap.get(key);
		if (null == value) {
			value = System.getProperty(key);
		}
		return value;
	}

	/**
	 * @TODO 是否包含schema(用于对象查询时,表名会自动包含schema,默认不包含)
	 * @return
	 */
	public static boolean includeSchema() {
		String result = getKeyValue("include.schema");
		if (StringUtil.isBlank(result)) {
			return false;
		}
		return Boolean.parseBoolean(result);
	}

	public static String getJdbcType(String jdbcType, int dbType) {
		if (dbType == DBUtil.DBType.CLICKHOUSE && jdbcType.equalsIgnoreCase("datetime")) {
			return "TIMESTAMP";
		}
		String[][] jdbcArray = null;
		switch (dbType) {
		case DBType.POSTGRESQL:
		case DBType.GAUSSDB: {
			jdbcArray = PostgresConstants.jdbcAry;
			break;
		}
		case DBType.ORACLE11:
		case DBType.ORACLE: {
			jdbcArray = OracleConstants.jdbcAry;
			break;
		}
		default:
			jdbcArray = DefaultConstants.jdbcAry;
		}
		for (String[] types : jdbcArray) {
			if (jdbcType.equalsIgnoreCase(types[0])) {
				return types[1];
			}
		}
		return jdbcType;
	}

	/**
	 * @todo 设置默认类型匹配
	 * @param typeMapping
	 */
	public static void addDefaultTypeMapping(List<ColumnTypeMapping> typeMapping) {
		ColumnTypeMapping typeMapping1 = new ColumnTypeMapping();
		typeMapping1.putNativeTypes(new String[] { "NUMBER", "DECIMAL", "NUMERIC" });
		typeMapping1.setJdbcType("INTEGER");
		typeMapping1.setJavaType("Integer");
		typeMapping1.setPrecisionMax(8);
		typeMapping1.setPrecisionMin(1);
		typeMapping1.setScaleMin(0);
		typeMapping1.setScaleMax(0);
		typeMapping1.setResultType("Integer");
		typeMapping.add(typeMapping1);
		ColumnTypeMapping typeMapping2 = new ColumnTypeMapping();
		typeMapping2.putNativeTypes(new String[] { "NUMBER", "DECIMAL", "NUMERIC" });
		typeMapping2.setJdbcType("INTEGER");
		typeMapping2.setJavaType("Long");
		typeMapping2.setPrecisionMax(64);
		typeMapping2.setPrecisionMin(9);
		typeMapping2.setScaleMin(0);
		typeMapping2.setScaleMax(0);
		typeMapping2.setResultType("Long");
		typeMapping.add(typeMapping2);
	}

}
