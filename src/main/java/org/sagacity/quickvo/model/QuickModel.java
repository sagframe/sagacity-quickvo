/**
 * 
 */
package org.sagacity.quickvo.model;

import java.io.Serializable;

/**
 * @project sagacity-quickvo
 * @description 单个任务的配置
 * @author zhongxuchen
 * @version v1.0,Date:2009-04-19
 */
public class QuickModel implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2416884401329432969L;
	private String entityName;
	private String entityPackage;
	private String entitySubstr;
	private String voPackage;
	private String voSubstr;
	private String voName;

	/**
	 * 路径
	 */
	private String voPath;

	private String entityPath;

	/**
	 * 字段统一剔除的前缀
	 */
	private String fieldRidPrefix;

	/**
	 * 实体类继承
	 */
	private String entityExtends;

	/**
	 * vo继承父类
	 */
	private String voExtends;

	/**
	 * 是否支持google的 lombok
	 */
	private boolean lombok = false;

	/**
	 * 支持链式赋值
	 */
	private boolean lombokChain = false;

	private boolean entityLombok = false;

	private boolean entityLombokChain = false;

	private String dataSource;

	/**
	 * 是否包含抽象实体类
	 */
	private boolean hasAbstractEntity = true;

	/**
	 * 是否存在抽象VO
	 */
	private boolean hasAbstractVO = false;

	/**
	 * 是否支持swagger 注解
	 */
	private String swaggerApi = "false";

	/**
	 * 生成api注释的标准
	 */
	private String apiDoc = "false";

	/**
	 * 作者，主要针对Dao层提供任务责任人
	 */
	private String author;

	/**
	 * 包含的表
	 */
	private String includeTables;

	/**
	 * 排除的表
	 */
	private String excludeTables;

	private boolean hasEntity = false;

	private boolean hasVO = false;

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public String getEntityPackage() {
		return entityPackage;
	}

	public void setEntityPackage(String entityPackage) {
		this.entityPackage = entityPackage;
	}

	public String getVoPackage() {
		return voPackage;
	}

	public void setVoPackage(String voPackage) {
		this.voPackage = voPackage;
	}

	public String getVoSubstr() {
		return voSubstr;
	}

	public void setVoSubstr(String voSubstr) {
		this.voSubstr = voSubstr;
	}

	public String getVoName() {
		return voName;
	}

	public void setVoName(String voName) {
		this.voName = voName;
	}

	/**
	 * @return the includeTables
	 */
	public String getIncludeTables() {
		return includeTables;
	}

	/**
	 * @param includeTables the includeTables to set
	 */
	public void setIncludeTables(String includeTables) {
		this.includeTables = includeTables;
	}

	/**
	 * @return the excludeTables
	 */
	public String getExcludeTables() {
		return excludeTables;
	}

	/**
	 * @param excludeTables the excludeTables to set
	 */
	public void setExcludeTables(String excludeTables) {
		this.excludeTables = excludeTables;
	}

	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * @return the dataSource
	 */
	public String getDataSource() {
		return dataSource;
	}

	/**
	 * @param dataSource the dataSource to set
	 */
	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * @return the swaggerApi
	 */
	public String getSwaggerApi() {
		return swaggerApi;
	}

	/**
	 * @param swaggerApi the swaggerApi to set
	 */
	public void setSwaggerApi(String swaggerApi) {
		this.swaggerApi = swaggerApi;
	}

	/**
	 * @return the lombok
	 */
	public boolean isLombok() {
		return lombok;
	}

	/**
	 * @param lombok the lombok to set
	 */
	public void setLombok(boolean lombok) {
		this.lombok = lombok;
	}

	/**
	 * @return the lombokChain
	 */
	public boolean isLombokChain() {
		return lombokChain;
	}

	/**
	 * @param lombokChain the lombokChain to set
	 */
	public void setLombokChain(boolean lombokChain) {
		this.lombokChain = lombokChain;
	}

	public String getEntitySubstr() {
		return entitySubstr;
	}

	public void setEntitySubstr(String entitySubstr) {
		this.entitySubstr = entitySubstr;
	}

	public boolean isHasAbstractEntity() {
		return hasAbstractEntity;
	}

	public void setHasAbstractEntity(boolean hasAbstractEntity) {
		this.hasAbstractEntity = hasAbstractEntity;
	}

	/**
	 * @return the voExtends
	 */
	public String getVoExtends() {
		return voExtends;
	}

	/**
	 * @param voExtends the voExtends to set
	 */
	public void setVoExtends(String voExtends) {
		this.voExtends = voExtends;
	}

	/**
	 * @return the entityExtends
	 */
	public String getEntityExtends() {
		return entityExtends;
	}

	/**
	 * @param entityExtends the entityExtends to set
	 */
	public void setEntityExtends(String entityExtends) {
		this.entityExtends = entityExtends;
	}

	/**
	 * @return the fieldRidPrefix
	 */
	public String getFieldRidPrefix() {
		return fieldRidPrefix;
	}

	/**
	 * @param fieldRidPrefix the fieldRidPrefix to set
	 */
	public void setFieldRidPrefix(String fieldRidPrefix) {
		this.fieldRidPrefix = fieldRidPrefix;
	}

	/**
	 * @return the voPath
	 */
	public String getVoPath() {
		return voPath;
	}

	/**
	 * @param voPath the voPath to set
	 */
	public void setVoPath(String voPath) {
		this.voPath = voPath;
	}

	/**
	 * @return the entityPath
	 */
	public String getEntityPath() {
		return entityPath;
	}

	/**
	 * @param entityPath the entityPath to set
	 */
	public void setEntityPath(String entityPath) {
		this.entityPath = entityPath;
	}

	/**
	 * @return the hasEntity
	 */
	public boolean isHasEntity() {
		return hasEntity;
	}

	/**
	 * @param hasEntity the hasEntity to set
	 */
	public void setHasEntity(boolean hasEntity) {
		this.hasEntity = hasEntity;
	}

	/**
	 * @return the hasVO
	 */
	public boolean isHasVO() {
		return hasVO;
	}

	/**
	 * @param hasVO the hasVO to set
	 */
	public void setHasVO(boolean hasVO) {
		this.hasVO = hasVO;
	}

	/**
	 * @return the hasAbstractVO
	 */
	public boolean isHasAbstractVO() {
		return hasAbstractVO;
	}

	/**
	 * @param hasAbstractVO the hasAbstractVO to set
	 */
	public void setHasAbstractVO(boolean hasAbstractVO) {
		this.hasAbstractVO = hasAbstractVO;
	}

	public boolean isEntityLombok() {
		return entityLombok;
	}

	public void setEntityLombok(boolean entityLombok) {
		this.entityLombok = entityLombok;
	}

	public boolean isEntityLombokChain() {
		return entityLombokChain;
	}

	public void setEntityLombokChain(boolean entityLombokChain) {
		this.entityLombokChain = entityLombokChain;
	}

	public String getApiDoc() {
		return apiDoc;
	}

	public void setApiDoc(String apiDoc) {
		this.apiDoc = apiDoc;
	}

}
