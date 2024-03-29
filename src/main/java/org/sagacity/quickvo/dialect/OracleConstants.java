package org.sagacity.quickvo.dialect;

/**
 * @author zhongxuchen
 * @date 2020-10-12
 */
public class OracleConstants {
	/**
	 * 默认数据类型匹配关系定义
	 */
	public static final String[][] jdbcTypeMapping = {
			// jdbc.type java.type importType precision(数据长度) scale(小数位)
			{ "REAL", "Float", "" }, { "TINYINT", "Integer", "" }, { "SHORT", "Short", "" },
			{ "SMALLINT", "Integer", "" }, { "MEDIUMINT", "Integer", "" }, { "SERIAL", "Integer", "" },
			{ "BIGSERIAL", "BigInteger", "java.math.BigInteger" }, { "SERIAL8", "BigInteger", "java.math.BigInteger" },
			{ "LARGEINT", "BigInteger", "java.math.BigInteger" }, { "BIGINT", "BigInteger", "java.math.BigInteger" },
			{ "INT", "Integer", "" }, { "INTEGER", "Integer", "" }, { "INT2", "Integer", "" },
			{ "_INT2", "Integer[]", "" }, { "INT4", "Integer", "" }, { "_INT4", "Integer[]", "" },
			{ "Int8", "Integer", "" }, { "_Int8", "Long[]", "" }, { "Int16", "Integer", "" }, { "Int32", "Long", "" },
			{ "Int64", "BigInteger", "java.math.BigInteger" }, { "Enum8", "Integer", "" }, { "Enum16", "Integer", "" },
			{ "UInt8", "Integer", "" }, { "UInt16", "Integer", "" }, { "UInt32", "Long", "" },
			{ "UInt64", "BigInteger", "java.math.BigInteger" }, { "SERIAL", "Integer", "" }, { "FLOAT", "Float", "" },
			{ "FLOAT4", "Float", "" }, { "_FLOAT4", "Float[]", "" }, { "FLOAT8", "Float", "" },
			{ "_FLOAT8", "Double[]", "" }, { "FLOAT32", "Float", "" }, { "FLOAT64", "Double", "" },
			{ "DOUBLE", "Double", "" }, { "NUMBER", "BigDecimal", "java.math.BigDecimal" },
			{ "MONEY", "BigDecimal", "java.math.BigDecimal" }, { "SMALLMONEY", "BigDecimal", "java.math.BigDecimal" },
			{ "NUMERIC", "BigDecimal", "java.math.BigDecimal" }, { "_NUMERIC", "BigDecimal[]", "java.math.BigDecimal" },
			{ "DECIMAL", "BigDecimal", "java.math.BigDecimal" }, { "TIMESTAMP", "Timestamp", "java.sql.Timestamp" },
			{ "TIMESTAMP(6)", "Timestamp", "java.sql.Timestamp" },
			{ "BIGDECIMAL", "BigDecimal", "java.math.BigDecimal" }, { "DATE", "LocalDateTime", "java.time.LocalDateTime" },
			{ "DATETIME", "LocalDateTime", "java.time.LocalDateTime" }, { "TIME", "LocalTime", "java.time.LocalTime" },
			{ "YEAR", "LocalDate", "java.time.LocalDate" }, { "VARCHAR", "String", "" }, { "_VARCHAR", "String[]", "" },
			{ "MEDIUMTEXT", "String", "" }, { "VARCHAR2", "String", "" }, { "LONG VARCHAR", "String", "" },
			{ "LONGVARCHAR", "String", "" }, { "NVARCHAR", "String", "" }, { "NVARCHAR2", "String", "" },
			{ "LONGNVARCHAR", "String", "" }, { "NCHAR", "String", "" }, { "JSON", "String", "" },
			{ "STRING", "String", "" }, { "FixedSTRING", "String", "" }, { "CHAR", "String", "" },
			{ "BPCHAR", "String", "" }, { "CHARACTER", "String", "" }, { "BIT", "Boolean", "" },
			{ "BOOLEAN", "Boolean", "" }, { "BOOL", "Boolean", "" }, { "Clob", "String", "java.sql.Clob" },
			{ "DBCLOB", "String", "java.sql.Clob" }, { "NCLOB", "String", "java.sql.Clob" },
			{ "CLOB", "String", "oracle.sql.CLOB", "oracle" }, { "BLOB", "byte[]", "oracle.sql.BLOB", "oracle" },
			{ "Blob", "byte[]", "java.sql.Blob" }, { "LONGBLOB", "byte[]", "java.sql.Blob" },
			{ "MEDIUMBLOB", "byte[]", "java.sql.Blob" }, { "TEXT", "String", "" }, { "_TEXT", "String[]", "" },
			{ "LONGTEXT", "String", "" }, { "TINYTEXT", "String" }, { "LONG VARGRAPHIC", "String", "" },
			{ "VARGRAPHIC", "String", "" }, { "GRAPHIC", "String", "" }, { "LONG VARCHAR", "String", "" },
			{ "IMAGE", "byte[]", "" }, { "VARBINARY", "byte[]", "" }, { "GEOMETRY", "Object", "" },
			{ "SDO_GEOMETRY", "Object", "" }, { "JSONB", "byte[]", "" }, { "BINARY", "byte[]", "" },
			{ "BYTEA", "byte[]", "" }, { "LONGVARBINARY", "byte[]", "" } };

