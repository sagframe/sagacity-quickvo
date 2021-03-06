/**
 * 
 */
package org.sagacity.quickvo.utils.callback;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @project sagacity-quickvo
 * @description 数据库preparedStatement处理反调抽象类,用来处理result
 * @author zhongxuchen
 * @version v1.0,Date:2009-3-20
 */
public abstract class PreparedStatementResultHandler {
	private Object result;

	/**
	 * @param rowData
	 * @param pst
	 * @param rs
	 * @throws Exception
	 */
	public abstract void execute(Object rowData, PreparedStatement pst, ResultSet rs) throws Exception;

	public void setResult(Object result) {
		this.result = result;
	}

	public Object getResult() {
		return this.result;
	}
}
