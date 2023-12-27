package org.sagacity.quickvo.model;

/**
 * 索引信息
 * 
 * @author zhong
 *
 */
public class IndexModel {
	/**
	 * 索引名称
	 */
	private String indexName;

	/**
	 * 表名称
	 */
	private String tableName;

	/**
	 * 是否唯一索引
	 */
	private Boolean isUnique;

	/**
	 * 对应列
	 */
	private String[] columns;

	/**
	 * 排序类型
	 */
	private String[] sortTypes;

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Boolean getIsUnique() {
		return isUnique;
	}

	public void setIsUnique(Boolean isUnique) {
		this.isUnique = isUnique;
	}

	public String[] getColumns() {
		return columns;
	}

	public void setColumns(String[] columns) {
		this.columns = columns;
	}

	public String[] getSortTypes() {
		return sortTypes;
	}

	public void setSortTypes(String[] sortTypes) {
		this.sortTypes = sortTypes;
	}

	public String getColumnsAry() {
		String result = "";
		for (int i = 0; i < columns.length; i++) {
			if (i > 0) {
				result = result.concat(",");
			}
			result = result.concat("\"").concat(columns[i]).concat("\"");
		}
		return result;
	}

	public String getSortTypesAry() {
		String result = "";
		for (int i = 0; i < sortTypes.length; i++) {
			if (i > 0) {
				result = result.concat(",");
			}
			if (sortTypes[i] == null) {
				result = result.concat("null");
			} else {
				result = result.concat("\"").concat(sortTypes[i]).concat("\"");
			}
		}
		return result;
	}

}
