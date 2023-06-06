/**
 *@Generated by sagacity-quickvo 5.0
 */
package ${quickVO.voPackage};

import java.io.Serializable;
<#if (quickVO.apiDoc=="swagger-v2")>
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
</#if>
<#if (quickVO.apiDoc=="swagger-v3")>
import io.swagger.v3.oas.annotations.media.Schema;
</#if>
<#if (quickVO.lombok)>
import lombok.Data;
<#if (quickVO.lombokChain)>
import lombok.experimental.Accessors;
</#if>
</#if>
<#if (quickVO.imports?exists && quickVO.imports?size>0)>
<#list quickVO.imports as import>
import ${import};
</#list>
</#if>
<#if (quickVO.apiDocImports?exists && quickVO.apiDocImports?size>0)>
<#list quickVO.apiDocImports as import>
import ${import};
</#list>
</#if>
<#if (quickVO.voExtends?exists)>
import ${quickVO.voExtends};
</#if>

/**
<#if (quickVO.projectName?exists)> * @project ${quickVO.projectName}</#if>
<#if (quickVO.author?exists)> * @author ${quickVO.author}</#if>
<#if (quickVO.version?exists)> * @version ${quickVO.version}</#if>
 * <#if (quickVO.tableRemark?exists && quickVO.tableRemark!='')>@description ${quickVO.tableName},${quickVO.tableRemark}</#if>  
 */
<#if (quickVO.apiDoc=="swagger-v2")>
@ApiModel(value="${quickVO.voName}"<#if (quickVO.tableRemark?exists && quickVO.tableRemark!='')>,description="${quickVO.tableRemark}"</#if>)
</#if>
<#if (quickVO.apiDoc=="swagger-v3")>
@Schema(name="${quickVO.voName}"<#if (quickVO.tableRemark?exists && quickVO.tableRemark!='')>,description="${quickVO.tableRemark}"</#if>)
</#if>
<#if (quickVO.apiDocContent?exists)>
${quickVO.apiDocContent}
</#if>
<#if (quickVO.lombok)>
@Data
<#if (quickVO.lombokChain)>
@Accessors(chain = true)
</#if>
</#if>
<#if (quickVO.voExtends?exists)>
public class ${quickVO.voName} extends ${quickVO.voExtends?substring(quickVO.voExtends?last_index_of(".")+1)} implements Serializable {
<#else>
public class ${quickVO.voName} implements Serializable {
</#if>
	
	/**
	 * 
	 */
	private static final long serialVersionUID = ${quickVO.voSerialUID}L;
	
/*---begin-auto-generate-don't-update-this-area--*/	
<#list quickVO.columns as column>
<#if (column.skipVO==false)>

	<#assign hasRemark=true>
	<#if (quickVO.apiDoc=="swagger-v2")>
	<#assign hasRemark=false>
	@ApiModelProperty(name="${column.colJavaName?uncap_first}",value="${column.colRemark!""}"<#if (column.nullable=='0')>,allowEmptyValue=false<#else>,allowEmptyValue=true</#if>)
	</#if>
	<#if (quickVO.apiDoc=="swagger-v3")>
	<#assign hasRemark=false>
	@Schema(name="${column.colJavaName?uncap_first}",description="${column.colRemark!""}"<#if (column.nullable=='0')>,nullable=false<#else>,nullable=true</#if>)
	</#if>
	<#if (column.apiDocContent?exists)>
	<#assign hasRemark=false>
	${column.apiDocContent}
	</#if>
	<#if (hasRemark==true)>
	/**
	 * ${column.colRemark!""}
	 */
	</#if>
	private ${column.resultType} ${column.colJavaName?uncap_first};
</#if>
</#list>
<#if (quickVO.columnSize==0)>
   // 未能获得表字段信息,请检查quickvo.xml 中dataSource的schema 和 catalog配置，可尝试先去除schema\catalog
   // 内部原理: conn.getMetaData().getColumns(catalog, schema, tableName, null);
</#if>
<#if (quickVO.lombok==false)>
<#list quickVO.columns as column>
<#if (column.skipVO==false)>
	
	/**
	 *@param ${column.colJavaName?uncap_first} the ${column.colJavaName?uncap_first} to set
	 */
	<#if (quickVO.returnSelf==true)>
	public ${quickVO.voName} set${column.colJavaName?cap_first}(${column.resultType} ${column.colJavaName?uncap_first}) {
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
</#if>
</#list>
</#if>
/*---end-auto-generate-don't-update-this-area--*/	
}