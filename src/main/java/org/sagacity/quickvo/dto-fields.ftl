/*---begin-auto-generate-don't-update-this-area--*/	
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
	private ${column.resultType} ${column.colJavaName?uncap_first};
	
</#list>
<#if (quickVO.lombok==false)>
<#list quickVO.columns as column>
	
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
</#list>
</#if>
/*---end-auto-generate-don't-update-this-area--*/	