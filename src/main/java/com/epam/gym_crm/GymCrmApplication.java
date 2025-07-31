package com.epam.gym_crm;

import java.io.File;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import com.epam.gym_crm.config.AppConfig;

public class GymCrmApplication {

	private static final Logger logger = LoggerFactory.getLogger(GymCrmApplication.class);

	public static void main(String[] args) {
		logger.info("--- Embedded Tomcat ve Spring Web Uygulaması Başlatılıyor ---");

		Tomcat tomcat = new Tomcat();
		tomcat.setPort(8080);
		tomcat.getConnector();
		AnnotationConfigWebApplicationContext applicationContext = null;
		try {

			logger.info("Spring Application Context oluşturuluyor...");
			applicationContext = new AnnotationConfigWebApplicationContext();
			applicationContext.register(AppConfig.class);

			logger.info("DispatcherServlet oluşturuluyor...");

			DispatcherServlet dispatcherServlet = new DispatcherServlet(applicationContext);
			logger.info("DispatcherServlet başarıyla oluşturuldu.");

			String tempDir = createTempDirectory("tomcat.gymcrm.temp");

			org.apache.catalina.Context webContext = tomcat.addContext("", tempDir);
			logger.info("Tomcat web context '{}' ve belge tabanı '{}' ile eklendi.", webContext.getPath(),
					webContext.getDocBase());

			FilterDef loggingFilterDef = new FilterDef();
			loggingFilterDef.setFilterName("loggingFilter");
			loggingFilterDef.setFilterClass(DelegatingFilterProxy.class.getName());
			loggingFilterDef.addInitParameter("targetBeanName", "requestLoggingFilter");
			webContext.addFilterDef(loggingFilterDef);

			FilterMap loggingFilterMap = new FilterMap();
			loggingFilterMap.setFilterName("loggingFilter");
			loggingFilterMap.addURLPattern("/*");

			webContext.addFilterMap(loggingFilterMap);

			FilterDef sessionFilterDef = new FilterDef();
			sessionFilterDef.setFilterName("sessionFilter");
			sessionFilterDef.setFilterClass(DelegatingFilterProxy.class.getName());
			sessionFilterDef.addInitParameter("targetBeanName", "sessionManagementFilter");

			webContext.addFilterDef(sessionFilterDef);

			FilterMap sessionFilterMap = new FilterMap();
			sessionFilterMap.setFilterName("sessionFilter");
			sessionFilterMap.addURLPattern("/*");
			webContext.addFilterMap(sessionFilterMap);

			logger.info("DelegatingFilterProxy 'loggingFilter' başarıyla tanımlandı ve '/*' URL'ine eşlendi.");

			Tomcat.addServlet(webContext, "dispatcherServlet", dispatcherServlet);
			webContext.addServletMappingDecoded("/", "dispatcherServlet");
			logger.info("DispatcherServlet '/' URL'ine eşlendi.");

			logger.info("Tomcat başlatılıyor...");
			tomcat.start();
			logger.info("--- Embedded Tomcat Başlatıldı ve 8080 Portunda Dinlemede ---");
			logger.info("Uygulama çalışır durumda. İstekler bekleniyor...");

			tomcat.getServer().await();

		} catch (LifecycleException e) {
			logger.error("Tomcat başlatılırken bir yaşam döngüsü hatası oluştu: {}", e.getMessage(), e);
		} catch (Exception e) {
			logger.error("Uygulama başlatılırken veya çalışırken beklenmedik bir hata oluştu: {}", e.getMessage(), e);
		} finally {
			if (applicationContext != null && applicationContext.isActive()) {
				logger.info("Spring Application Context kapatılıyor.");
				applicationContext.close();
			}
			logger.info("--- Embedded Tomcat ve Spring Web Uygulaması Kapatıldı ---");
		}
	}

	private static String createTempDirectory(String prefix) {
		try {
			File tempDir = File.createTempFile(prefix, "");
			if (!tempDir.delete()) {
				logger.warn("Geçici dosya silinemedi: {}", tempDir.getAbsolutePath());
			}
			if (!tempDir.mkdir()) {
				throw new RuntimeException("Geçici dizin oluşturulamadı: " + tempDir.getAbsolutePath());
			}
			tempDir.deleteOnExit();
			logger.info("Geçici Tomcat çalışma dizini oluşturuldu: {}", tempDir.getAbsolutePath());
			return tempDir.getAbsolutePath();
		} catch (Exception e) {
			logger.error("Geçici dizin oluşturulurken hata oluştu!", e);
			throw new RuntimeException("Geçici dizin oluşturulamadı!", e);
		}
	}
}