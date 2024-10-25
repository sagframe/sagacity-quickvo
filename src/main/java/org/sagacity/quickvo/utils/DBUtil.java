/**
 * @Copyright 2008 版权归陈仁飞，不要肆意侵权抄袭，如引用请注明出处保留作者信息。
 */
package org.sagacity.quickvo.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Logger;

import org.sagacity.quickvo.utils.callback.PreparedStatementResultHandler;

/**
 * @project sagacity-quickvo
 * @description 数据库连接工具类
 * @author zhongxuchen
 * @version v1.0,Date:2008-11-24
 */
public class DBUtil {
	/**
	 * 定义日志
	 */
	private static Logger logger = LoggerUtil.getLogger();

	/**
	 * 数据库方言定义
	 */
	public static final class Dialect {
		// oracle12c+
		public final static String ORACLE = "oracle";

		// oracle11g
		public final static String ORACLE11 = "oracle11";
		// 10.x
		public final static String DB2 = "db2";

		// sqlserver2012或以上版本
		public final static String SQLSERVER = "sqlserver";

		// mysql的三个变种，5.6版本或以上
		public final static String MYSQL = "mysql";
		public final static String MYSQL57 = "mysql57";
		public final static String INNOSQL = "innosql";
		public final static String MARIADB = "mariadb";

		// 9.5+ 开始
		public final static String POSTGRESQL = "postgresql";
		public final static String POSTGRESQL15 = "postgresql15";
		public final static String GREENPLUM = "greenplum";
		// 神通数据库
		public final static String OSCAR = "oscar";

		// 华为gaussdb(源于postgresql)未验证
		public final static String GAUSSDB = "gaussdb";

		// 3.0以上版本
		public final static String SQLITE = "sqlite";

		// mongodb
		public final static String MONGO = "mongo";

		// elasticsearch
		public final static String ES = "elastic";

		// 19.x版本
		public final static String CLICKHOUSE = "clickhouse";

		// 阿里 oceanbase(未验证)
		public final static String OCEANBASE = "oceanbase";

		// tidb(语法遵循mysql)
		public final static String TIDB = "tidb";

		// 达梦数据库(dm8验证)
		public final static String DM = "dm";

		// 人大金仓数据库
		public final static String KINGBASE = "kingbase";
		public final static String IMPALA = "impala";
		public final static String TDENGINE = "tdengine";

		// h2
		public final static String H2 = "h2";

		// mogdb
		public final static String MOGDB = "mogdb";
		// 海量数据库(opengauss)
		public final static String VASTBASE = "vastbase";

		public final static String UNDEFINE = "UNDEFINE";
	}

	/*
	 * 数据库类型数字标识
	 */
	public static final class DBType {
		// 未定义未识别
		public final static int UNDEFINE = 0;
		// 12c+
		public final static int ORACLE = 10;
		// 11g
		public final static int ORACLE11 = 11;
		// 10.x版本
		public final static int DB2 = 20;
		// 2017及以上版本
		public final static int SQLSERVER = 30;
		public final static int MYSQL = 40;
		public final static int MYSQL57 = 42;

		// 默认9.5+版本
		public final static int POSTGRESQL = 50;
		public final static int POSTGRESQL15 = 51;

		// clickhouse
		public final static int CLICKHOUSE = 60;

		// gaussdb
		public final static int GAUSSDB = 70;
		// sqlite
		public final static int SQLITE = 80;
		// tidb
		public final static int TIDB = 90;
		// 阿里oceanbase
		public final static int OCEANBASE = 100;
		// 达梦
		public final static int DM = 110;

		// 人大金仓数据库
		public final static int KINGBASE = 120;
		public final static int MONGO = 130;
		public final static int ES = 140;
		public final static int TDENGINE = 150;
		public final static int IMPALA = 160;
		// h2
		public final static int H2 = 170;
		public final static int OSCAR = 180;

		// MOGDB 基于openGauss开发。
		public final static int MOGDB = 190;
		public final static int VASTBASE = 200;
	}

