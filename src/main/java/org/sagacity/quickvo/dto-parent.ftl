/**
 *@Generated by sagacity-quickvo 5.0
 */
package ${quickVO.voPackage};

import ${quickVO.voPackage}.${quickVO.abstractPath}.Abstract${quickVO.voName};
<#if (quickVO.apiDoc=="swagger-v2")>
import io.swagger.annotations.ApiModel;
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
<#if (quickVO.apiDocImports?exists && quickVO.apiDocImports?size>0)>
<#list quickVO.apiDocImports as import>
import ${import};
</#list>
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
public class ${quickVO.voName} extends Abstract${quickVO.voName} {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = ${quickVO.voSerialUID}L;
}