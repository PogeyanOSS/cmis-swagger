package com.pogeyan.swagger.server;

/*import java.io.File;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.util.scan.StandardJarScanner;*/

public class TomcatLauncher {
	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 * @throws Exception
	 *             the exception
	 */
	public static void main(String[] args) throws Exception {
		// Define a folder to hold web application contents.
		/*
		 * String webappDirLocation = "src/main/webapp/"; Tomcat tomcat = new
		 * Tomcat();
		 * 
		 * // Define port number for the web application String webPort =
		 * System.getenv("PORT"); if (webPort == null || webPort.isEmpty()) {
		 * webPort = "8081"; } // Bind the port to Tomcat server
		 * tomcat.setPort(Integer.valueOf(webPort));
		 * 
		 * // Define a web application context. Context context =
		 * tomcat.addWebapp("/", new File(webappDirLocation).getAbsolutePath());
		 * 
		 * // Define and bind web.xml file location. File configFile = new
		 * File(webappDirLocation + "WEB-INF/web.xml");
		 * context.setConfigFile(configFile.toURI().toURL()); // declare an
		 * alternate location for your "WEB-INF/classes" dir: File
		 * additionWebInfClasses = new File("target/classes"); WebResourceRoot
		 * resources = new StandardRoot(context); resources.addPreResources( new
		 * DirResourceSet(resources, "/WEB-INF/classes",
		 * additionWebInfClasses.getAbsolutePath(), "/"));
		 * context.setResources(resources);
		 * 
		 * JarScanner jarScanner = context.getJarScanner(); if (jarScanner
		 * instanceof StandardJarScanner) { ((StandardJarScanner)
		 * jarScanner).setScanAllDirectories(true); ((StandardJarScanner)
		 * jarScanner).setScanAllDirectories(true); }
		 * 
		 * tomcat.start(); tomcat.getServer().await();
		 */
	}
}