	public static HashMap<String, Integer> DBNameTypeMap = new HashMap<String, Integer>();
	static {
		DBNameTypeMap.put(Dialect.DB2, DBType.DB2);
		DBNameTypeMap.put(Dialect.ORACLE, DBType.ORACLE);
		DBNameTypeMap.put(Dialect.ORACLE11, DBType.ORACLE11);
		DBNameTypeMap.put(Dialect.SQLSERVER, DBType.SQLSERVER);
		DBNameTypeMap.put(Dialect.MYSQL, DBType.MYSQL);
		DBNameTypeMap.put(Dialect.MYSQL57, DBType.MYSQL57);
		// mariaDB的方言以mysql为基准
		DBNameTypeMap.put(Dialect.MARIADB, DBType.MYSQL);
		DBNameTypeMap.put(Dialect.INNOSQL, DBType.MYSQL);

		DBNameTypeMap.put(Dialect.POSTGRESQL, DBType.POSTGRESQL);
		DBNameTypeMap.put(Dialect.POSTGRESQL15, DBType.POSTGRESQL15);
		DBNameTypeMap.put(Dialect.GREENPLUM, DBType.POSTGRESQL);
		DBNameTypeMap.put(Dialect.GAUSSDB, DBType.GAUSSDB);
		// 20240702 增加对mogdb的支持
		DBNameTypeMap.put(Dialect.MOGDB, DBType.MOGDB);

		DBNameTypeMap.put(Dialect.MONGO, DBType.MONGO);
		DBNameTypeMap.put(Dialect.ES, DBType.ES);
		DBNameTypeMap.put(Dialect.SQLITE, DBType.SQLITE);
		DBNameTypeMap.put(Dialect.CLICKHOUSE, DBType.CLICKHOUSE);
		DBNameTypeMap.put(Dialect.OCEANBASE, DBType.OCEANBASE);
		// 2020-6-5 增加对达梦数据库的支持
		DBNameTypeMap.put(Dialect.DM, DBType.DM);
		// 2020-8-14 增加对人大金仓数据库支持
		DBNameTypeMap.put(Dialect.KINGBASE, DBType.KINGBASE);
		// 2020-6-7 启动增加对tidb的支持
		DBNameTypeMap.put(Dialect.TIDB, DBType.TIDB);
		DBNameTypeMap.put(Dialect.TDENGINE, DBType.TDENGINE);
		DBNameTypeMap.put(Dialect.IMPALA, DBType.IMPALA);
		DBNameTypeMap.put(Dialect.UNDEFINE, DBType.UNDEFINE);
		// 20220829 增加对h2的支持
		DBNameTypeMap.put(Dialect.H2, DBType.H2);
		DBNameTypeMap.put(Dialect.OSCAR, DBType.OSCAR);
		DBNameTypeMap.put(Dialect.VASTBASE, DBType.VASTBASE);
	}

