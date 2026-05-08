/**
 * 
 */
package org.sagacity.quickvo.utils;

import static java.lang.System.err;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.sagacity.quickvo.Constants;
import org.sagacity.quickvo.model.DataSourceModel;
import org.sagacity.quickvo.model.IndexModel;
import org.sagacity.quickvo.model.TableColumnMeta;
import org.sagacity.quickvo.model.TableConstractModel;
import org.sagacity.quickvo.model.TableMeta;
import org.sagacity.quickvo.utils.DBUtil.DBType;
import org.sagacity.quickvo.utils.callback.PreparedStatementResultHandler;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @project sagacity-quickvo
 * @description quickvo数据库解析
 * @author zhongxuchen
 * @version v1.0,Date:2010-7-12
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class DBHelper {
	/**
	 * 定义全局日志
	 */
	private static Logger logger = LoggerUtil.getLogger();

	/**
	 * 数据库连接
	 */
	private static Connection conn;

	private static DataSourceModel dbConfig = null;

	private static HashMap<String, DataSourceModel> dbMaps = new HashMap<String, DataSourceModel>();

	/**
	 * @todo 加载数据库配置
	 * @param datasouceElts
	 * @throws Exception
	 */
	public static void loadDatasource(NodeList datasouceElts) throws Exception {
		if (datasouceElts == null || datasouceElts.getLength() == 0) {
			logger.info("没有配置相应的数据库");
			throw new Exception("没有配置相应的数据库");
		}
		Element datasouceElt;
		for (int m = 0; m < datasouceElts.getLength(); m++) {
			datasouceElt = (Element) datasouceElts.item(m);
			DataSourceModel dbModel = new DataSourceModel();
			String name = null;
			if (datasouceElt.hasAttribute("name")) {
				name = datasouceElt.getAttribute("name");
			}
			if (datasouceElt.hasAttribute("catalog")) {
				dbModel.setCatalog(Constants.replaceConstants(datasouceElt.getAttribute("catalog")));
			}
			if (datasouceElt.hasAttribute("schema")) {
				dbModel.setSchema(Constants.replaceConstants(datasouceElt.getAttribute("schema")));
			}
			dbModel.setUrl(Constants.replaceConstants(datasouceElt.getAttribute("url")));
			dbModel.setDriver(Constants.replaceConstants(datasouceElt.getAttribute("driver")));
			dbModel.setUsername(Constants.replaceConstants(datasouceElt.getAttribute("username")));
			dbModel.setPassword(Constants.replaceConstants(datasouceElt.getAttribute("password")));
			dbMaps.put(StringUtil.isBlank(name) ? ("" + m) : name, dbModel);
		}
	}

	/**
	 * @todo 获取数据库连接
	 * @param dbName
	 * @return
	 * @throws Exception
	 */
	public static boolean getConnection(String dbName) throws Exception {
		dbConfig = dbMaps.get(StringUtil.isBlank(dbName) ? "0" : dbName);
		if (dbConfig == null && dbMaps.size() == 1 && StringUtil.isBlank(dbName)) {
			dbConfig = dbMaps.values().iterator().next();
		}
		if (dbConfig != null) {
			logger.info("开始连接数据库:" + dbName + ",url:" + dbConfig.getUrl());
			try {
				Class.forName(dbConfig.getDriver());
				conn = DriverManager.getConnection(dbConfig.getUrl(), dbConfig.getUsername(), dbConfig.getPassword());
				return true;
			} catch (ClassNotFoundException cnfe) {
				cnfe.printStackTrace();
				logger.info("数据库驱动未能加载，请在/libs 目录下放入正确的数据库驱动jar包,或请再参照文档了解quickvo-maven插件的配置!");
				throw cnfe;
			} catch (SQLException se) {
				logger.info("获取数据库连接失败!");
				throw se;
			}
		} else {
			logger.info("数据库名称:" + dbName + "不在dataSource定义的名单中,请检查<task datasource=\"" + dbName
					+ "\" 跟<datasource name=\"定义值\"> 的一致性");
		}
		return false;
	}

	/**
	 * @todo 关闭数据库并销毁
	 */
	public static void close() {
		try {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @todo 获取符合条件的表和视图
	 * @param includes
	 * @param excludes
	 * @return
	 * @throws Exception
	 */
	public static List getTableAndView(final String[] includes, final String[] excludes) throws Exception {
		int dbType = DBUtil.getDbType(conn);
		String schema = dbConfig.getSchema();
		String catalog = dbConfig.getCatalog();
		logger.info("提取数据库:schema=[" + schema + "]和 catalog=[" + catalog + "]");
		String[] types = new String[] { "TABLE", "VIEW" };
		PreparedStatement pst = null;
		ResultSet rs = null;
		// 数据库表注释，默认为remarks，不同数据库其名称不一样
		String commentName = "REMARKS";
		boolean isPolardb = dbConfig.getUrl().toLowerCase().contains("polardb");
		boolean skipGetTables = false;
		// oracle数据库
		if ((dbType == DBType.ORACLE || dbType == DBType.ORACLE11) && !isPolardb) {
			try {
				pst = conn.prepareStatement("select * from user_tab_comments");
				rs = pst.executeQuery();
				commentName = "COMMENTS";
				skipGetTables = true;
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("表:user_tab_comments 不存在,如当前非oracle数据库(如:polardb等),此错误请忽略!");
			}
		} // mysql数据库
		if ((dbType == DBType.MYSQL || dbType == DBType.MYSQL57) && !isPolardb) {
			try {
				StringBuilder queryStr = new StringBuilder("SELECT TABLE_NAME,TABLE_SCHEMA,TABLE_TYPE,TABLE_COMMENT ");
				queryStr.append(" FROM INFORMATION_SCHEMA.TABLES where 1=1 ");
				if (schema != null) {
					queryStr.append(" and TABLE_SCHEMA='").append(schema).append("'");
				} else if (catalog != null) {
					queryStr.append(" and TABLE_SCHEMA='").append(catalog).append("'");
				}
				if (types != null) {
					queryStr.append(" and (");
					for (int i = 0; i < types.length; i++) {
						if (i > 0) {
							queryStr.append(" or ");
						}
						queryStr.append(" TABLE_TYPE like '%").append(types[i]).append("'");
					}
					queryStr.append(")");
				}
				pst = conn.prepareStatement(queryStr.toString());
				rs = pst.executeQuery();
				commentName = "TABLE_COMMENT";
				skipGetTables = true;
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("表:INFORMATION_SCHEMA.TABLES 不存在,如当前非mysql数据库(如:polardb、dorisdb等),此错误请忽略!");
			}
		}
		if (dbType == DBType.POSTGRESQL || dbType == DBType.POSTGRESQL15 || dbType == DBType.GAUSSDB
				|| dbType == DBType.STARDB || dbType == DBType.VASTBASE || dbType == DBType.OPENGAUSS
				|| dbType == DBType.OSCAR) {
			try {
				String sql = "SELECT   c.relname AS TABLE_NAME,CASE c.relkind  "
						+ "        WHEN 'r' THEN 'TABLE'  WHEN 'p' THEN 'TABLE' "
						+ "        WHEN 'v' THEN 'VIEW'   END AS TABLE_TYPE, d.description AS COMMENTS "
						+ "    FROM pg_class c "
						+ "		    LEFT JOIN pg_description d ON d.objoid = c.oid AND d.objsubid = 0 "
						+ "         JOIN pg_namespace n ON n.oid = c.relnamespace "
						+ "    WHERE c.relkind IN ('r', 'v', 'p') "
						+ "        AND c.oid NOT IN (SELECT inhrelid FROM pg_inherits) AND c.relname NOT LIKE 'pg_%' "
						+ "        AND n.nspname NOT LIKE 'pg_%' "
						+ "        AND n.nspname = ANY (current_schemas(false)) ";
				pst = conn.prepareStatement(sql);
				rs = pst.executeQuery();
				commentName = "COMMENTS";
				skipGetTables = true;
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("表:pg_class 不存在,如当前非postgresql以及延申数据库,此错误请忽略!");
			}
		}
		if (!skipGetTables) {
			// 获取当前数据库的全部表名信息
			rs = conn.getMetaData().getTables(catalog, schema, null, types);
		}
		List tableList = (List) DBUtil.preparedStatementProcess(commentName, pst, rs,
				new PreparedStatementResultHandler() {
					public void execute(Object obj, PreparedStatement pst, ResultSet rs) throws Exception {
						List tables = new ArrayList();
						String tableName;
						// 是否包含标识，通过正则表达是判断是否是需要获取的表
						boolean is_include = false;
						String type;
						while (rs.next()) {
							is_include = false;
							tableName = rs.getString("TABLE_NAME");
							if (includes != null && includes.length > 0) {
								for (int i = 0; i < includes.length; i++) {
									if (StringUtil.matches(tableName, includes[i])) {
										is_include = true;
										break;
									}
								}
							} else {
								is_include = true;
							}
							if (excludes != null && excludes.length > 0) {
								for (int j = 0; j < excludes.length; j++) {
									if (StringUtil.matches(tableName, excludes[j])) {
										is_include = false;
										break;
									}
								}
							}
							if (is_include) {
								TableMeta tableMeta = new TableMeta();
								tableMeta.setTableName(tableName);
								tableMeta.setSchema(dbConfig.getSchema());
								type = rs.getString("TABLE_TYPE").toLowerCase();
								if (type.contains("view")) {
									tableMeta.setTableType("VIEW");
								} else {
									tableMeta.setTableType("TABLE");
								}
								tableMeta.setTableRemark(StringUtil.clearMistyChars(rs.getString(obj.toString()), " "));
								tables.add(tableMeta);
							}
						}
						this.setResult(tables);
					}
				});
		return tableList;
	}

	/**
	 * @todo 获取表名的注释
	 * @param tableName
	 * @return
	 * @throws Exception
	 */
	public static String getTableRemark(String tableName) throws Exception {
		final int dbType = DBUtil.getDbType(conn);
		PreparedStatement pst = null;
		ResultSet rs;
		// sqlserver
		String tableComment = null;
		if (dbType == DBType.SQLSERVER) {
			StringBuilder queryStr = new StringBuilder();
			queryStr.append("select cast(isnull(f.value,'') as varchar(1000)) COMMENTS");
			queryStr.append(" from syscolumns a");
			queryStr.append(" inner join sysobjects d on a.id=d.id and d.xtype='U' and d.name<>'dtproperties'");
			queryStr.append(" left join sys.extended_properties f on d.id=f.major_id and f.minor_id=0");
			queryStr.append(" where a.colorder=1 and d.name=?");
			pst = conn.prepareStatement(queryStr.toString());
			pst.setString(1, tableName);
			rs = pst.executeQuery();
			tableComment = (String) DBUtil.preparedStatementProcess(null, pst, rs,
					new PreparedStatementResultHandler() {
						public void execute(Object obj, PreparedStatement pst, ResultSet rs) throws SQLException {
							while (rs.next()) {
								this.setResult(rs.getString("COMMENTS"));
							}
						}
					});
		}
		return tableComment;
	}

	/**
	 * @todo 获取表的字段信息
	 * @param tableName
	 * @param toUpperCase
	 * @return
	 * @throws Exception
	 */
	public static List getTableColumnMeta(final String tableName, boolean toUpperCase) throws Exception {
		final int dbType = DBUtil.getDbType(conn);
		PreparedStatement pst = null;
		ResultSet rs;
		HashMap filedsComments = null;
		boolean isPolardb = dbConfig.getUrl().toLowerCase().contains("polardb");
		// sybase or sqlserver
		if (dbType == DBType.SQLSERVER && !isPolardb) {
			if (dbType == DBType.SQLSERVER) {
				StringBuilder sql = new StringBuilder();
				sql.append("SELECT ");
				sql.append("   c.name AS COLUMN_NAME, ");
				sql.append("	  ISNULL(ep.value, '') AS COMMENTS, ");
				sql.append("	  CASE ");
				sql.append("	    WHEN c.is_computed = 0 THEN 0 ");
				sql.append("	    WHEN cc.is_persisted = 1 THEN 2 ");
				sql.append("		   ELSE 1 ");
				sql.append("	  END AS GENERATED_TYPE, ");
				sql.append("  CASE WHEN EXISTS ( ");
				sql.append("     SELECT 1 FROM sys.index_columns ic ");
				sql.append("     WHERE ic.object_id = c.object_id ");
				sql.append("       AND ic.column_id = c.column_id ");
				sql.append("       AND ic.partition_ordinal > 0 ");
				sql.append("   ) THEN 1 ELSE 0 END AS IS_PARTITION_KEY ");
				sql.append(" FROM sys.columns c ");
				sql.append("	    LEFT JOIN sys.extended_properties ep ");
				sql.append("	         ON ep.major_id = c.object_id ");
				sql.append("	         AND ep.minor_id = c.column_id ");
				sql.append("         AND ep.name = 'MS_Description' ");
				sql.append("	    LEFT JOIN sys.computed_columns cc ");
				sql.append("         ON c.object_id = cc.object_id ");
				sql.append("         AND c.column_id = cc.column_id ");
				sql.append("	    LEFT JOIN sys.default_constraints dc ");
				sql.append("			  ON c.default_object_id = dc.object_id ");
				sql.append(" WHERE OBJECT_NAME(c.object_id) =? ");
				sql.append("	 ORDER BY c.column_id ");
				pst = conn.prepareStatement(sql.toString());
				pst.setString(1, tableName);
				rs = pst.executeQuery();
				filedsComments = (HashMap) DBUtil.preparedStatementProcess(null, pst, rs,
						new PreparedStatementResultHandler() {
							public void execute(Object obj, PreparedStatement pst, ResultSet rs) throws SQLException {
								HashMap filedHash = new HashMap();
								while (rs.next()) {
									TableColumnMeta colMeta = new TableColumnMeta();
									colMeta.setColName(rs.getString("COLUMN_NAME"));
									colMeta.setColRemark(rs.getString("COMMENTS"));
									if (rs.getInt("IS_PARTITION_KEY") == 1) {
										colMeta.setPartitionKey(true);
									}
									colMeta.setGeneratedType(rs.getInt("GENERATED_TYPE"));
									filedHash.put(rs.getString("COLUMN_NAME"), colMeta);
								}
								this.setResult(filedHash);
							}
						});
			}
			String queryStr = "{call sp_columns ('" + tableName + "')}";
			pst = conn.prepareCall(queryStr);
			rs = pst.executeQuery();
			final HashMap metaMap = filedsComments;
			return (List) DBUtil.preparedStatementProcess(null, null, rs, new PreparedStatementResultHandler() {

				public void execute(Object obj, PreparedStatement pst, ResultSet rs) throws SQLException {
					List result = new ArrayList();
					String isAutoIncrement;
					String colName;
					while (rs.next()) {
						TableColumnMeta colMeta;
						colName = rs.getString("COLUMN_NAME");
						if (colName == null) {
							colName = rs.getString("column_name");
						}
						if (dbType == DBType.SQLSERVER) {
							if (metaMap == null) {
								colMeta = new TableColumnMeta();
								colMeta.setColName(colName);
								colMeta.setColRemark(rs.getString("REMARKS"));
							} else {
								colMeta = (TableColumnMeta) metaMap.get(colName);
							}
						} else {
							colMeta = new TableColumnMeta();
						}
						if (colMeta != null) {
							colMeta.setColDefault(clearDefaultValue(StringUtil.trim(rs.getString("column_def"))));
							colMeta.setDataType(rs.getInt("data_type"));
							colMeta.setTypeName(rs.getString("type_name"));
							if (rs.getInt("char_octet_length") != 0) {
								colMeta.setLength(rs.getInt("char_octet_length"));
							} else {
								colMeta.setLength(rs.getInt("precision"));
							}
							colMeta.setPrecision(colMeta.getLength());
							// 字段名称
							colMeta.setColName(colName);
							colMeta.setScale(rs.getInt("scale"));
							colMeta.setNumPrecRadix(rs.getInt("radix"));
							try {
								isAutoIncrement = rs.getString("IS_AUTOINCREMENT");
								if (isAutoIncrement != null && (isAutoIncrement.equalsIgnoreCase("true")
										|| isAutoIncrement.equalsIgnoreCase("YES")
										|| isAutoIncrement.equalsIgnoreCase("Y") || isAutoIncrement.equals("1"))) {
									colMeta.setAutoIncrement(true);
								} else {
									colMeta.setAutoIncrement(false);
								}
							} catch (Exception e) {
							}
							if (colMeta.getTypeName().toLowerCase().indexOf("identity") != -1) {
								colMeta.setAutoIncrement(true);
							}
							// 是否可以为空
							if (rs.getInt("nullable") == 1) {
								colMeta.setNullable(true);
							} else {
								colMeta.setNullable(false);
							}
							result.add(colMeta);
						} else {
							err.println("表:" + tableName + " 对应的列:" + colName + "不在当前用户表字段内,请检查schema或catalog配置是否正确!");
							logger.info("表:" + tableName + " 对应的列:" + colName + "不在当前用户表字段内,请检查schema或catalog配置是否正确!");
						}
					}
					this.setResult(result);
				}
			});
		}
		try {
			// oracle 数据库
			if ((dbType == DBType.ORACLE || dbType == DBType.ORACLE11) && !isPolardb) {
				StringBuilder sql = new StringBuilder();
				sql.append("		SELECT ");
				sql.append("		    t.COLUMN_NAME,");
				sql.append("		    t.COMMENTS,");
				sql.append("		    CASE WHEN c.VIRTUAL_COLUMN = 'YES' THEN 2 ELSE 0 END AS GENERATED_TYPE,");
				sql.append("		    CASE WHEN p.COLUMN_NAME IS NOT NULL THEN 1 ELSE 0 END AS IS_PARTITION_KEY,");
				sql.append("		    c.DATA_DEFAULT ");
				sql.append("		FROM ALL_COL_COMMENTS t ");
				sql.append("		JOIN ALL_TAB_COLS c ");
				sql.append("		    ON t.OWNER = c.OWNER ");
				sql.append("		    AND t.TABLE_NAME = c.TABLE_NAME ");
				sql.append("		    AND t.COLUMN_NAME = c.COLUMN_NAME ");
				sql.append("		LEFT JOIN ALL_PART_KEY_COLUMNS p ");
				sql.append("		    ON t.OWNER = p.OWNER ");
				sql.append("		    AND t.TABLE_NAME = p.NAME ");
				sql.append("		    AND t.COLUMN_NAME = p.COLUMN_NAME ");
				sql.append("		WHERE t.TABLE_NAME =? ");
				sql.append("		ORDER BY c.COLUMN_ID ");
				pst = conn.prepareStatement(sql.toString());
				pst.setString(1, tableName);
				rs = pst.executeQuery();
				filedsComments = (HashMap) DBUtil.preparedStatementProcess(null, pst, rs,
						new PreparedStatementResultHandler() {
							public void execute(Object obj, PreparedStatement pst, ResultSet rs) throws SQLException {
								HashMap filedHash = new HashMap();
								while (rs.next()) {
									TableColumnMeta colMeta = new TableColumnMeta();
									colMeta.setColName(rs.getString("COLUMN_NAME"));
									colMeta.setColRemark(StringUtil.clearMistyChars(rs.getString("COMMENTS"), " "));
									colMeta.setColDefault(StringUtil.trim(rs.getString("DATA_DEFAULT")));
									if (rs.getInt("IS_PARTITION_KEY") == 1) {
										colMeta.setPartitionKey(true);
									}
									colMeta.setGeneratedType(rs.getInt("GENERATED_TYPE"));
									filedHash.put(rs.getString("COLUMN_NAME"), colMeta);
								}
								this.setResult(filedHash);
							}
						});
			}
		} catch (Exception e) {
			logger.info("如果当前数据库非oracle(如polardb)，请忽视错误信息:" + e.getMessage());
		}

		try {
			// postgresql
			if (dbType == DBType.POSTGRESQL || dbType == DBType.POSTGRESQL15 || dbType == DBType.GAUSSDB
					|| dbType == DBType.STARDB || dbType == DBType.VASTBASE || dbType == DBType.OPENGAUSS
					|| dbType == DBType.OSCAR) {
				StringBuilder sql = new StringBuilder();
				sql.append("	 SELECT ");
				sql.append("	   a.attname AS COLUMN_NAME,");
				sql.append("	   d.description AS COMMENTS,");
				sql.append("	   CASE WHEN a.attgenerated = 's' THEN 2 ELSE 0 END AS GENERATED_TYPE,");
				sql.append("	   CASE WHEN p.partattrs IS NOT NULL AND a.attnum = ANY(p.partattrs) ");
				sql.append("        THEN 1 ELSE 0 END AS IS_PARTITION_KEY,");
				sql.append("	   pg_get_expr(ad.adbin, ad.adrelid) AS DATA_DEFAULT ");
				sql.append("	 FROM  pg_class c ");
				sql.append("		JOIN pg_namespace n ON c.relnamespace = n.oid ");
				sql.append("		JOIN pg_attribute a ON c.oid = a.attrelid ");
				sql.append("		LEFT JOIN pg_description d ON c.oid = d.objoid AND a.attnum = d.objsubid ");
				sql.append("		LEFT JOIN pg_attrdef ad ON c.oid = ad.adrelid AND a.attnum = ad.adnum ");
				sql.append("		LEFT JOIN pg_partitioned_table p ON c.oid = p.partrelid ");
				sql.append("	 WHERE c.relname = ? ");
				sql.append("		AND a.attnum > 0 ");
				sql.append("	    AND NOT a.attisdropped ");
				sql.append("	 ORDER BY a.attnum ");
				pst = conn.prepareStatement(sql.toString());
				pst.setString(1, tableName);
				rs = pst.executeQuery();
				filedsComments = (HashMap) DBUtil.preparedStatementProcess(null, pst, rs,
						new PreparedStatementResultHandler() {
							public void execute(Object obj, PreparedStatement pst, ResultSet rs) throws SQLException {
								HashMap filedHash = new HashMap();
								while (rs.next()) {
									TableColumnMeta colMeta = new TableColumnMeta();
									colMeta.setColName(rs.getString("COLUMN_NAME"));
									colMeta.setColRemark(StringUtil.clearMistyChars(rs.getString("COMMENTS"), " "));
									colMeta.setColDefault(StringUtil.trim(rs.getString("DATA_DEFAULT")));
									if (rs.getInt("IS_PARTITION_KEY") == 1) {
										colMeta.setPartitionKey(true);
									}
									colMeta.setGeneratedType(rs.getInt("GENERATED_TYPE"));
									filedHash.put(rs.getString("COLUMN_NAME"), colMeta);
								}
								this.setResult(filedHash);
							}
						});
			}
		} catch (Exception e) {
			logger.info("如果当前数据库非postgresql，请忽视错误信息:" + e.getMessage());
		}

		try {
			// mysql
			if (dbType == DBType.MYSQL || dbType == DBType.MYSQL57) {
				StringBuilder sql = new StringBuilder();
				sql.append("SELECT ");
				sql.append("   COLUMN_NAME,");
				sql.append("   COLUMN_DEFAULT AS DATA_DEFAULT,");
				sql.append("   COLUMN_COMMENT AS COMMENTS,");
				sql.append(
						"   CASE WHEN PARTITION_EXPRESSION IS NOT NULL AND INSTR(PARTITION_EXPRESSION, COLUMN_NAME) > 0 ");
				sql.append("      THEN 1 ");
				sql.append("      ELSE 0 ");
				sql.append("   END AS IS_PARTITION_KEY,");
				sql.append("   CASE WHEN EXTRA LIKE '%GENERATED%' THEN ");
				sql.append("     CASE WHEN EXTRA LIKE '%VIRTUAL%' THEN 1 ");
				sql.append("        WHEN EXTRA LIKE '%STORED%' THEN 2 ");
				sql.append("        ELSE 0 ");
				sql.append("     END ");
				sql.append("   ELSE 0 ");
				sql.append("   END AS GENERATED_TYPE ");
				sql.append(" FROM information_schema.COLUMNS c ");
				sql.append(" LEFT JOIN information_schema.PARTITIONS p ");
				sql.append("   ON c.TABLE_SCHEMA = p.TABLE_SCHEMA ");
				sql.append("   AND c.TABLE_NAME = p.TABLE_NAME ");
				sql.append(" WHERE c.TABLE_NAME =? ");
				sql.append(" GROUP BY c.TABLE_SCHEMA, c.TABLE_NAME, c.COLUMN_NAME, c.ORDINAL_POSITION ");
				sql.append(" ORDER BY c.ORDINAL_POSITION ");
				pst = conn.prepareStatement(sql.toString());
				pst.setString(1, tableName);
				rs = pst.executeQuery();
				filedsComments = (HashMap) DBUtil.preparedStatementProcess(null, pst, rs,
						new PreparedStatementResultHandler() {
							public void execute(Object obj, PreparedStatement pst, ResultSet rs) throws SQLException {
								HashMap filedHash = new HashMap();
								while (rs.next()) {
									TableColumnMeta colMeta = new TableColumnMeta();
									colMeta.setColName(rs.getString("COLUMN_NAME"));
									colMeta.setColRemark(StringUtil.clearMistyChars(rs.getString("COMMENTS"), " "));
									colMeta.setColDefault(StringUtil.trim(rs.getString("DATA_DEFAULT")));
									if (rs.getInt("IS_PARTITION_KEY") == 1) {
										colMeta.setPartitionKey(true);
									}
									colMeta.setGeneratedType(rs.getInt("GENERATED_TYPE"));
									filedHash.put(rs.getString("COLUMN_NAME"), colMeta);
								}
								this.setResult(filedHash);
							}
						});
			}
		} catch (Exception e) {
			logger.info("如果当前数据库非postgresql，请忽视错误信息:" + e.getMessage());
		}
		// clickhouse 数据库
		if (dbType == DBType.CLICKHOUSE && !isPolardb) {
			StringBuilder queryStr = new StringBuilder();
			queryStr.append(
					"select name COLUMN_NAME,comment COMMENTS,is_in_primary_key PRIMARY_KEY,is_in_partition_key PARTITION_KEY,type TYPE,default_expression DEFAULT_EXPRESSION from system.columns t where t.table=?");
			pst = conn.prepareStatement(queryStr.toString());
			pst.setString(1, tableName);
			rs = pst.executeQuery();
			filedsComments = (HashMap) DBUtil.preparedStatementProcess(null, pst, rs,
					new PreparedStatementResultHandler() {

						public void execute(Object obj, PreparedStatement pst, ResultSet rs) throws SQLException {
							HashMap filedHash = new HashMap();
							while (rs.next()) {
								TableColumnMeta colMeta = new TableColumnMeta();
								colMeta.setColName(rs.getString("COLUMN_NAME"));
								colMeta.setColRemark(StringUtil.clearMistyChars(rs.getString("COMMENTS"), " "));
								// 是否主键
								if (rs.getString("PRIMARY_KEY").equals("1")) {
									colMeta.setIsPrimaryKey(true);
								}
								if (rs.getString("PARTITION_KEY").equals("1")) {
									colMeta.setPartitionKey(true);
								}
								String typeName = rs.getString("TYPE");
								colMeta.setTypeName(typeName);
								colMeta.setColDefault(rs.getString("DEFAULT_EXPRESSION"));
								if (colMeta.getColDefault() != null && colMeta.getColDefault().equals("")) {
									if (typeName.toLowerCase().startsWith("nullable(")
											|| !colMeta.getTypeName().toLowerCase().equalsIgnoreCase("string")) {
										colMeta.setColDefault(null);
									}
								}
								filedHash.put(rs.getString("COLUMN_NAME"), colMeta);
							}
							this.setResult(filedHash);
						}
					});
		}
		final HashMap metaMap = filedsComments;
		String catalog = dbConfig.getCatalog();
		String schema = dbConfig.getSchema();
		// 获取具体表对应的列字段信息
		if ((dbType == DBType.MYSQL || dbType == DBType.MYSQL57) && !isPolardb) {
			rs = conn.getMetaData().getColumns(catalog, schema, tableName, "%");
		} else {
			// oracle 场景存在大小写问题
			if (toUpperCase) {
				rs = conn.getMetaData().getColumns((catalog == null) ? null : catalog.toUpperCase(),
						(schema == null) ? null : schema.toUpperCase(), tableName, null);
			} else {
				rs = conn.getMetaData().getColumns(catalog, schema, tableName, null);
			}
		}

		return (List) DBUtil.preparedStatementProcess(metaMap, null, rs, new PreparedStatementResultHandler() {
			public void execute(Object obj, PreparedStatement pst, ResultSet rs) throws SQLException {
				List result = new ArrayList();
				String isAutoIncrement;
				String colName;
				while (rs.next()) {
					TableColumnMeta colMeta;
					colName = rs.getString("COLUMN_NAME");
					if (metaMap == null) {
						colMeta = new TableColumnMeta();
						colMeta.setColName(colName);
						colMeta.setColDefault(clearDefaultValue(rs.getString("COLUMN_DEF")));
						colMeta.setColRemark(StringUtil.clearMistyChars(rs.getString("REMARKS"), " "));
					} else {
						colMeta = (TableColumnMeta) metaMap.get(colName);
						if (dbType != DBType.CLICKHOUSE) {
							if (colMeta != null && colMeta.getColDefault() == null) {
								colMeta.setColDefault(clearDefaultValue(rs.getString("COLUMN_DEF")));
							}
						}
					}
					if (colMeta != null) {
						colMeta.setDataType(rs.getInt("DATA_TYPE"));
						if (dbType != DBType.CLICKHOUSE) {
							colMeta.setTypeName(rs.getString("TYPE_NAME"));
						}
						colMeta.setLength(rs.getInt("COLUMN_SIZE"));
						colMeta.setPrecision(colMeta.getLength());
						colMeta.setScale(rs.getInt("DECIMAL_DIGITS"));
						colMeta.setNumPrecRadix(rs.getInt("NUM_PREC_RADIX"));
						try {
							isAutoIncrement = rs.getString("IS_AUTOINCREMENT");
							if (isAutoIncrement != null && (isAutoIncrement.equalsIgnoreCase("true")
									|| isAutoIncrement.equalsIgnoreCase("YES") || isAutoIncrement.equalsIgnoreCase("Y")
									|| isAutoIncrement.equals("1"))) {
								colMeta.setAutoIncrement(true);
							} else {
								colMeta.setAutoIncrement(false);
							}
						} catch (Exception e) {
						}
						if (dbType == DBType.ORACLE) {
							if (colMeta.getColDefault() != null
									&& colMeta.getColDefault().toLowerCase().endsWith(".nextval")) {
								colMeta.setAutoIncrement(true);
								colMeta.setColDefault(colMeta.getColDefault().replaceAll("\"", "\\\\\""));
							}
						}
						if (colMeta.getColDefault() != null && colMeta.getColDefault().equalsIgnoreCase("NULL")) {
							colMeta.setColDefault(null);
						}
						if (rs.getInt("NULLABLE") == 1) {
							colMeta.setNullable(true);
						} else {
							colMeta.setNullable(false);
						}
						result.add(colMeta);
					} else {
						err.println("表:" + tableName + " 对应的列:" + colName + "不在当前用户表字段内,请检查schema或catalog配置是否正确!");
						logger.info("表:" + tableName + " 对应的列:" + colName + "不在当前用户表字段内,请检查schema或catalog配置是否正确!");
					}
				}
				this.setResult(result);
			}

		});
	}

	/**
	 * @todo 处理sqlserver default值为((value))问题
	 * @param defaultValue
	 * @return
	 */
	private static String clearDefaultValue(String defaultValue) {
		if (defaultValue == null) {
			return null;
		}
		if (defaultValue.trim().equals("")) {
			return defaultValue;
		}
		String result = defaultValue;
		if (result.startsWith("NULL::")) {
			return null;
		}
		int brecketIndex = result.indexOf("(");
		// 针对postgresql
		if (brecketIndex != -1 && result.indexOf(")") != -1 && result.indexOf("::", brecketIndex) != -1) {
			result = result.substring(result.indexOf("(") + 1, result.indexOf("::", brecketIndex));
		}
		// postgresql
		if (result.indexOf("'") != -1 && result.indexOf("::") != -1) {
			result = result.substring(0, result.indexOf("::"));
		}
		if (result.startsWith("((") && result.endsWith("))")) {
			result = result.substring(2, result.length() - 2);
		}
		if (result.startsWith("(") && result.endsWith(")")) {
			result = result.substring(1, result.length() - 1);
		}
		if (result.startsWith("'") && result.endsWith("'")) {
			result = result.substring(1, result.length() - 1);
		}
		if (result.startsWith("\"") && result.endsWith("\"")) {
			result = result.substring(1, result.length() - 1);
		}
		return result.trim();
	}

	/**
	 * @todo <b>获取表的外键信息</b>
	 * @param tableName
	 * @return
	 * @throws SQLException
	 */
	public static List getTableImpForeignKeys(String tableName) {
		try {
			ResultSet rs = conn.getMetaData().getImportedKeys(dbConfig.getCatalog(), dbConfig.getSchema(), tableName);
			List result = (List) DBUtil.preparedStatementProcess(null, null, rs, new PreparedStatementResultHandler() {
				public void execute(Object obj, PreparedStatement pst, ResultSet rs) throws SQLException {
					List result = new ArrayList();
					while (rs.next()) {
						TableConstractModel constractModel = new TableConstractModel();
						constractModel.setFkName(rs.getString("FK_NAME"));
						constractModel.setFkRefTableName(rs.getString("PKTABLE_NAME"));
						constractModel.setFkColName(rs.getString("FKCOLUMN_NAME"));
						constractModel.setPkColName(rs.getString("PKCOLUMN_NAME"));
						constractModel.setUpdateRule(rs.getInt("UPDATE_RULE"));
						constractModel.setDeleteRule(rs.getInt("DELETE_RULE"));
						result.add(constractModel);
					}
					this.setResult(result);
				}
			});
			return result;
		} catch (Exception e) {
		}
		return new ArrayList();
	}

	/**
	 * @todo 获取表主键被其他表关联的信息(作为其它表的外键)
	 * @param tableName
	 * @return
	 * @throws Exception
	 */
	public static List<TableConstractModel> getTableExportKeys(String tableName) {
		try {
			ResultSet rs = conn.getMetaData().getExportedKeys(dbConfig.getCatalog(), dbConfig.getSchema(), tableName);
			List result = (List<TableConstractModel>) DBUtil.preparedStatementProcess(null, null, rs,
					new PreparedStatementResultHandler() {
						public void execute(Object obj, PreparedStatement pst, ResultSet rs) throws SQLException {
							List<TableConstractModel> result = new ArrayList<TableConstractModel>();
							while (rs.next()) {
								TableConstractModel constractModel = new TableConstractModel();
								constractModel.setPkRefTableName(rs.getString("FKTABLE_NAME"));
								constractModel.setPkColName(rs.getString("PKCOLUMN_NAME"));
								constractModel.setPkRefColName(rs.getString("FKCOLUMN_NAME"));
								constractModel.setUpdateRule(rs.getInt("UPDATE_RULE"));
								constractModel.setDeleteRule(rs.getInt("DELETE_RULE"));
								result.add(constractModel);
							}
							this.setResult(result);
						}
					});
			return result;
		} catch (Exception e) {
		}
		return new ArrayList();
	}

	/**
	 * @todo <b>获取表的主键信息</b>
	 * @param tableName
	 * @return
	 * @throws SQLException
	 */
	public static List getTablePrimaryKeys(String tableName) {
		int dbType = -1;
		try {
			dbType = DBUtil.getDbType(conn);
			boolean isPolardb = dbConfig.getUrl().toLowerCase().contains("polardb");
			ResultSet rs = null;
			List pkList = null;
			if (dbType == DBType.CLICKHOUSE) {
				rs = conn.createStatement()
						.executeQuery("select t.name COLUMN_NAME from system.columns t where t.table='" + tableName
								+ "' and t.is_in_primary_key=1");
			} else {
				try {
					rs = conn.getMetaData().getPrimaryKeys(dbConfig.getCatalog(), dbConfig.getSchema(), tableName);
				} catch (Exception e) {

				}
			}
			// 针对StarRocks场景
			if (rs == null && (dbType == DBType.MYSQL || dbType == DBType.MYSQL57) && !isPolardb) {
				rs = conn.createStatement().executeQuery("desc " + tableName);
				pkList = (List) DBUtil.preparedStatementProcess(null, null, rs, new PreparedStatementResultHandler() {
					public void execute(Object obj, PreparedStatement pst, ResultSet rs) throws SQLException {
						List result = new ArrayList();
						String field;
						while (rs.next()) {
							field = rs.getString("FIELD");
							if (rs.getBoolean("KEY")) {
								result.add(field);
							}
						}
						this.setResult(result);
					}
				});
			} else if (dbType == DBType.IMPALA) {
				rs = conn.createStatement().executeQuery("DESCRIBE " + tableName);
				pkList = (List) DBUtil.preparedStatementProcess(null, null, rs, new PreparedStatementResultHandler() {

					public void execute(Object obj, PreparedStatement pst, ResultSet rs) throws SQLException {
						List result = new ArrayList();
						String field;
						while (rs.next()) {
							field = rs.getString("NAME");
							if (rs.getBoolean("PRIMARY_KEY")) {
								result.add(field);
							}
						}
						this.setResult(result);
					}
				});
			} else {
				pkList = (List) DBUtil.preparedStatementProcess(null, null, rs, new PreparedStatementResultHandler() {
					public void execute(Object obj, PreparedStatement pst, ResultSet rs) throws SQLException {
						List result = new ArrayList();
						while (rs.next()) {
							result.add(rs.getString("COLUMN_NAME"));
						}
						this.setResult(result);
					}
				});
			}

			// 排除重复主键约束
			HashSet hashSet = new HashSet(pkList);
			return new ArrayList(hashSet);
		} catch (Exception e) {
			if (!(dbType == DBType.MYSQL || dbType == DBType.MYSQL57)) {
				e.printStackTrace();
			}
		}
		return new ArrayList();
	}

	/**
	 * @todo <b>获取表的主键约束名称</b>
	 * @param tableName
	 * @return
	 * @throws SQLException
	 */
	public static String getTablePKConstraint(String tableName) throws Exception {
		String pkName = null;
		int dbType = DBUtil.getDbType(conn);
		if (dbType == DBType.CLICKHOUSE || dbType == DBType.IMPALA) {
			return pkName;
		}
		try {
			ResultSet rs = conn.getMetaData().getPrimaryKeys(dbConfig.getCatalog(), dbConfig.getSchema(), tableName);
			pkName = (String) DBUtil.preparedStatementProcess(null, null, rs, new PreparedStatementResultHandler() {
				public void execute(Object obj, PreparedStatement pst, ResultSet rs) throws SQLException {
					rs.next();
					this.setResult(rs.getString("PK_NAME"));
				}
			});
		} catch (Exception e) {
			if (!(dbType == DBType.MYSQL || dbType == DBType.MYSQL57)) {
				e.printStackTrace();
			}
		}
		return pkName;
	}

	public static int getDBType() throws Exception {
		return DBUtil.getDbType(conn);
	}

	public static String getDBDialect() throws Exception {
		return DBUtil.getCurrentDBDialect(conn);
	}

	public static List<IndexModel> getIndexInfo(String tableName, String pkName) {
		List result = new ArrayList();
		List<IndexModel> uniqueIndexes = getIndexInfo(tableName, pkName, true);
		Set<String> indexNames = new HashSet<>();
		if (uniqueIndexes != null && !uniqueIndexes.isEmpty()) {
			for (IndexModel indexModel : uniqueIndexes) {
				if (!indexNames.contains(indexModel.getIndexName())) {
					indexModel.setIsUnique(true);
					result.add(indexModel);
					indexNames.add(indexModel.getIndexName());
				}
			}
		}
		List<IndexModel> otherIndexes = getIndexInfo(tableName, pkName, false);
		if (otherIndexes != null && !otherIndexes.isEmpty()) {
			for (IndexModel indexModel : otherIndexes) {
				if (!indexNames.contains(indexModel.getIndexName())) {
					indexModel.setIsUnique(false);
					result.add(indexModel);
					indexNames.add(indexModel.getIndexName());
				}
			}
		}
		return result;
	}

	public static List<IndexModel> getIndexInfo(String tableName, String pkName, boolean isUnique) {
		try {
			ResultSet rs = conn.getMetaData().getIndexInfo(dbConfig.getCatalog(), dbConfig.getSchema(), tableName,
					isUnique, true);
			List result = (List<IndexModel>) DBUtil.preparedStatementProcess(null, null, rs,
					new PreparedStatementResultHandler() {
						public void execute(Object obj, PreparedStatement pst, ResultSet rs) throws SQLException {
							List<IndexModel> result = new ArrayList<IndexModel>();
							Map<String, IndexModel> indexMap = new HashMap<>();
							String indexName;
							String columnName;
							String sortType;
							while (rs.next()) {
								indexName = rs.getString("INDEX_NAME");
								if ((indexName != null && !indexName.equalsIgnoreCase("PRIMARY")) && (pkName == null
										|| (pkName != null && !indexName.equalsIgnoreCase(pkName)))) {
									columnName = rs.getString("COLUMN_NAME");
									sortType = rs.getString("ASC_OR_DESC");
									if (sortType != null) {
										if (sortType.equalsIgnoreCase("A")) {
											sortType = "ASC";
										} else {
											sortType = "DESC";
										}
									} else {
										sortType = "";
									}
									IndexModel indexModel = indexMap.get(indexName);
									if (indexModel == null) {
										indexModel = new IndexModel();
										indexModel.setIndexName(indexName);
										indexModel.setSortTypes(new String[] { sortType });
										indexModel.setColumns(new String[] { columnName });
										indexModel.setIsUnique(rs.getBoolean("NON_UNIQUE"));
										indexModel.setTableName(tableName);
										result.add(indexModel);
									} else {
										int len = indexModel.getColumns().length;
										String[] columns = new String[len + 1];
										String[] sortTypes = new String[len + 1];
										System.arraycopy(indexModel.getColumns(), 0, columns, 0, len);
										System.arraycopy(indexModel.getSortTypes(), 0, sortTypes, 0, len);
										columns[len] = columnName;
										sortTypes[len] = sortType;
										indexModel.setColumns(columns);
										indexModel.setSortTypes(sortTypes);
									}
									indexMap.put(indexName, indexModel);
								}
							}
							this.setResult(result);
						}
					});
			return result;
		} catch (Exception e) {
		}
		return new ArrayList();
	}
}
