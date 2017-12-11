package com.git.listener;

import org.apache.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ContextListener implements ServletContextListener{
	public static final Logger logger = Logger.getLogger(ContextListener.class);
	
	public void contextInitialized(ServletContextEvent sce) {
		logger.error("启动服务器");
	}

	public void contextDestroyed(ServletContextEvent sce) {
		logger.error("关闭服务器");
	}

}