	/**
	 * @todo 获取数据库类型
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static String getCurrentDBDialect(final Connection conn) throws SQLException {
		String dilectName = Dialect.UNDEFINE;
		// 从hashMap中获取
		if (null != conn) {
			// 剔除空白
			String dbDialect = conn.getMetaData().getDatabaseProductName().replaceAll("\\s+", "");
			// oracle
			if (StringUtil.indexOfIgnoreCase(dbDialect, Dialect.ORACLE) != -1) {
				dilectName = Dialect.ORACLE;
			} // mysql以及mysql的分支数据库
			else if (StringUtil.indexOfIgnoreCase(dbDialect, Dialect.MYSQL) != -1
					|| StringUtil.indexOfIgnoreCase(dbDialect, Dialect.MARIADB) != -1
					|| StringUtil.indexOfIgnoreCase(dbDialect, Dialect.INNOSQL) != -1) {
				dilectName = Dialect.MYSQL;
			} // postgresql
			else if (StringUtil.indexOfIgnoreCase(dbDialect, Dialect.POSTGRESQL) != -1) {
				dilectName = Dialect.POSTGRESQL;
			} // sqlserver,只支持2012或以上版本
			else if (StringUtil.indexOfIgnoreCase(dbDialect, Dialect.SQLSERVER) != -1
					|| StringUtil.indexOfIgnoreCase(dbDialect, "mssql") != -1
					|| StringUtil.indexOfIgnoreCase(dbDialect, "microsoftsqlserver") != -1) {
				dilectName = Dialect.SQLSERVER;
			} // db2
			else if (StringUtil.indexOfIgnoreCase(dbDialect, Dialect.DB2) != -1) {
				dilectName = Dialect.DB2;
			} // clickhouse
			else if (StringUtil.indexOfIgnoreCase(dbDialect, Dialect.CLICKHOUSE) != -1) {
				dilectName = Dialect.CLICKHOUSE;
			} // OCEANBASE
			else if (StringUtil.indexOfIgnoreCase(dbDialect, Dialect.OCEANBASE) != -1) {
				dilectName = Dialect.OCEANBASE;
			} // GAUSSDB
			else if (StringUtil.indexOfIgnoreCase(dbDialect, Dialect.GAUSSDB) != -1
					|| "zenith".equalsIgnoreCase(dbDialect) || "opengauss".equalsIgnoreCase(dbDialect)) {
				dilectName = Dialect.GAUSSDB;
			} // MOGDB
			else if (StringUtil.indexOfIgnoreCase(dbDialect, Dialect.MOGDB) != -1) {
				dilectName = Dialect.MOGDB;
			} else if (StringUtil.indexOfIgnoreCase(dbDialect, Dialect.SQLITE) != -1) {
				dilectName = Dialect.SQLITE;
			} // dm
			else if (StringUtil.indexOfIgnoreCase(dbDialect, Dialect.DM) != -1) {
				dilectName = Dialect.DM;
			} // TIDB
			else if (StringUtil.indexOfIgnoreCase(dbDialect, Dialect.TIDB) != -1) {
				dilectName = Dialect.TIDB;
			} // 2022-12-14 验证
			else if (StringUtil.indexOfIgnoreCase(dbDialect, Dialect.TDENGINE) != -1) {
				dilectName = Dialect.TDENGINE;
			} else if (StringUtil.indexOfIgnoreCase(dbDialect, Dialect.KINGBASE) != -1) {
				dilectName = Dialect.KINGBASE;
			} else if (StringUtil.indexOfIgnoreCase(dbDialect, Dialect.GREENPLUM) != -1) {
				dilectName = Dialect.POSTGRESQL;
			} else if (StringUtil.indexOfIgnoreCase(dbDialect, Dialect.IMPALA) != -1) {
				dilectName = Dialect.IMPALA;
			} // elasticsearch
			else if (StringUtil.indexOfIgnoreCase(dbDialect, Dialect.ES) != -1) {
				dilectName = Dialect.ES;
			} // 20220829 h2
			else if (StringUtil.indexOfIgnoreCase(dbDialect, Dialect.H2) != -1) {
				dilectName = Dialect.H2;
			} else if (StringUtil.indexOfIgnoreCase(dbDialect, Dialect.OSCAR) != -1) {
				dilectName = Dialect.OSCAR;
			} else if (StringUtil.indexOfIgnoreCase(dbDialect, Dialect.VASTBASE) != -1) {
				dilectName = Dialect.VASTBASE;
			}
		}
		return dilectName;
	}

	/**
	 * @todo 获取当前数据库的版本
	 * @return
	 */
	public static int getCurrentDBVersion(final Connection conn) {
		// -1表示版本不确定
		int result = -1;
		// 部分数据库驱动还不支持此方法
		try {
			result = conn.getMetaData().getDatabaseMajorVersion();
		} catch (Exception e) {
		}
		return result;
	}

