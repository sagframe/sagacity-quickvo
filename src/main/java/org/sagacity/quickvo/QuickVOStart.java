package org.sagacity.quickvo;

import static java.lang.System.out;

import java.util.logging.Logger;

import org.sagacity.quickvo.config.XMLConfigLoader;
import org.sagacity.quickvo.model.ConfigModel;
import org.sagacity.quickvo.utils.FreemarkerUtil;
import org.sagacity.quickvo.utils.LoggerUtil;

/**
 * @project sagacity-quickvo
 * @description 快速从数据库生成VO以及VO<-->PO 映射的mapping工具
 * @author chenrenfei <a href="mailto:zhongxuchen@hotmail.com">联系作者</a>
 * @version id:QuickVOStart.java,Revision:v2.0,Date:2009-04-15
 */
public class QuickVOStart {
	private static Logger logger = LoggerUtil.getLogger();

	/**
	 * 开始生成文件
	 * 
	 * @throws Exception
	 */
	public void doStart() {
		try {
			out.println("\n\n=========    welcome use sagacity-quickvo-4.17.2     ==========");
			out.println("/*----遇到问题请关注日志提示，最常见错误有2个:     ----------------------------*/\n"
					+ "/*-1、没有匹配到表: include表达式或 schema和catalog配置错误(含大小写)         \n"
					+ "/*-2、VO中字段出现重复字段，错误就是schema和catalog配置问题                           \n"
					+ "/*---------------------------------------------------------------*/\n\n ");
			Thread.sleep(1500);

			// 解析配置文件
			ConfigModel configModel = XMLConfigLoader.parse();
			TaskController.setConfigModel(configModel);
			// 创建vo和vof
			TaskController.create();
			FreemarkerUtil.destory();
			// 发生表字段重复现象
			if (Constants.hasRepeatField) {
				Thread.sleep(1000);
				logger.info("\n\n\n/*------------任务已结束,生成的VO对象中有重复字段致命性错误,具体原因:---------------------------*/\n"
						+ "/*-  就是datasource中的schema 和 catalog 配置问题,导致在多用户多实例环境下隔离不正确!\n"
						+ "/*--------------------------处理协助----------------------------------\n"
						+ "/*-1、比如mysql场景下可以设置schema和catalog的值一致进行尝试!\n"
						+ "/*-2、datasource中是可以配置schema和catalog属性的，如果没有可以自行调整和尝试赋值(不同数据库一般跟用户名，库名、tablespace等相关)!\n"
						+ "/*-3、取表字段的内部原理:conn.getMetaData().getColumns(catalog, schema, tableName, \"%\");\n"
						+ "/*-------------------------------------------------------------*/");
				Thread.sleep(10000);
			} else {
				logger.info("成功完成vo以及vo<-->po映射类的生成!");
			}
		} catch (ClassNotFoundException e) {
			logger.info("数据库驱动加载失败!请将数据库驱动jar文件放到当前目录libs目录下!" + e.getMessage());
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
	}

	/**
	 * 主调度控制
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		QuickVOStart quickStart = new QuickVOStart();
		if (args != null && args.length > 0) {
			Constants.QUICK_CONFIG_FILE = args[0];
		}
		String baseDir;
		if (args != null && args.length > 1) {
			baseDir = args[1];
		} else {
			baseDir = System.getProperty("user.dir");
		}
		Constants.BASE_LOCATE = baseDir;
		// 代码调试时使用(真实场景不起作用,注意pom中要增加对应数据库驱动才可以运行)
		if (args == null || args.length == 0) {
			Constants.BASE_LOCATE = "D:\\personal\\sqltoy\\sqltoy-postgresql\\tools\\quickvo";
			Constants.QUICK_CONFIG_FILE = "quickvo.xml";
		}
		// 开始根据数据库产生VO文件
		quickStart.doStart();
	}
}