	public static final String[][] jdbcAry = { { "REAL", "REAL" }, { "YEAR", "DATE" }, { "DateTime", "DATE" },
			{ "Int", "INTEGER" }, { "Int2", "INTEGER" }, { "_Int2", "ARRAY" }, { "Int4", "INTEGER" },
			{ "_Int4", "ARRAY" }, { "Int8", "INTEGER" }, { "_Int8", "ARRAY" }, { "SMALLINT", "SMALLINT" },
			{ "LARGEINT", "BIGINT" }, { "MEDIUMINT", "INTEGER", "" }, { "Int16", "INTEGER" }, { "Int32", "INTEGER" },
			{ "Int64", "BIGINT" }, { "SERIAL8", "BIGINT" }, { "SERIAL", "INTEGER", "" }, { "BIGSERIAL", "BIGINT" },
			{ "Enum8", "INTEGER" }, { "Enum16", "INTEGER" }, { "UInt8", "INTEGER" }, { "UInt16", "INTEGER" },
			{ "UInt32", "INTEGER" }, { "UInt64", "BIGINT" }, { "FLOAT4", "FLOAT" }, { "_FLOAT4", "ARRAY" },
			{ "FLOAT8", "FLOAT" }, { "_FLOAT8", "ARRAY" }, { "FLOAT32", "FLOAT" }, { "FLOAT64", "DOUBLE" },
			{ "STRING", "VARCHAR" }, { "FixedSTRING", "VARCHAR" }, { "LONG VARGRAPHIC", "CLOB" },
			{ "VARGRAPHIC", "VARCHAR" }, { "GRAPHIC", "VARCHAR" }, { "LONG VARCHAR", "VARCHAR" }, { "DATE", "DATE" },
			{ "DATETIME", "DATE" }, { "TIMESTAMP", "TIMESTAMP" }, { "TIMESTAMP(6)", "TIMESTAMP" }, { "TIME", "TIME" },
			{ "CHAR", "CHAR" }, { "CLOB", "CLOB" }, { "DBCLOB", "CLOB" }, { "JSONB", "BLOB" }, { "BLOB", "BLOB" },
			{ "BINARY", "BINARY" }, { "VARBINARY", "BINARY" }, { "LONGVARBINARY", "BINARY" }, { "BYTEA", "BINARY" },
			{ "LONGBLOB", "BLOB" }, { "BOOLEAN", "BOOLEAN" }, { "BOOL", "BOOLEAN" }, { "MEDIUMBLOB", "BLOB" },
			{ "LONGTEXT", "VARCHAR" }, { "MEDIUMTEXT", "VARCHAR" }, { "TEXT", "VARCHAR" }, { "_TEXT", "ARRAY" },
			{ "JSON", "VARCHAR" }, { "TINYTEXT", "VARCHAR" }, { "VARCHAR", "VARCHAR" }, { "_VARCHAR", "ARRAY" },
			{ "NVARCHAR", "VARCHAR" }, { "NVARCHAR2", "VARCHAR" }, { "BPCHAR", "VARCHAR" }, { "VARCHAR2", "VARCHAR" },
			{ "TINYINT", "TINYINT" }, { "INT", "INTEGER" }, { "INTEGER", "INTEGER" }, { "BIGINT", "BIGINT" },
			{ "BIT", "BIT" }, { "NUMBER", "DECIMAL" }, { "DECIMAL", "DECIMAL" }, { "MONEY", "DECIMAL" },
			{ "SMALLMONEY", "DECIMAL" }, { "NUMERIC", "DECIMAL" }, { "_NUMERIC", "ARRAY" },
			{ "IMAGE", "LONGVARBINARY" }, { "GEOMETRY", "VARCHAR" }, { "SDO_GEOMETRY", "VARCHAR" } };
}