	/**
	 * @todo <b>获取数据库类型</b>
	 * @author zhongxuchen
	 * @date 2011-8-3 下午06:25:41
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static int getDbType(final Connection conn) throws SQLException {
		// 从hashMap中获取
		String productName = conn.getMetaData().getDatabaseProductName();
		int majorVersion = getCurrentDBVersion(conn);
		String dbKey = productName + majorVersion;
		if (!DBNameTypeMap.containsKey(dbKey)) {
			String dbDialect = getCurrentDBDialect(conn);
			int dbType = DBType.UNDEFINE;
			// oracle12+
			if (dbDialect.equals(Dialect.ORACLE)) {
				dbType = DBType.ORACLE;
				if (majorVersion <= 11) {
					dbType = DBType.ORACLE11;
				}
			} else if (dbDialect.equals(Dialect.ORACLE11)) {
				dbType = DBType.ORACLE11;
			} // mysql以及mysql的分支数据库
			else if (dbDialect.equals(Dialect.MYSQL)) {
				dbType = DBType.MYSQL;
				if (majorVersion <= 5) {
					dbType = DBType.MYSQL57;
				}
			} else if (dbDialect.equals(Dialect.MYSQL57)) {
				dbType = DBType.MYSQL57;
			} // 9.5以上为标准支持模式
			else if (dbDialect.equals(Dialect.POSTGRESQL)) {
				dbType = DBType.POSTGRESQL;
				if (majorVersion >= 15) {
					dbType = DBType.POSTGRESQL15;
				}
			} else if (dbDialect.equals(Dialect.POSTGRESQL15)) {
				dbType = DBType.POSTGRESQL15;
			} else if (dbDialect.equals(Dialect.GREENPLUM)) {
				dbType = DBType.POSTGRESQL;
			} // sqlserver,只支持2012或以上版本
			else if (dbDialect.equals(Dialect.SQLSERVER)) {
				dbType = DBType.SQLSERVER;
			} // db2 10+版本
			else if (dbDialect.equals(Dialect.DB2)) {
				dbType = DBType.DB2;
			} else if (dbDialect.equals(Dialect.CLICKHOUSE)) {
				dbType = DBType.CLICKHOUSE;
			} else if (dbDialect.equals(Dialect.OCEANBASE)) {
				dbType = DBType.OCEANBASE;
			} else if (dbDialect.equals(Dialect.GAUSSDB)) {
				dbType = DBType.GAUSSDB;
			} else if (dbDialect.equals(Dialect.MOGDB)) {
				dbType = DBType.MOGDB;
			} else if (dbDialect.equals(Dialect.SQLITE)) {
				dbType = DBType.SQLITE;
			} else if (dbDialect.equals(Dialect.DM)) {
				dbType = DBType.DM;
			} else if (dbDialect.equals(Dialect.TIDB)) {
				dbType = DBType.TIDB;
			} else if (dbDialect.equals(Dialect.IMPALA)) {
				dbType = DBType.IMPALA;
			} else if (dbDialect.equals(Dialect.TDENGINE)) {
				dbType = DBType.TDENGINE;
			} else if (dbDialect.equals(Dialect.KINGBASE)) {
				dbType = DBType.KINGBASE;
			} else if (dbDialect.equals(Dialect.ES)) {
				dbType = DBType.ES;
			} else if (dbDialect.equals(Dialect.H2)) {
				dbType = DBType.H2;
			} else if (dbDialect.equals(Dialect.OSCAR)) {
				dbType = DBType.OSCAR;
			} else if (dbDialect.equals(Dialect.VASTBASE)) {
				dbType = DBType.VASTBASE;
			}
			DBNameTypeMap.put(dbKey, dbType);
		}
		return DBNameTypeMap.get(dbKey);
	}

	/**
	 * @todo 提供统一的ResultSet,PreparedStatemenet 关闭功能
	 * @param userData
	 * @param pst
	 * @param rs
	 * @param preparedStatementResultHandler
	 * @return
	 */
	public static Object preparedStatementProcess(Object userData, PreparedStatement pst, ResultSet rs,
			PreparedStatementResultHandler preparedStatementResultHandler) throws Exception {
		try {
			preparedStatementResultHandler.execute(userData, pst, rs);
		} catch (Exception se) {
			se.printStackTrace();
			logger.info(se.getMessage());
			throw se;
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pst != null) {
					pst.close();
					pst = null;
				}
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
		return preparedStatementResultHandler.getResult();
	}
}
