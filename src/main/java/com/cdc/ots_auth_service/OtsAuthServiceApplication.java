package com.cdc.ots_auth_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class OtsAuthServiceApplication {
	private static final Logger LOG = LoggerFactory.getLogger(OtsAuthServiceApplication.class);

	@Value("${spring.datasource.url}")
	private String datasourceUrl;

	@PostConstruct
	public void logDbConnectionUrl() {
		LOG.info("Connecting to DB with JDBC URL: {}", datasourceUrl);
	}

	public static void main(String[] args) {
		SpringApplication.run(OtsAuthServiceApplication.class, args);
	}

}
