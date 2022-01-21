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
import java.util.logging.Logger;

import org.sagacity.quickvo.Constants;
import org.sagacity.quickvo.model.DataSourceModel;
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
				logger.info("数据库驱动未能加载，请在/libs 目录下放入正确的数据库驱动jar包!");
				throw cnfe;
			} catch (SQLException se) {
				logger.info("获取数据库连接失败!");
				throw se;
			}
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
				logger.info("表:user_tab_comments 不存在,如当前非oracle数据库(如:polardb等),此错误请忽略!");
			}
		} // mysql数据库
		if (dbType == DBType.MYSQL && !isPolardb) {
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
				logger.info("表:INFORMATION_SCHEMA.TABLES 不存在,如当前非mysql数据库(如:polardb、dorisdb等),此错误请忽略!");
			}
		}
		if (!skipGetTables) {
			// 获取当前数据库的全部表名信息
			rs = conn.getMetaData().getTables(catalog, schema, null, types);
		}
		return (List) DBUtil.preparedStatementProcess(commentName, pst, rs, new PreparedStatementResultHandler() {
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
						// tableMeta.setSchema(rs.getString("TABLE_SCHEM"));
						// tableMeta.setSchema(rs.getString("TABLE_CAT"));
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
	 * @return
	 * @throws Exception
	 */
	public static List getTableColumnMeta(final String tableName) throws Exception {
		final int dbType = DBUtil.getDbType(conn);
		PreparedStatement pst = null;
		ResultSet rs;
		HashMap filedsComments = null;
		boolean isPolardb = dbConfig.getUrl().toLowerCase().contains("polardb");
		// sybase or sqlserver
		if (dbType == DBType.SQLSERVER && !isPolardb) {
			if (dbType == DBType.SQLSERVER) {
				StringBuilder queryStr = new StringBuilder();
				queryStr.append("SELECT a.name COLUMN_NAME,");
				queryStr.append(" cast(isnull(g.[value],'') as varchar(1000)) as COMMENTS");
				queryStr.append(" FROM syscolumns a");
				queryStr.append(" inner join sysobjects d on a.id=d.id ");
				queryStr.append(" and d.xtype='U' and d.name<>'dtproperties'");
				queryStr.append(" left join syscomments e");
				queryStr.append(" on a.cdefault=e.id");
				queryStr.append(" left join sys.extended_properties g");
				queryStr.append(" on a.id=g.major_id AND a.colid = g.minor_id");
				queryStr.append(" where d.name=?");
				queryStr.append(" order by a.id,a.colorder");
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
									colMeta.setColRemark(rs.getString("COMMENTS"));
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
				StringBuilder queryStr = new StringBuilder();
				queryStr.append("SELECT t1.*,t2.DATA_DEFAULT FROM (SELECT COLUMN_NAME,COMMENTS");
				queryStr.append("  FROM user_col_comments");
				queryStr.append("  WHERE table_name =?) t1");
				queryStr.append("  LEFT JOIN(SELECT COLUMN_NAME,DATA_DEFAULT");
				queryStr.append("            FROM user_tab_cols");
				queryStr.append("            WHERE table_name =?) t2");
				queryStr.append("  on t1.COLUMN_NAME=t2.COLUMN_NAME");
				pst = conn.prepareStatement(queryStr.toString());
				pst.setString(1, tableName);
				pst.setString(2, tableName);
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
									filedHash.put(rs.getString("COLUMN_NAME"), colMeta);
								}
								this.setResult(filedHash);
							}
						});
			}
		} catch (Exception e) {
			logger.info("如果当前数据库非oracle(如polardb)，请忽视错误信息:" + e.getMessage());
		}
		// clickhouse 数据库
		if (dbType == DBType.CLICKHOUSE && !isPolardb) {
			StringBuilder queryStr = new StringBuilder();
			queryStr.append(
					"select name COLUMN_NAME,comment COMMENTS,is_in_primary_key PRIMARY_KEY,is_in_partition_key PARTITION_KEY from system.columns t where t.table=?");
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
			rs = conn.getMetaData().getColumns(catalog, schema, tableName, null);
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
						if (colMeta != null && colMeta.getColDefault() == null) {
							colMeta.setColDefault(clearDefaultValue(rs.getString("COLUMN_DEF")));
						}
					}
					if (colMeta != null) {
						colMeta.setDataType(rs.getInt("DATA_TYPE"));
						colMeta.setTypeName(rs.getString("TYPE_NAME"));
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
		// 针对postgresql
		if (result.indexOf("(") != -1 && result.indexOf(")") != -1 && result.indexOf("::") != -1) {
			result = result.substring(result.indexOf("(") + 1, result.indexOf("::"));
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
}
