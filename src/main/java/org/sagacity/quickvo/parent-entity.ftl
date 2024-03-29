/**
 *@Generated by sagacity-quickvo 5.0
 */
package ${quickVO.entityPackage};

import org.sagacity.sqltoy.config.annotation.SqlToyEntity;
<#if (quickVO.imports?exists && quickVO.imports?size>0)>
<#list quickVO.imports as import>
import ${import};
</#list>
</#if>
import ${quickVO.entityPackage}.${quickVO.abstractPath}.Abstract${quickVO.entityName};

/**
<#if (quickVO.projectName?exists)> * @project ${quickVO.projectName}</#if>
<#if (quickVO.author?exists)> * @author ${quickVO.author}</#if>
<#if (quickVO.version?exists)> * @version ${quickVO.version}</#if>
 * Table: ${quickVO.tableName}<#if (quickVO.tableRemark?exists && quickVO.tableRemark!='')>,Remark:${quickVO.tableRemark}</#if> 	
 */
@SqlToyEntity
public class ${quickVO.entityName} extends Abstract${quickVO.entityName} {	
	/**
	 * 
	 */
	private static final long serialVersionUID = ${quickVO.entitySerialUID}L;
	
	/** default constructor */
	public ${quickVO.entityName}() {
		super();
	}
	
<#if (quickVO.type=="TABLE")>	
	/*---begin-constructor-area---don't-update-this-area--*/
<#if (quickVO.singlePk=='1'||quickVO.singlePk=='0')>
<#assign paramCnt="0"/> 
	/** pk constructor */
	public ${quickVO.entityName}(<#list quickVO.columns as column><#if (column.pkFlag=='1')><#if (paramCnt=='1')>,</#if><#assign paramCnt='1'/>${column.resultType} ${column.colJavaName?uncap_first}</#if></#list>)
	{
		<#list quickVO.columns as column>
		<#if (column.pkFlag=='1')>
		this.${column.colJavaName?uncap_first}=${column.colJavaName?uncap_first};
		</#if>
		</#list>
	}
</#if>
	/*---end-constructor-area---don't-update-this-area--*/
</#if>

	/**
     * @todo vo columns to String
     */
    @Override
	public String toString() {
		return super.toString();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public ${quickVO.entityName} clone() {
		try {
			return (${quickVO.entityName}) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}