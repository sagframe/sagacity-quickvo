/**
 * 
 */
package org.sagacity.quickvo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.sagacity.quickvo.model.BusinessIdConfig;
import org.sagacity.quickvo.model.CascadeModel;
import org.sagacity.quickvo.model.ChangeModel;
import org.sagacity.quickvo.model.ColumnTypeMapping;
import org.sagacity.quickvo.model.ConfigModel;
import org.sagacity.quickvo.model.PrimaryKeyStrategy;
import org.sagacity.quickvo.model.QuickColMeta;
import org.sagacity.quickvo.model.QuickModel;
import org.sagacity.quickvo.model.QuickVO;
import org.sagacity.quickvo.model.TableColumnMeta;
import org.sagacity.quickvo.model.TableConstractModel;
import org.sagacity.quickvo.model.TableMeta;
import org.sagacity.quickvo.utils.CommonUtils;
import org.sagacity.quickvo.utils.DBHelper;
import org.sagacity.quickvo.utils.DBUtil.DBType;
import org.sagacity.quickvo.utils.FileUtil;
import org.sagacity.quickvo.utils.FreemarkerUtil;
import org.sagacity.quickvo.utils.LoggerUtil;
import org.sagacity.quickvo.utils.StringUtil;

/**
 * @project sagacity-quickvo
 * @description 获取数据库表或视图信息,生成VO、AbstractVO文件
 * @author zhongxuchen
 * @version v1.0,Date:2010-7-21
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TaskController {
	/**
	 * 定义全局日志
	 */
	private static Logger logger = LoggerUtil.getLogger();

	/**
	 * dto模板
	 */
	private static String dtoTemplate;

	private static String dtoAbstractTemplate;

	private static String dtoParentTemplate;

	/**
	 * 实体对象模板
	 */
	private static String entityTemplate;

	private static String entityLombokTemplate;

	private static String entityAbstractTemplate;

	private static String entityParentTemplate;

	/**
	 * 任务配置
	 */
	private static ConfigModel configModel;

	/**
	 * @param configModel the configModel to set
	 */
	public static void setConfigModel(ConfigModel configModel) {
		TaskController.configModel = configModel;
	}

	/**
	 * @todo 加载freemarker模板
	 * @param configModel
	 */
	private static void init() {
		dtoTemplate = FileUtil.inputStream2String(FileUtil.getResourceAsStream(Constants.dtoTempalte),
				configModel.getEncoding());
		dtoAbstractTemplate = FileUtil.inputStream2String(FileUtil.getResourceAsStream(Constants.dtoAbstractTempalte),
				configModel.getEncoding());
		dtoParentTemplate = FileUtil.inputStream2String(FileUtil.getResourceAsStream(Constants.dtoParentTempalte),
				configModel.getEncoding());
		// 无抽象类的实体类模板
		entityTemplate = FileUtil.inputStream2String(FileUtil.getResourceAsStream(Constants.entityTemplate),
				configModel.getEncoding());
		entityLombokTemplate = FileUtil.inputStream2String(FileUtil.getResourceAsStream(Constants.entityLombokTemplate),
				configModel.getEncoding());
		entityAbstractTemplate = FileUtil.inputStream2String(FileUtil.getResourceAsStream(Constants.abstractEntity),
				configModel.getEncoding());
		entityParentTemplate = FileUtil.inputStream2String(FileUtil.getResourceAsStream(Constants.parentEntity),
				configModel.getEncoding());
	}

	/**
	 * @todo 总控启动执行所有任务
	 * @throws Exception
	 */
	public static void create() throws Exception {
		if (configModel == null || configModel.getTasks() == null || configModel.getTasks().isEmpty()) {
			logger.info("没有配置可执行任务信息或所有任务状态全部为失效!");
			return;
		}
		// 初始化模板的内容
		init();
		// 设置编码格式
		if (StringUtil.isBlank(configModel.getEncoding())) {
			FreemarkerUtil.getInstance().setEncoding(configModel.getEncoding());
		}
		// 循环执行任务
		QuickModel quickModel;
		String supportLinkedSet = Constants.getKeyValue("field.support.linked.set");
		String selectFieldsClass = Constants.getKeyValue("generate.selectFields.class");
		// 支持链式set
		boolean isLinkSet = false;
		if (StringUtil.isNotBlank(supportLinkedSet)) {
			isLinkSet = Boolean.parseBoolean(supportLinkedSet);
		}
		// 产生SelectField内部类
		boolean generateFieldsClass = false;
		if (StringUtil.isNotBlank(selectFieldsClass)) {
			generateFieldsClass = Boolean.parseBoolean(selectFieldsClass);
		}
		int i = 0;
		for (Iterator iter = configModel.getTasks().iterator(); iter.hasNext();) {
			i++;
			quickModel = (QuickModel) iter.next();
			boolean isConn = DBHelper.getConnection(quickModel.getDataSource());
			if (!isConn) {
				logger.info("数据库:[" + quickModel.getDataSource() + "]连接异常,请确认你的数据库配置信息或者数据库环境!");
			} else {
				logger.info("开始执行第:{" + i + "} 个任务,includes=:" + quickModel.getIncludeTables());
				createTask(quickModel, isLinkSet, generateFieldsClass);
				// 销毁数据库连接
				DBHelper.close();
			}
		}
	}

	/**
	 * @todo 执行单个任务生成vo
	 * @param quickModel
	 * @param supportLinkSet
	 * @param selectFields
	 * @throws Exception
	 */
	public static void createTask(QuickModel quickModel, boolean supportLinkSet, boolean selectFields)
			throws Exception {
		String[] includes = null;
		boolean skipPkConstraint = Constants.getSkipPkConstraint();
		if (quickModel.getIncludeTables() != null) {
			includes = new String[] { "(?i)".concat(quickModel.getIncludeTables()) };
		}
		int dbType = DBHelper.getDBType();
		String dialect = DBHelper.getDBDialect();
		// (?i)忽略大小写
		List tables = DBHelper.getTableAndView(includes, quickModel.getExcludeTables() == null ? null
				: new String[] { "(?i)".concat(quickModel.getExcludeTables()) });
		if (tables == null || tables.isEmpty()) {
			logger.info("/*--------没有取到匹配的表，错误原因提示,请严重关注!-------------------------------*/\n"
					+ "/*-1、请检查task配置中的: include=\"" + quickModel.getIncludeTables() + "\"是否正确!这是用来表名匹配的正则表达式!\n"
					+ "/*-2、请检查datasource配置,关注schema、catalog属性配置是否正确、或者大小写,其核心原理:conn.getMetaData().getTables(catalog, schema,*, TABLE);\n"
					+ "/* -------友情提示:datasoure中是可以包含: schema=''和 catalog='' 属性的，请灵活应用! ----------- \n"
					+ "/*---------------------------------------------------*/");
			return;
		}
		logger.info("当前任务共取出:" + tables.size() + " 张表或视图!");
		QuickVO quickVO;
		String entityName;
		String voPackageDir = "";
		String entityDir = "";
		String tableName = null;
		TableMeta tableMeta;
		List pks = null;
		if (quickModel.isHasVO()) {
			voPackageDir = quickModel.getVoPath() + File.separator
					+ StringUtil.replaceAllStr(quickModel.getVoPackage(), ".", File.separator);
			// 创建vo包文件
			if (quickModel.isHasAbstractVO()) {
				FileUtil.createFolder(
						FileUtil.formatPath(voPackageDir + File.separator + configModel.getAbstractPath()));
			} else {
				FileUtil.createFolder(FileUtil.formatPath(voPackageDir));
			}
		}
		if (quickModel.isHasEntity()) {
			entityDir = quickModel.getEntityPath() + File.separator
					+ StringUtil.replaceAllStr(quickModel.getEntityPackage(), ".", File.separator);
			// 创建abstract entity包文件
			if (quickModel.isHasAbstractEntity()) {
				FileUtil.createFolder(FileUtil.formatPath(entityDir + File.separator + configModel.getAbstractPath()));
			} else {
				FileUtil.createFolder(FileUtil.formatPath(entityDir));
			}
		}

		// 表或视图的标志
		boolean isTable;
		BusinessIdConfig businessIdConfig;
		boolean includeSchema = Constants.includeSchema();
		boolean hasApiDoc = quickModel.getApiDoc().equalsIgnoreCase("custom");
		boolean toUpperCase = false;
		for (int i = 0; i < tables.size(); i++) {
			tableMeta = (TableMeta) tables.get(i);
			tableName = tableMeta.getTableName();
			if (tableMeta.getTableType().equals("VIEW")) {
				logger.info("正在处理视图:" + tableName);
			} else {
				logger.info("正在处理表:" + tableName);
			}
			businessIdConfig = configModel.getBusinessId(tableName);
			quickVO = new QuickVO();
			quickVO.setSelectFields(selectFields);
			quickVO.setVoExtends(quickModel.getVoExtends());
			quickVO.setEntityExtends(quickModel.getEntityExtends());
			// 匹配表主键产生策略，主键策略通过配置文件进行附加定义
			PrimaryKeyStrategy primaryKeyStrategy = getPrimaryKeyStrategy(configModel.getPkGeneratorStrategy(),
					tableName);
			entityName = StringUtil.toHumpStr(tableName, true, true);
			quickVO.setApiDoc(quickModel.getApiDoc());
			quickVO.setReturnSelf(supportLinkSet);
			quickVO.setAbstractPath(configModel.getAbstractPath());
			quickVO.setVersion(Constants.getPropertyValue("project.version"));
			quickVO.setProjectName(Constants.getPropertyValue("project.name"));
			quickVO.setAuthor(quickModel.getAuthor());
			quickVO.setDateTime(CommonUtils.formatDate(CommonUtils.getNowTime(), "yyyy-MM-dd HH:mm:ss"));
			quickVO.setTableName(tableName);
			quickVO.setType(tableMeta.getTableType());
			if (includeSchema) {
				quickVO.setSchema(tableMeta.getSchema());
			}
			// 针对sqlserver
			if (StringUtil.isBlank(tableMeta.getTableRemark())) {
				quickVO.setTableRemark(DBHelper.getTableRemark(tableName));
			} else {
				quickVO.setTableRemark(tableMeta.getTableRemark());
			}
			quickVO.setEntityPackage(quickModel.getEntityPackage());
			// 截取VO前面的模块标识名称(一般数据库表名前缀为特定的模块名称)
			quickVO.setEntityName(StringUtil.firstToUpperCase(StringUtil.replaceStr(quickModel.getEntityName(),
					"#{subName}", StringUtil.subStart(entityName, quickModel.getEntitySubstr()))));
			quickVO.setVoPackage(quickModel.getVoPackage());
			// 截取VO前面的模块标识名称(一般数据库表名前缀为特定的模块名称)
			quickVO.setVoName(StringUtil.firstToUpperCase(StringUtil.replaceStr(quickModel.getVoName(), "#{subName}",
					StringUtil.subStart(entityName, quickModel.getVoSubstr()))));
			isTable = true;
			// 判断是"表还是视图"
			if (quickVO.getType().equals("VIEW")) {
				isTable = false;
			}
			List tableCols = DBHelper.getTableColumnMeta(tableName, toUpperCase);
			// oracle 存在schema大小写问题，通过转大写进行重试
			if ((tableCols == null || tableCols.isEmpty()) && (dbType == DBType.ORACLE || dbType == DBType.ORACLE11)
					&& toUpperCase == false) {
				tableCols = DBHelper.getTableColumnMeta(tableName, true);
				if (tableCols != null && tableCols.size() > 0) {
					toUpperCase = true;
				}
			}
			// vo中需要import的数据类型
			List impList = new ArrayList();
			List<QuickColMeta> colList = processTableCols(configModel, quickModel, tableName, tableCols,
					isTable ? DBHelper.getTableImpForeignKeys(tableName) : null, impList,
					quickModel.getFieldRidPrefix(), dbType, dialect, hasApiDoc);
			List exportKeys = DBHelper.getTableExportKeys(tableName);
			// 处理主键被其它表作为外键关联
			processExportTables(quickVO, exportKeys, quickModel);
			// 判断表字段是否全部为非空约束
			int notNullCnt = judgeFullNotNull(colList);
			// 默认设置为运行部分列为空
			quickVO.setFullNotNull("0");
			// 全部为空
			if (notNullCnt == colList.size()) {
				quickVO.setFullNotNull("1");
			}
			// 表
			if (isTable) {
				pks = DBHelper.getTablePrimaryKeys(tableName);
				// 主键字段长度等于表字段长度，设置所有字段为主键标志为1
				if (pks.size() == colList.size()) {
					quickVO.setPkIsAllColumn("1");
				}
				if (pks != null && notNullCnt == pks.size()) {
					quickVO.setPkSizeEqualNotNullSize("1");
				}
				// 单主键
				if (pks.size() == 1) {
					quickVO.setSinglePk("1");
				}
				// 无主键
				if (pks == null || pks.size() == 0) {
					quickVO.setSinglePk("-1");
					logger.info("======表" + tableName + "无主键!请检查数据库配置是否正确!");
				} else {
					// 设置主键约束配置,对postgresql 有意义
					if (!skipPkConstraint) {
						quickVO.setPkConstraint(DBHelper.getTablePKConstraint(tableName));
					}
					String pkCol;
					QuickColMeta quickColMeta;
					List pkList = new ArrayList();
					for (int m = 0; m < colList.size(); m++) {
						quickColMeta = (QuickColMeta) colList.get(m);
						// 判断是否是业务主键字段
						if (businessIdConfig != null) {
							if (quickColMeta.getColName().replaceAll("\\_|\\-", "")
									.equalsIgnoreCase(businessIdConfig.getColumn().replaceAll("\\_|\\-", ""))) {
								quickColMeta.setBusinessIdConfig(businessIdConfig);
								quickVO.setHasBusinessId(true);
								break;
							}
						}
					}
					for (int y = 0; y < pks.size(); y++) {
						pkCol = (String) pks.get(y);
						for (int m = 0; m < colList.size(); m++) {
							quickColMeta = (QuickColMeta) colList.get(m);
							// 是主键
							if (pkCol.equalsIgnoreCase(quickColMeta.getColName())) {
								quickColMeta.setPkFlag("1");
								int pksSize = pks.size();
								boolean isIdentity = (pksSize == 1
										&& quickColMeta.getAutoIncrement().equalsIgnoreCase("true")) ? true : false;
								String strategy;
								String sequence;
								String generator;
								if (pksSize == 1 && primaryKeyStrategy != null) {
									strategy = primaryKeyStrategy.getStrategy();
									if (!primaryKeyStrategy.isForce() && isIdentity) {
										quickColMeta.setStrategy("identity");
									} else {
										sequence = primaryKeyStrategy.getSequence();
										generator = primaryKeyStrategy.getGenerator();
										if (strategy.equalsIgnoreCase("assign")
												|| strategy.equalsIgnoreCase("generator")
												|| strategy.equalsIgnoreCase("identity")
												|| strategy.equalsIgnoreCase("sequence")) {
											quickColMeta.setStrategy(strategy);
											if (strategy.equalsIgnoreCase("sequence")) {
												if (StringUtil.isBlank(sequence)) {
													throw new Exception("please give a sequence for" + tableName
															+ " where primary key strategy is sequence!");
												}
												// 支持sequence命名以seq_${tableName} 跟表名有规则相关模式
												quickColMeta.setSequence(sequence
														.replaceFirst("(?i)\\$?\\{\\s*tableName\\s*\\}", tableName));
											}
											if (strategy.equalsIgnoreCase("generator")) {
												if (StringUtil.isNotBlank(generator)) {
													quickColMeta.setGenerator(generator);
												}
												// 设置default generator
												if (StringUtil.isBlank(generator)
														|| generator.equalsIgnoreCase("default")) {
													quickColMeta.setGenerator(Constants.PK_DEFAULT_GENERATOR);
												}
												// uuid
												else if (generator.equalsIgnoreCase("UUID")) {
													quickColMeta.setGenerator(Constants.PK_UUID_GENERATOR);
												}
												// 雪花算法
												else if (generator.equalsIgnoreCase("snowflake")) {
													quickColMeta.setGenerator(Constants.PK_SNOWFLAKE_GENERATOR);
												}
												// 纳秒
												else if (generator.equalsIgnoreCase("nanotime")) {
													quickColMeta.setGenerator(Constants.PK_NANOTIME_ID_GENERATOR);
												}
												// 基于redis的主键策略
												else if (generator.equalsIgnoreCase("redis")) {
													quickColMeta.setGenerator(Constants.PK_REDIS_ID_GENERATOR);
												}
											}
										} else {
											throw new Exception("please check primaryKey Strategy for table of "
													+ tableName + ",must like:sequence、assign、generator、identity");
										}
									}
								} else if (isIdentity) {
									quickColMeta.setStrategy("identity");
								} else if (pksSize == 1) {
									if ("varchar".equalsIgnoreCase(quickColMeta.getDataType())
											|| "char".equalsIgnoreCase(quickColMeta.getDataType())) {
										// 16位默认为雪花算法
										if (quickColMeta.getPrecision() >= 16) {
											quickColMeta.setStrategy("generator");
											quickColMeta.setGenerator(Constants.PK_SNOWFLAKE_GENERATOR);
										}
										// 22位纳秒算法
										if (quickColMeta.getPrecision() >= 22) {
											quickColMeta.setStrategy("generator");
											quickColMeta.setGenerator(Constants.PK_DEFAULT_GENERATOR);
										}
										// 26位纳秒算法
										if (quickColMeta.getPrecision() >= 26) {
											quickColMeta.setStrategy("generator");
											quickColMeta.setGenerator(Constants.PK_NANOTIME_ID_GENERATOR);
										}
									} else if ("long".equalsIgnoreCase(quickColMeta.getDataType())
											|| "integer".equalsIgnoreCase(quickColMeta.getDataType())
											|| "decimal".equalsIgnoreCase(quickColMeta.getDataType())
											|| "number".equalsIgnoreCase(quickColMeta.getDataType())
											|| "NUMERIC".equalsIgnoreCase(quickColMeta.getDataType())
											|| "BIGINT".equalsIgnoreCase(quickColMeta.getDataType())
											|| "BIGINTEGER".equalsIgnoreCase(quickColMeta.getDataType())
											|| "BIGDECIMAL".equalsIgnoreCase(quickColMeta.getDataType())) {
										if (quickColMeta.getPrecision() >= 16) {
											quickColMeta.setStrategy("generator");
											quickColMeta.setGenerator(Constants.PK_SNOWFLAKE_GENERATOR);
										}
										if (quickColMeta.getPrecision() >= 22) {
											quickColMeta.setStrategy("generator");
											quickColMeta.setGenerator(Constants.PK_DEFAULT_GENERATOR);
										}
										if (quickColMeta.getPrecision() >= 26) {
											quickColMeta.setStrategy("generator");
											quickColMeta.setGenerator(Constants.PK_NANOTIME_ID_GENERATOR);
										}
									}
								}
								// 当主键是雪花算法、默认的22位、26位数字类型，将java类型改成BigInteger类型
								String generate = (quickColMeta.getGenerator() == null) ? ""
										: quickColMeta.getGenerator();
								String resultType = (quickColMeta.getResultType() == null) ? ""
										: quickColMeta.getResultType().toLowerCase();
								if (generate.equals(Constants.PK_SNOWFLAKE_GENERATOR)
										|| generate.equals(Constants.PK_DEFAULT_GENERATOR)
										|| generate.equals(Constants.PK_NANOTIME_ID_GENERATOR)
												&& (resultType.equals("int") || resultType.equals("integer")
														|| resultType.equals("short") || resultType.equals("long"))) {
									quickColMeta.setResultType("BigInteger");
									if (!impList.contains("java.math.BigInteger")) {
										impList.add("java.math.BigInteger");
									}
								}
								pkList.add(quickColMeta);
								break;
							}
						}
					}
					if (pkList.size() > 1) {
						quickVO.setPkList(pkList);
						quickVO.setSinglePk("0");
					}
				}
			}

			// 针对数据库表中字段存在reciveTime 和recive_time 这种形态优化
			Set<String> fieldNames = new HashSet<String>();
			for (QuickColMeta colMeta : colList) {
				if (!colMeta.getColName().contains("_")) {
					fieldNames.add(colMeta.getColName().toLowerCase());
				}
			}
			for (QuickColMeta colMeta : colList) {
				if (colMeta.getColName().contains("_")
						&& fieldNames.contains(colMeta.getColName().replace("_", "").toLowerCase())) {
					colMeta.setColJavaName(StringUtil.toHumpStr(colMeta.getColName(), true, false));
				}
			}
			quickVO.setColumns(colList);
			if (colList != null) {
				quickVO.setColumnSize(colList.size());
			}
			// 加入自定义api-doc依赖的类
			if (hasApiDoc) {
				if (configModel.getDocApiImports() != null) {
					quickVO.setApiDocImports(configModel.getDocApiImports());
				}
				if (configModel.getDocApiClassTemplate() != null) {
					quickVO.setApiDocContent(FreemarkerUtil.getInstance().create(
							new String[] { "className", "tableName", "tableRemark" },
							new Object[] { quickVO.getEntityName(), quickVO.getTableName(), quickVO.getTableRemark() },
							configModel.getDocApiClassTemplate()));
				}
			}
			if (impList.contains("java.util.List")) {
				quickVO.setHasListType(true);
			}
			quickVO.setImports(impList);
			if (quickModel.isHasEntity() && quickModel.isHasVO()) {
				quickVO.setHasVoEntity(true);
			}
			String docClassTemplate = configModel.getDocApiClassTemplate();
			boolean hasClassApiDoc = (hasApiDoc && docClassTemplate != null);
			String[] apiDocNames = { "className", "tableName", "tableRemark" };
			// 创建entity文件
			if (quickModel.isHasEntity()) {
				quickVO.setLombok(quickModel.isEntityLombok());
				quickVO.setLombokChain(quickModel.isEntityLombokChain());
				if (quickModel.isHasAbstractEntity()) {
					if (hasClassApiDoc) {
						quickVO.setApiDocContent(
								FreemarkerUtil.getInstance()
										.create(apiDocNames,
												new Object[] { "Abstract" + quickVO.getEntityName(),
														quickVO.getTableName(), quickVO.getTableRemark() },
												docClassTemplate));
					}
					// 创建abstract entity文件
					generateAbstractEntity(
							entityDir + File.separator + configModel.getAbstractPath() + File.separator + "Abstract"
									+ quickVO.getEntityName() + ".java",
							entityAbstractTemplate, quickVO, configModel.getEncoding());
					if (hasClassApiDoc) {
						quickVO.setApiDocContent(
								FreemarkerUtil
										.getInstance().create(
												apiDocNames, new Object[] { quickVO.getEntityName(),
														quickVO.getTableName(), quickVO.getTableRemark() },
												docClassTemplate));
					}
					// 创建entity 文件
					generateParentEntity(entityDir + File.separator + quickVO.getEntityName() + ".java", quickVO,
							configModel.getEncoding());
				} else {
					if (hasClassApiDoc) {
						quickVO.setApiDocContent(
								FreemarkerUtil
										.getInstance().create(
												apiDocNames, new Object[] { quickVO.getEntityName(),
														quickVO.getTableName(), quickVO.getTableRemark() },
												docClassTemplate));
					}
					if (quickModel.isEntityLombok()) {
						generateLombokEntity(entityDir + File.separator + quickVO.getEntityName() + ".java",
								entityLombokTemplate, quickVO, configModel.getEncoding());
					} else {
						generateEntity(entityDir + File.separator + quickVO.getEntityName() + ".java", entityTemplate,
								quickVO, configModel.getEncoding());
					}
				}
			}
			// 创建DTO 文件
			if (quickModel.isHasVO()) {
				quickVO.setLombok(quickModel.isLombok());
				quickVO.setLombokChain(quickModel.isLombokChain());
				if (quickModel.isHasAbstractVO()) {
					if (hasClassApiDoc) {
						quickVO.setApiDocContent(
								FreemarkerUtil
										.getInstance().create(
												apiDocNames, new Object[] { "Abstract" + quickVO.getVoName(),
														quickVO.getTableName(), quickVO.getTableRemark() },
												docClassTemplate));
					}
					// 创建抽象dto
					generateAbstractDTO(voPackageDir + File.separator + configModel.getAbstractPath() + File.separator
							+ "Abstract" + quickVO.getVoName() + ".java", quickVO, configModel.getEncoding());
					if (hasClassApiDoc) {
						quickVO.setApiDocContent(FreemarkerUtil.getInstance().create(apiDocNames,
								new Object[] { quickVO.getVoName(), quickVO.getTableName(), quickVO.getTableRemark() },
								docClassTemplate));
					}
					generateParentDTO(voPackageDir + File.separator + quickVO.getVoName() + ".java", quickVO,
							configModel.getEncoding());
				} else {
					if (hasClassApiDoc) {
						quickVO.setApiDocContent(FreemarkerUtil.getInstance().create(apiDocNames,
								new Object[] { quickVO.getVoName(), quickVO.getTableName(), quickVO.getTableRemark() },
								docClassTemplate));
					}
					generateDTO(voPackageDir + File.separator + quickVO.getVoName() + ".java", quickVO,
							configModel.getEncoding());
				}
			}

		}
	}

	/**
	 * @todo 处理表的列信息
	 * @param configModel
	 * @param quickModel
	 * @param tableName
	 * @param cols
	 * @param fks
	 * @param impList
	 * @param ridPrefix
	 * @param dbType
	 * @param dialect
	 * @param hasApiDoc
	 * @return
	 * @throws Exception
	 */
	private static List processTableCols(ConfigModel configModel, QuickModel quickModel, String tableName, List cols,
			List fks, List impList, String ridPrefix, int dbType, String dialect, boolean hasApiDoc) throws Exception {
		List quickColMetas = new ArrayList();
		TableColumnMeta colMeta;
		String sqlType = "";
		ColumnTypeMapping colTypeMapping = null;
		TableConstractModel constractModel = null;
		String importType;
		int precision;
		int scale;
		int maxScale = Constants.getMaxScale();
		int typeMappSize = 0;

		if (configModel.getTypeMapping() != null && !configModel.getTypeMapping().isEmpty()) {
			typeMappSize = configModel.getTypeMapping().size();
		}
		Set<String> colsSet = new HashSet<String>();
		String tableField;
		boolean hasPartitionKey = false;
		String apiDoc;

		for (int i = 0; i < cols.size(); i++) {
			colMeta = (TableColumnMeta) cols.get(i);
			tableField = tableName.concat(".").concat(colMeta.getColName()).toLowerCase();
			QuickColMeta quickColMeta = new QuickColMeta();
			quickColMeta.setColRemark(colMeta.getColRemark());
			// 判断是否存在重复字段
			if (colsSet.contains(colMeta.getColName().toLowerCase())) {
				Constants.hasRepeatField = true;
			} else {
				colsSet.add(colMeta.getColName().toLowerCase());
			}
			// update 2020-4-8 剔除掉UNSIGNED对类型的干扰
			String jdbcType = colMeta.getTypeName().replaceFirst("(?i)\\sUNSIGNED", "");
			if (colMeta.getTypeName().indexOf(".") != -1) {
				jdbcType = colMeta.getTypeName().substring(colMeta.getTypeName().lastIndexOf(".") + 1);
			}
			// sqlserver 和sybase、sybase iq数据库identity主键类别包含identity字符
			jdbcType = jdbcType.replaceFirst("(?i)\\s*identity", "").trim();
			// 原始数据类型输出在vo字段上,便于开发者调整
			quickColMeta.setColType(jdbcType);
			// 提取原始类型
			sqlType = jdbcType.toLowerCase();
			jdbcType = Constants.getJdbcType(jdbcType, dbType);
			quickColMeta.setDataType(jdbcType);
			quickColMeta.setColName(colMeta.getColName());
			if (colMeta.getPartitionKey()) {
				quickColMeta.setPartitionKey(colMeta.getPartitionKey());
				hasPartitionKey = true;
			}
			quickColMeta.setAutoIncrement(Boolean.toString(colMeta.isAutoIncrement()));
			// 剔除字段统一前缀
			if (StringUtil.isNotBlank(ridPrefix) && colMeta.getColName().toLowerCase().startsWith(ridPrefix)) {
				quickColMeta.setColJavaName(
						StringUtil.toHumpStr(colMeta.getColName().substring(ridPrefix.length()), true, true));
			} else {
				quickColMeta.setColJavaName(StringUtil.toHumpStr(colMeta.getColName(), true, true));
			}
			quickColMeta.setJdbcType(jdbcType);
			quickColMeta.setPrecision(colMeta.getPrecision());
			quickColMeta.setScale(colMeta.getScale());
			quickColMeta.setDefaultValue(colMeta.getColDefault());
			// 避免部分数据库整数类型，默认值小数位后面还有好几个0,如：1.000000
			if (quickColMeta.getDefaultValue() != null && CommonUtils.isNumber(quickColMeta.getDefaultValue())) {
				if (colMeta.getLength() == colMeta.getPrecision()) {
					quickColMeta
							.setDefaultValue(Long.toString(Double.valueOf(quickColMeta.getDefaultValue()).longValue()));
				}
			}
			quickColMeta.setNullable(colMeta.isNullable() ? "1" : "0");
			importType = null;
			// 默认数据类型进行匹配
			String[] jdbcTypeMap;
			String[][] jdbcTypeMapping = Constants.getJdbcTypeMapping(dbType);
			for (int k = 0; k < jdbcTypeMapping.length; k++) {
				jdbcTypeMap = jdbcTypeMapping[k];
				if (sqlType.equalsIgnoreCase(jdbcTypeMap[0])) {
					// 针对一些数据库要求提供数据库类型和数据类型双重判断
					if ((jdbcTypeMap.length == 4 && dialect.equals(jdbcTypeMap[3])) || jdbcTypeMap.length == 3) {
						quickColMeta.setResultType(jdbcTypeMap[1]);
						// vo中需要import的类
						if (StringUtil.isNotBlank(jdbcTypeMap[2])) {
							importType = jdbcTypeMap[2];
						}
						break;
					}
				}
			}

			// 额外数据类型匹配,匹配以最后一个设置为准
			if (typeMappSize > 0) {
				precision = colMeta.getPrecision();
				scale = colMeta.getScale();
				if (maxScale != -1 && scale > maxScale) {
					scale = maxScale;
				}
				// 逆向进行匹配
				for (int j = typeMappSize - 1; j >= 0; j--) {
					colTypeMapping = (ColumnTypeMapping) configModel.getTypeMapping().get(j);
					if (StringUtil.isBlank(colTypeMapping.getTableField()) || (colTypeMapping.getTableField() != null
							&& colTypeMapping.getTableField().equals(tableField))) {
						// 类型一致(小写)
						if (colTypeMapping.getNativeTypes().containsKey(sqlType)) {
							boolean mapped = false;
							// 不判断长度
							if (colTypeMapping.getPrecisionMax() == -1 && colTypeMapping.getScaleMax() == -1) {
								if (null != colTypeMapping.getJdbcType()) {
									quickColMeta.setDataType(colTypeMapping.getJdbcType());
								}
								quickColMeta.setResultType(colTypeMapping.getResultType());
								mapped = true;
							}

							// 判断小数
							if (colTypeMapping.getPrecisionMax() == -1 && colTypeMapping.getScaleMax() != -1) {
								if (colTypeMapping.getScaleMax() >= scale && colTypeMapping.getScaleMin() <= scale) {
									if (null != colTypeMapping.getJdbcType()) {
										quickColMeta.setDataType(colTypeMapping.getJdbcType());
									}
									quickColMeta.setResultType(colTypeMapping.getResultType());
									mapped = true;
								}
							}

							// 判断长度
							if (colTypeMapping.getScaleMax() == -1 && colTypeMapping.getPrecisionMax() != -1) {
								if (colTypeMapping.getPrecisionMax() >= precision
										&& colTypeMapping.getPrecisionMin() <= precision) {
									if (null != colTypeMapping.getJdbcType()) {
										quickColMeta.setDataType(colTypeMapping.getJdbcType());
									}
									quickColMeta.setResultType(colTypeMapping.getResultType());
									mapped = true;
								}
							}

							// 判断长度和小数位
							if (colTypeMapping.getScaleMax() != -1 && colTypeMapping.getPrecisionMax() != -1) {
								// 长度跟整数位相等表示没有小数
								if (colTypeMapping.getPrecisionMax() >= precision
										&& colTypeMapping.getPrecisionMin() <= precision
										&& colTypeMapping.getScaleMax() >= scale
										&& colTypeMapping.getScaleMin() <= scale) {
									if (null != colTypeMapping.getJdbcType()) {
										quickColMeta.setDataType(colTypeMapping.getJdbcType());
									}
									quickColMeta.setResultType(colTypeMapping.getResultType());
									mapped = true;
								}
							}
							// 类型匹配
							if (mapped) {
								// 规避数组类型
								importType = colTypeMapping.getJavaType().replaceAll("\\[", "").replaceAll("\\]", "")
										.trim();
								break;
							}
						}
					}
				}
			}

			// 存在外键，则设置对应外键表和对应字段
			if (fks != null && !fks.isEmpty()) {
				// HashMap fkTables = new HashMap();
				for (int x = 0; x < fks.size(); x++) {
					constractModel = (TableConstractModel) fks.get(x);
					// 外键
					if (colMeta.getColName().equalsIgnoreCase(constractModel.getFkColName())) {
						quickColMeta.setFkRefJavaTableName(
								StringUtil.toHumpStr(constractModel.getFkRefTableName(), true, true));
						if (StringUtil.isNotBlank(ridPrefix)
								&& constractModel.getPkColName().toLowerCase().startsWith(ridPrefix)) {
							quickColMeta.setFkRefTableColJavaName(StringUtil.toHumpStr(
									constractModel.getPkColName().substring(ridPrefix.length()), true, true));
						} else {
							quickColMeta.setFkRefTableColJavaName(
									StringUtil.toHumpStr(constractModel.getPkColName(), true, true));
						}
						break;
					}
				}
			}

			// 默认数据类型都是非原始类型
			quickColMeta.setColTypeFlag("0");
			if (quickColMeta.getResultType() == null) {
				logger.info("字段:[" + colMeta.getColName() + "]数据类型:[" + colMeta.getTypeName() + "]数据长度:["
						+ colMeta.getPrecision() + "]小数位:[" + colMeta.getScale() + "]没有设置对应的java-type!");
				logger.info(
						"请在quickvo.xml 正确配置<sql-type native-types=\"" + colMeta.getTypeName() + "\" java-type=\"\" />");
			} else {
				for (int m = 0; m < Constants.prototype.length; m++) {
					// 是否原始类型
					if (quickColMeta.getResultType().equals(Constants.prototype[m][0])) {
						quickColMeta.setColTypeFlag(Constants.prototype[m][1]);
						break;
					}
				}
			}
			// 增加类引入类型对象
			if (importType != null) {
				if (importType.indexOf(".") != -1 && !impList.contains(importType)) {
					if (StringUtil.isBlank(colTypeMapping.getImportTypes())) {
						impList.add(importType);
					}
				}
				if (importType.toLowerCase().startsWith("list<") && !impList.contains("java.util.List")) {
					impList.add("java.util.List");
				}
			}
			if (colTypeMapping != null && StringUtil.isNotBlank(colTypeMapping.getImportTypes())) {
				String[] imports = colTypeMapping.getImportTypes().split("\\,");
				for (String imp : imports) {
					if (!impList.contains(imp.trim())) {
						impList.add(imp.trim());
					}
				}
			}

			if (hasApiDoc && StringUtil.isNotBlank(configModel.getDocApiFieldTemplate())) {
				// @Schema(name="${colName}",description="${colRemark}",nullable=${nullable},type="${filedType}")
				apiDoc = FreemarkerUtil.getInstance().create(
						new String[] { "colName", "colRemark", "nullable", "fieldType", "fieldName", "defaultValue" },
						new Object[] { quickColMeta.getColName(), quickColMeta.getColRemark(),
								colMeta.isNullable() ? "true" : "false", quickColMeta.getResultType(),
								StringUtil.firstToLowerCase(quickColMeta.getColJavaName()),
								quickColMeta.getDefaultValue() },
						configModel.getDocApiFieldTemplate());
				quickColMeta.setApiDocContent(apiDoc);
			}
			if (isSkipField(quickModel.getSkipEntityExtendsFields(), quickColMeta.getColJavaName().toLowerCase())) {
				quickColMeta.setSkipEntity(true);
			}
			if (isSkipField(quickModel.getSkipVOExtendsFields(), quickColMeta.getColJavaName().toLowerCase())) {
				quickColMeta.setSkipVO(true);
			}
			quickColMetas.add(quickColMeta);
		}
		if (hasPartitionKey) {
			impList.add("org.sagacity.sqltoy.config.annotation.PartitionKey");
		}
		return quickColMetas;
	}

	private static boolean isSkipField(String[] skipFields, String fieldName) {
		if (skipFields == null || skipFields.length == 0) {
			return false;
		}
		for (String skipField : skipFields) {
			if (skipField.equals(fieldName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @todo 判断字段是否全不为null,是返回1，可以有null返回0
	 * @param columns
	 * @return
	 */
	private static int judgeFullNotNull(List columns) {
		QuickColMeta quickColMeta;
		// 不为空的数量
		int notNullCnt = 0;
		for (int i = 0; i < columns.size(); i++) {
			quickColMeta = (QuickColMeta) columns.get(i);
			// 不为空
			if (!quickColMeta.getNullable().equalsIgnoreCase("1")) {
				notNullCnt++;
			}
		}
		return notNullCnt;
	}

	/**
	 * @todo 处理主键被其它表作为外键关联
	 * @param quickVO
	 * @param exportKeys
	 * @param quickModel
	 */
	private static void processExportTables(QuickVO quickVO, List<TableConstractModel> exportKeys,
			QuickModel quickModel) {
		// 设置被关联的表
		if (exportKeys != null && !exportKeys.isEmpty()) {
			List<CascadeModel> cascadeModels;
			HashMap<String, TableConstractModel> subTablesMap = new HashMap<String, TableConstractModel>();
			TableConstractModel subTable;
			String refTable;
			String pkColJavaName;
			String pkRefColJavaName;
			String refJavaTable;
			String fieldPrefix = quickModel.getFieldRidPrefix();
			for (TableConstractModel exportKey : exportKeys) {
				refTable = exportKey.getPkRefTableName();
				refJavaTable = StringUtil.toHumpStr(refTable, true, true);
				if (StringUtil.isNotBlank(fieldPrefix)
						&& exportKey.getPkColName().toLowerCase().startsWith(fieldPrefix)) {
					pkColJavaName = StringUtil.toHumpStr(exportKey.getPkColName().substring(fieldPrefix.length()),
							false, true);
				} else {
					pkColJavaName = StringUtil.toHumpStr(exportKey.getPkColName(), false, true);
				}
				if (StringUtil.isNotBlank(fieldPrefix)
						&& exportKey.getPkRefColName().toLowerCase().startsWith(fieldPrefix)) {
					pkRefColJavaName = StringUtil.toHumpStr(exportKey.getPkRefColName().substring(fieldPrefix.length()),
							false, true);
				} else {
					pkRefColJavaName = StringUtil.toHumpStr(exportKey.getPkRefColName(), false, true);
				}
				if (subTablesMap.containsKey(refTable)) {
					subTable = subTablesMap.get(refTable);
					subTable.setPkColName(subTable.getPkColName() + ",\"" + exportKey.getPkColName() + "\"");
					subTable.setPkColJavaName(subTable.getPkColJavaName() + ",\"" + pkColJavaName + "\"");
					subTable.setPkRefColName(subTable.getPkRefColName() + ",\"" + exportKey.getPkRefColName() + "\"");
					subTable.setPkRefColJavaName(subTable.getPkRefColJavaName() + ",\"" + pkRefColJavaName + "\"");
					subTable.setPkEqualsFkStr(subTable.getPkEqualsFkStr().concat("&& main.get")
							.concat(StringUtil.firstToUpperCase(pkColJavaName)).concat("().equals(item.get")
							.concat(StringUtil.firstToUpperCase(pkRefColJavaName)).concat("())"));
				} else {
					subTablesMap.put(refTable, exportKey);
					if (quickModel.isHasEntity()) {
						refJavaTable = StringUtil.firstToUpperCase(StringUtil.replaceStr(quickModel.getEntityName(),
								"#{subName}", StringUtil.subStart(refJavaTable, quickModel.getEntitySubstr())));
					} else {
						refJavaTable = StringUtil.firstToUpperCase(StringUtil.replaceStr(quickModel.getVoName(),
								"#{subName}", StringUtil.subStart(refJavaTable, quickModel.getVoSubstr())));
					}
					exportKey.setPkRefTableJavaName(refJavaTable);

					exportKey.setPkColName("\"" + exportKey.getPkColName() + "\"");
					exportKey.setPkColJavaName("\"" + pkColJavaName + "\"");
					exportKey.setPkRefColName("\"" + exportKey.getPkRefColName() + "\"");
					exportKey.setPkRefColJavaName("\"" + pkRefColJavaName + "\"");
					exportKey.setPkEqualsFkStr(
							"main.get".concat(StringUtil.firstToUpperCase(pkColJavaName)).concat("().equals(item.get")
									.concat(StringUtil.firstToUpperCase(pkRefColJavaName)).concat("())"));
					cascadeModels = configModel.getCascadeConfig();
					if (cascadeModels != null) {
						for (CascadeModel cascadeModel : cascadeModels) {
							if (refTable.equalsIgnoreCase(cascadeModel.getTableName())) {
								exportKey.setLoad(cascadeModel.getLoad());
								exportKey.setAutoSave(cascadeModel.isSave() ? 1 : 0);
								exportKey.setCascade(cascadeModel.isDelete() ? 1 : 0);
								exportKey.setUpdateSql(cascadeModel.getUpdateSql());
								exportKey.setOrderBy(cascadeModel.getOrderBy());
							}
						}
					} else {
						exportKey.setCascade(1);
					}
				}
			}
			quickVO.setExportTables(new ArrayList(subTablesMap.values()));
		}
	}

	private static void generateAbstractDTO(String file, QuickVO quickVO, String charset) throws Exception {
		// 根据包名和类名称产生hash值
		String hashStr = quickVO.getVoPackage().concat(".").concat(quickVO.getAbstractPath()).concat(".Abstract")
				.concat(quickVO.getVoName());
		quickVO.setVoAbstractSerialUID(Long.toString(StringUtil.hash(hashStr)));
		boolean needGen = true;
		File voFile = new File(file);
		// 文件不存在
		if (voFile.exists()) {
			String oldFileContent = FileUtil.readAsString(voFile, charset);
			String newFileContent = FreemarkerUtil.getInstance().create(new String[] { "quickVO" },
					new Object[] { quickVO }, dtoAbstractTemplate);
			// 剔除所有回车换行和空白
			oldFileContent = StringUtil.clearMistyChars(oldFileContent, "");
			oldFileContent = StringUtil.replaceAllStr(oldFileContent, " ", "");

			newFileContent = StringUtil.clearMistyChars(newFileContent, "");
			newFileContent = StringUtil.replaceAllStr(newFileContent, " ", "");
			// 内容相等
			if (oldFileContent.equals(newFileContent)) {
				needGen = false;
			}
		}
		// 需要产生
		if (needGen) {
			logger.info("正在生成文件:" + file);
			FreemarkerUtil.getInstance().create(new String[] { "quickVO" }, new Object[] { quickVO },
					dtoAbstractTemplate, file);
		}
	}

	private static void generateParentDTO(String file, QuickVO quickVO, String charset) throws Exception {
		// 根据包名和类名称产生hash值
		String hashStr = quickVO.getVoPackage().concat(".").concat(quickVO.getVoName());
		quickVO.setVoSerialUID(Long.toString(StringUtil.hash(hashStr)));
		File voFile = new File(file);
		// 文件不存在
		if (!voFile.exists()) {
			logger.info("正在生成文件:" + file);
			FreemarkerUtil.getInstance().create(new String[] { "quickVO" }, new Object[] { quickVO }, dtoParentTemplate,
					file);
		}
	}

	private static void generateDTO(String file, QuickVO quickVO, String charset) throws Exception {
		// 根据包名和类名称产生hash值
		String hashStr = quickVO.getVoPackage() + "." + quickVO.getVoName();
		quickVO.setVoSerialUID(Long.toString(StringUtil.hash(hashStr)));
		File voFile = new File(file);
		// 文件存在判断是否相等，不相等则生成
		if (voFile.exists()) {
			String oldFileContent = FileUtil.readAsString(voFile, charset);
			String newFileContent = FreemarkerUtil.getInstance().create(new String[] { "quickVO" },
					new Object[] { quickVO }, dtoTemplate);
			ChangeModel changeResult = mergeResult(newFileContent, oldFileContent, true);
			// 文件已经修改
			if (changeResult.isModified()) {
				logger.info("数据库发生变更:正在更新文件:" + file);
				FileUtil.putStringToFile(changeResult.getResult(), file, charset);
			}
			return;
		} else {
			logger.info("正在生成文件:" + file);
			FreemarkerUtil.getInstance().create(new String[] { "quickVO" }, new Object[] { quickVO }, dtoTemplate,
					file);
		}
	}

	private static void generateEntity(String file, String template, QuickVO quickVO, String charset) throws Exception {
		File generateFile = new File(file);
		boolean needGen = true;
		// 根据包名和类名称产生hash值
		String hashStr = quickVO.getEntityPackage() + "." + quickVO.getEntityName();
		quickVO.setEntitySerialUID(Long.toString(StringUtil.hash(hashStr)));
		// 文件存在判断是否相等，不相等则生成
		if (generateFile.exists()) {
			String oldFileContent = FileUtil.readAsString(generateFile, charset);
			String newFileContent = FreemarkerUtil.getInstance().create(new String[] { "quickVO" },
					new Object[] { quickVO }, template);
			// 剔除所有回车换行和空白
			oldFileContent = StringUtil.clearMistyChars(oldFileContent, "");
			oldFileContent = StringUtil.replaceAllStr(oldFileContent, " ", "");

			newFileContent = StringUtil.clearMistyChars(newFileContent, "");
			newFileContent = StringUtil.replaceAllStr(newFileContent, " ", "");

			// 内容相等
			if (oldFileContent.equals(newFileContent)) {
				needGen = false;
			}
		}
		// 需要产生
		if (needGen) {
			logger.info("正在生成文件:" + file);
			FreemarkerUtil.getInstance().create(new String[] { "quickVO" }, new Object[] { quickVO }, template, file);
		}
	}

	private static void generateLombokEntity(String file, String template, QuickVO quickVO, String charset)
			throws Exception {
		File generateFile = new File(file);
		// 根据包名和类名称产生hash值
		String hashStr = quickVO.getEntityPackage().concat(".").concat(quickVO.getEntityName());
		quickVO.setEntitySerialUID(Long.toString(StringUtil.hash(hashStr)));
		// 文件存在判断是否相等，不相等则生成
		if (generateFile.exists()) {
			String oldFileContent = FileUtil.readAsString(generateFile, charset);
			String newFileContent = FreemarkerUtil.getInstance().create(new String[] { "quickVO" },
					new Object[] { quickVO }, template);
			ChangeModel changeResult = mergeResult(newFileContent, oldFileContent, true);
			// 文件已经修改
			if (changeResult.isModified()) {
				logger.info("数据库发生变更:正在更新文件:" + file);
				FileUtil.putStringToFile(changeResult.getResult(), file, charset);
			}
			return;
		} else {
			logger.info("正在生成文件:" + file);
			FreemarkerUtil.getInstance().create(new String[] { "quickVO" }, new Object[] { quickVO }, template, file);
		}
	}

	private static void generateAbstractEntity(String file, String template, QuickVO quickVO, String charset)
			throws Exception {
		File generateFile = new File(file);
		boolean needGen = true;
		// 根据包名和类名称产生hash值
		String hashStr = quickVO.getEntityPackage().concat(".").concat(quickVO.getAbstractPath()).concat(".Abstract")
				.concat(quickVO.getEntityName());
		quickVO.setAbstractEntitySerialUID(Long.toString(StringUtil.hash(hashStr)));
		// 文件存在判断是否相等，不相等则生成
		if (generateFile.exists()) {
			String oldFileContent = FileUtil.readAsString(generateFile, charset);
			String newFileContent = FreemarkerUtil.getInstance().create(new String[] { "quickVO" },
					new Object[] { quickVO }, template);
			// 剔除所有回车换行和空白
			oldFileContent = StringUtil.clearMistyChars(oldFileContent, "");
			oldFileContent = StringUtil.replaceAllStr(oldFileContent, " ", "");

			newFileContent = StringUtil.clearMistyChars(newFileContent, "");
			newFileContent = StringUtil.replaceAllStr(newFileContent, " ", "");

			// 内容相等
			if (oldFileContent.equals(newFileContent)) {
				needGen = false;
			}
		}
		// 需要产生
		if (needGen) {
			logger.info("正在生成文件:" + file);
			FreemarkerUtil.getInstance().create(new String[] { "quickVO" }, new Object[] { quickVO }, template, file);
		}
	}

	private static void generateParentEntity(String file, QuickVO quickVO, String charset) throws Exception {
		// 根据包名和类名称产生hash值
		String hashStr = quickVO.getEntityPackage().concat(".").concat(quickVO.getEntityName());
		quickVO.setEntitySerialUID(Long.toString(StringUtil.hash(hashStr)));
		File entityFile = new File(file);
		// 文件存在判断是否相等，不相等则生成
		if (entityFile.exists()) {
			String oldFileContent = FileUtil.readAsString(entityFile, charset);
			String newFileContent = FreemarkerUtil.getInstance().create(new String[] { "quickVO" },
					new Object[] { quickVO }, entityParentTemplate);
			ChangeModel changeResult = mergeResult(newFileContent, oldFileContent, false);
			// 文件已经修改
			if (changeResult.isModified()) {
				logger.info("数据库发生变更:正在更新文件:" + file);
				FileUtil.putStringToFile(changeResult.getResult(), file, charset);
			}
			return;
		} else {
			logger.info("正在生成文件:" + file);
			FreemarkerUtil.getInstance().create(new String[] { "quickVO" }, new Object[] { quickVO },
					entityParentTemplate, file);
		}
	}

	private static ChangeModel mergeResult(String newContent, String oldContent, boolean isFields) throws Exception {
		ChangeModel result = new ChangeModel();
		String start = isFields ? Constants.fieldsBegin : Constants.constructorBegin;
		String end = isFields ? Constants.fieldsEnd : Constants.constructorEnd;
		int oldBegin = oldContent.indexOf(start);
		int oldEnd = oldContent.indexOf(end);
		int newBegin = newContent.indexOf(start);
		int newEnd = newContent.indexOf(end);
		if (oldBegin == -1 || oldEnd == -1 || newBegin == -1 || newEnd == -1) {
			throw new Exception("文件内容中不存在规定的开始和截止区域标记:\n" + start + "\n" + end);
		}
		String newTableGenStr = newContent.substring(newBegin, newEnd + end.length());
		String oldTableGenStr = oldContent.substring(oldBegin, oldEnd + end.length());
		// 内容相同，表示表结构没有发生变化
		if (newTableGenStr.replaceAll("\\s|\t|\n|\r", "").equals(oldTableGenStr.replaceAll("\\s|\t|\n|\r", ""))) {
			return result;
		}
		result.setResult(oldContent.substring(0, oldBegin).concat(newTableGenStr)
				.concat(oldContent.substring(oldEnd + end.length())));
		result.setModified(true);
		return result;
	}

	/**
	 * @todo 获取指定配置的主键策略
	 * @param pkGeneratorStrategyList
	 * @param tableName
	 * @return
	 */
	private static PrimaryKeyStrategy getPrimaryKeyStrategy(List<PrimaryKeyStrategy> pkGeneratorStrategyList,
			String tableName) {
		PrimaryKeyStrategy result = null;
		// 以最后的为准
		for (PrimaryKeyStrategy primaryKeyStrategy : pkGeneratorStrategyList) {
			// 不区分大小写
			if (tableName.matches("(?i)".concat(primaryKeyStrategy.getName()))) {
				result = primaryKeyStrategy;
			}
		}
		return result;
	}
}
