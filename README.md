# sagacity-quickvo 是sqltoy-orm配套的POJO生成工具
# 当前版本
```xml
<!-- https://mvnrepository.com/artifact/com.sagframe/sqltoy-quickvo -->
<dependency>
    <groupId>com.sagframe</groupId>
    <artifactId>sqltoy-quickvo</artifactId>
    <version>5.2.4</version>
</dependency>

```
# 使用方式
* 请参见 https://github.com/sagframe/sqltoy-quickstart

* 在tools/quickvo 目录下面，配置quickvo.xml 设置相关任务信息，点击quickvo.bat 生成POJO
* 参照下面的例子：注意根据项目实际情况决定是否要严格分entity和dto(vo)两层

```xml
<?xml version="1.0" encoding="UTF-8"?>
<quickvo xmlns="http://www.sagframe.com/schema/quickvo"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.sagframe.com/schema/quickvo http://www.sagframe.com/schema/sqltoy/quickvo.xsd">
	<!-- db配置文件 -->
	<property file="db.properties" />
	<property name="project.version" value="1.0.0" />
	<property name="project.name" value="sqltoy-strict" />
	<property name="project.package" value="com.strict.modules" />
	<property name="include.schema" value="false" />
	<!-- set method 是否支持返回对象自身(默认是true),即: public VO setName(String name){this.name=name;return this;} -->
	<property name="field.support.linked.set" value="false" />
	<!-- 演示自定义类似swagger api文档注释实现,配合task定义中的api-doc="custom" 使用
	     当然这里是演示，正常task中的api-doc 有swagger-v3、swagger-v2默认选项
	 -->
	<api-doc>
		<imports value="io.swagger.v3.oas.annotations.media.Schema" />
		<doc-class-template>
		<![CDATA[@Schema(name="${className}",description="${tableRemark}")]]>
		</doc-class-template>
                <!-- 固化属性: colName, colRemark, nullable, fieldType, fieldName, defaultValue  -->
		<doc-field-template>
		<![CDATA[@Schema(name="${fieldName}",description="${colRemark}",nullable=${nullable})]]>
		</doc-field-template>
	</api-doc>
	<!-- schema 对照关系:mysql 对应 db 名称; oracle 对应 用户名称，如出现字段重复等情况，请结合schema和catalog进行配置过滤 -->
	<!-- 注意:当在多schema或tablespace场景下,在POJO中会出现重复字段，是因为schema和catalog 配置不正确，没有完成隔离 -->
	<datasource name="strict" url="${db.url}" driver="${db.driver_class}" 
		schema="${db.schema}" catalog="${db.schema}" username="${db.username}" password="${db.password}" />
	
	<tasks dist="../../src/main/java" encoding="UTF-8">
		<task active="true" author="zhongxuchen" include="^SAG_\w+" datasource="strict" api-doc="swagger-v3|swagger-v2|custom|false">
		    <!-- 
                     entity 配置中存在has-abstract:默认为true,可以设置为false表示pojo不需要抽象类，
		     可以设置extends="package.parentClass"指定父类 
		     skip-extends-fields="${commonFilelds}"   lombok="true" lombok-chain="true"
                    -->
		    <entity package="${project.package}.sagacity.entity" substr="Sag" name="#{subName}"/>
		    <!-- 在pojo和vo严格分层情况下，VO支持 lombok="true" lombok-chain="true" 避免生成get/set，
                         to-dir: 支持 extends指定父类
			 skip-extends-fields="${commonFilelds}" 
		     -->
		     <vo package="${project.package}.sagacity.vo" substr="Sag" name="#{subName}VO" />
		</task>
		<task active="true" author="zhongxuchen" include="^SYS_\w+" datasource="strict" swagger-model="true">
		     <entity package="${project.package}.system.entity" substr="Sys" name="#{subName}"/>
		     <vo package="${project.package}.system.vo" substr="Sys" name="#{subName}VO" />
		</task>
	</tasks>
	<!-- 主键策略配置: identity类型的会自动产生主键策略，其他场景sqltoy根据主键类型和长度自动分配相应的策略方式. 
	     strategy分:sequence\assign\generator 三种策略：
	     sequence 需要指定数据库对应的sequence名称。 
	     assign 为手工赋值
	     generator为指定具体产生策略,目前分:default:22位长度的主键\nanotime:26位纳秒形式\snowflake雪花算法\uuid\redis -->
	<primary-key>
		<table name="SAG_\w+|SYS_\w+" strategy="generator" generator="default" />
		<!--<table name="xxxTABLE" strategy="sequence" sequence="SEQ_XXXX"/> -->
		<!--<table name="sys_staff_info" strategy="generator" generator="snowflake"/> -->
	</primary-key>

	<!-- 基于redis产生有规则的业务主键 -->
	<business-primary-key>
		<!-- 1位购销标志_2位设备分类代码_6位日期_3位流水 (如果当天超过3位会自动扩展到4位) -->
		<!-- 业务主键可以充当实际的主键，如果字段是主键会覆盖原来的主键策略 -->
		<!-- 包含的三个宏: @substr(${field},startIndex,length) @case(value,case1,value1,case2,value2,other) 
			@df(yyMMdd) 日期格式化,如果不需要加入日期可以用@df('') -->
		<!--<table name="SQLTOY_DEVICE_ORDER" column="ORDER_ID"	generator="redis" signature="${psType}@case(${deviceType},PC,PC,NET,NT,OFFICE,OF,SOFTWARE,SF,OT)@day(yyMMdd)"
			related-columns="psType,deviceType" length="12" />-->
	</business-primary-key>

	<!-- 主子表的级联关系 update-cascade:delete 表示对存量数据进行删除操作,也可以写成:ENABLED=0(sql片段,置状态为无效),orderBy 支持级联时排序 -->
	<!--  常规情况下无需配置cascade,也不推荐，这里只是演示说明，不要被误导   -->
	<cascade>
		<!-- <table  name="SAG_DICT_DETAIL"  update-cascade="delete" load="STATUS=1"  orderBy=""/> -->
	</cascade>

	<!-- 数据类型对应关系 
	    native-types表示特定数据库返回的字段类型; 
	    jdbc-type：表示对应jdbc标准的类型(见:java.sql.Types),主要用于vo @Column注解中，设置其类型,方便操作数据库插入或修改时设置类型;
		java-type:表示对应java对象的属性类型(非java.lang的需要将包名写完整便于import) 
		import-types: 可以自行定义需要引入的类型，多个用逗号分隔
		-->
	<type-mapping>
		<!-- 保留1个范例,一般无需配置 -->
		<sql-type native-types="NUMBER,DECIMAL,NUMERIC"	precision="1..8" scale="0" jdbc-type="INTEGER" java-type="Integer" />
		<!-- 增加雪花算法的演示 -->
		<sql-type native-types="BIGINT" jdbc-type="BIGINT" java-type="java.math.BigInteger" />
<!-- 泛型注意xml转义符号，table-field指定具体表和字段; jdbc-type 可以直接填数字(jdbc.Types没有明显区分的类型)-->
		<sql-type table-field="sqltoy_jsontype_showcae.staff_set" native-types="json" jdbc-type="1021" java-type="List&lt;StaffInfoVO&gt;" import-types="com.sqltoy.quickstart.vo.StaffInfoVO" />
	</type-mapping>
</quickvo>
```
