/**
 * 
 */
package org.sagacity.quickvo.dialect;

/**
 * @author zhongxuchen
 * @date 2020-10-12
 */
public class PostgresConstants {
	public static final String[][] jdbcTypeMapping = {
			// jdbc.type java.type importType precision(数据长度) scale(小数位)
			{ "REAL", "Float", "" }, { "TINYINT", "Integer", "" }, { "SHORT", "Short", "" },
			{ "SMALLINT", "Integer", "" }, { "MEDIUMINT", "Integer", "" }, { "SERIAL", "Integer", "" },
			{ "BIGSERIAL", "BigInteger", "java.math.BigInteger" }, { "SERIAL8", "BigInteger", "java.math.BigInteger" },
			{ "BIGINT", "BigInteger", "java.math.BigInteger" }, { "INT", "Integer", "" }, { "INTEGER", "Integer", "" },
			{ "INT2", "Integer", "" }, { "_INT2", "Integer[]", "" }, { "INT4", "Integer", "" },
			{ "_INT4", "Integer[]", "" }, { "Int8", "Long", "" }, { "_Int8", "Long[]", "" },
			{ "SERIAL", "Integer", "" }, { "FLOAT", "Float", "" }, { "FLOAT4", "Float", "" },
			{ "_FLOAT4", "Float[]", "" }, { "FLOAT8", "Double", "" }, { "_FLOAT8", "Double[]", "" },
			{ "DOUBLE", "Double", "" }, { "NUMBER", "BigDecimal", "java.math.BigDecimal" },
			{ "MONEY", "BigDecimal", "java.math.BigDecimal" }, { "SMALLMONEY", "BigDecimal", "java.math.BigDecimal" },
			{ "NUMERIC", "BigDecimal", "java.math.BigDecimal" }, { "_NUMERIC", "BigDecimal[]", "java.math.BigDecimal" },
			{ "DECIMAL", "BigDecimal", "java.math.BigDecimal" }, { "TIMESTAMP", "Timestamp", "java.sql.Timestamp" },
			{ "TIMESTAMP(6)", "Timestamp", "java.sql.Timestamp" },
			{ "BIGDECIMAL", "BigDecimal", "java.math.BigDecimal" }, { "DATE", "LocalDate", "java.time.LocalDate" },
			{ "DATETIME", "LocalDateTime", "java.time.LocalDateTime" }, { "TIME", "LocalTime", "java.time.LocalTime" },
			{ "YEAR", "LocalDate", "java.time.LocalDate" }, { "VARCHAR", "String", "" }, { "_VARCHAR", "String[]", "" },
			{ "MEDIUMTEXT", "String", "" }, { "VARCHAR2", "String", "" }, { "LONG VARCHAR", "String", "" },
			{ "LONGVARCHAR", "String", "" }, { "NVARCHAR", "String", "" }, { "LONGNVARCHAR", "String", "" },
			{ "NCHAR", "String", "" }, { "JSON", "String", "" }, { "STRING", "String", "" }, { "CHAR", "String", "" },
			{ "BPCHAR", "String", "" }, { "CHARACTER", "String", "" }, { "BIT", "Boolean", "" },
			{ "BOOLEAN", "Boolean", "" }, { "BOOL", "Boolean", "" }, { "TEXT", "String", "" },
			{ "_TEXT", "String[]", "" }, { "LONGTEXT", "String", "" }, { "TINYTEXT", "String" },
			{ "LONG VARCHAR", "String", "" }, { "GEOMETRY", "Object", "" }, { "SDO_GEOMETRY", "Object", "" },
			{ "JSONB", "byte[]", "" }, { "BINARY", "byte[]", "" }, { "BYTEA", "byte[]", "" }, { "UUID", "String", "" },
			{ "TSVECTOR", "String", "" } };

	public static final String[][] jdbcAry = { { "REAL", "REAL" }, { "YEAR", "DATE" }, { "DateTime", "DATE" },
			{ "Int", "INTEGER" }, { "Int2", "INTEGER" }, { "_Int2", "ARRAY" }, { "Int4", "INTEGER" },
			{ "_Int4", "ARRAY" }, { "Int8", "BIGINT" }, { "_Int8", "ARRAY" }, { "SMALLINT", "SMALLINT" },
			{ "SERIAL8", "BIGINT" }, { "SERIAL", "INTEGER", "" }, { "BIGSERIAL", "BIGINT" }, { "FLOAT4", "FLOAT" },
			{ "_FLOAT4", "ARRAY" }, { "FLOAT8", "DOUBLE" }, { "_FLOAT8", "ARRAY" }, { "STRING", "VARCHAR" },
			{ "LONG VARCHAR", "VARCHAR" }, { "DATE", "DATE" }, { "DATETIME", "DATE" }, { "TIMESTAMP", "TIMESTAMP" },
			{ "TIMESTAMP(6)", "TIMESTAMP" }, { "TIME", "TIME" }, { "CHAR", "CHAR" }, { "JSONB", "VARCHAR" },
			{ "BINARY", "BINARY" }, { "BYTEA", "BINARY" }, { "BOOLEAN", "BOOLEAN" }, { "BOOL", "BOOLEAN" },
			{ "LONGTEXT", "VARCHAR" }, { "MEDIUMTEXT", "VARCHAR" }, { "TEXT", "VARCHAR" }, { "_TEXT", "ARRAY" },
			{ "JSON", "VARCHAR" }, { "TINYTEXT", "VARCHAR" }, { "VARCHAR", "VARCHAR" }, { "_VARCHAR", "ARRAY" },
			{ "NVARCHAR", "VARCHAR" }, { "BPCHAR", "VARCHAR" }, { "VARCHAR2", "VARCHAR" }, { "TINYINT", "TINYINT" },
			{ "INT", "INTEGER" }, { "INTEGER", "INTEGER" }, { "BIGINT", "BIGINT" }, { "BIT", "BIT" },
			{ "NUMBER", "DECIMAL" }, { "DECIMAL", "DECIMAL" }, { "MONEY", "DECIMAL" }, { "SMALLMONEY", "DECIMAL" },
			{ "NUMERIC", "DECIMAL" }, { "_NUMERIC", "ARRAY" }, { "GEOMETRY", "VARCHAR" }, { "SDO_GEOMETRY", "VARCHAR" },
			{ "UUID", "VARCHAR" }, { "TSVECTOR", "VARCHAR" } };
}
