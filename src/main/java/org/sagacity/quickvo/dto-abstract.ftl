/**
 *@Generated by sagacity-quickvo 4.18
 */
package ${quickVO.voPackage}.${quickVO.abstractPath};

import java.io.Serializable;
<#if (quickVO.swaggerModel=="v2")>
import io.swagger.annotations.ApiModelProperty;
</#if>
<#if (quickVO.swaggerModel=="v3")>
import io.swagger.v3.oas.annotations.media.Schema;
</#if>
<#if (quickVO.lombok)>
import lombok.*;
</#if>
<#if (quickVO.imports?exists && quickVO.imports?size>0)>
<#list quickVO.imports as import>
import ${import};
</#list>
</#if>
<#if (quickVO.voExtends?exists)>
import ${quickVO.voExtends};
</#if>

/**
 * @project <#if (quickVO.projectName?exists)>${quickVO.projectName}</#if>
 * @version <#if (quickVO.version?exists)>${quickVO.version}</#if>
 * <#if (quickVO.tableRemark?exists && quickVO.tableRemark!='')>@description ${quickVO.tableRemark}</#if>  
 */
<#if (quickVO.lombok)>
@Data
<#if (quickVO.lombokChain)>
@Accessors(fluent = true)
</#if>
</#if>
<#if (quickVO.voExtends?exists)>
public class Abstract${quickVO.voName} extends ${quickVO.voExtends?substring(quickVO.voExtends?last_index_of(".")+1)} implements Serializable {
<#else>
public class Abstract${quickVO.voName} implements Serializable {
</#if>
	
	/**
	 * 
	 */
	private static final long serialVersionUID = ${quickVO.voAbstractSerialUID}L;
	
<#list quickVO.columns as column>
	/**
	 * ${column.colRemark!""}
	 */
	<#if (quickVO.swaggerModel=="v2")>
	@ApiModelProperty(name="${column.colName}",value="${column.colRemark}"<#if (column.nullable=='0')>,allowEmptyValue=false<#else>,allowEmptyValue=true</#if>)
	</#if>
	<#if (quickVO.swaggerModel=="v3")>
	@Schema(name="${column.colName}",description="${column.colRemark}"<#if (column.nullable=='0')>,nullable=false<#else>,nullable=true</#if>)
	</#if>
	protected ${column.resultType} ${column.colJavaName?uncap_first};
	
</#list>
<#if (quickVO.lombok==false)>
<#list quickVO.columns as column>
	
	/**
	 *@param ${column.colJavaName?uncap_first} the ${column.colJavaName?uncap_first} to set
	 */
	<#if (quickVO.returnSelf==true)>
	public Abstract${quickVO.voName} set${column.colJavaName?cap_first}(${column.resultType} ${column.colJavaName?uncap_first}) {
		this.${column.colJavaName?uncap_first}=${column.colJavaName?uncap_first};
		return this;
	}
	<#else>
	public void set${column.colJavaName?cap_first}(${column.resultType} ${column.colJavaName?uncap_first}) {
		this.${column.colJavaName?uncap_first}=${column.colJavaName?uncap_first};
	}
	</#if>
		
	/**
	 *@return the ${column.colJavaName}
	 */
	public ${column.resultType} get${column.colJavaName?cap_first}() {
	    return this.${column.colJavaName?uncap_first};
	}
</#list>
</#if>
}