package com.alexswd.userservice.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.hibernate.HibernateTransactionManager;
import org.springframework.orm.jpa.hibernate.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableWebMvc
@EnableTransactionManagement
@ComponentScan("com.alexswd.userservice")
@PropertySource("classpath:application.properties")
public class WebConfig implements WebMvcConfigurer {

  private final Environment environment;

  public WebConfig(Environment environment) {
    this.environment = environment;
  }

  @Bean
  public DataSource dataSource() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName(environment.getRequiredProperty("db.driver"));
    dataSource.setUrl(environment.getRequiredProperty("db.url"));
    dataSource.setUsername(environment.getRequiredProperty("db.username"));
    dataSource.setPassword(environment.getRequiredProperty("db.password"));
    return dataSource;
  }

  @Bean
  public LocalSessionFactoryBean sessionFactory(DataSource dataSource) {
    LocalSessionFactoryBean factory = new LocalSessionFactoryBean();
    factory.setDataSource(dataSource);
    factory.setPackagesToScan("com.alexswd.userservice.entity");
    Properties properties = new Properties();
    properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
    properties.put("hibernate.hbm2ddl.auto", environment.getRequiredProperty("db.ddl-auto"));
    properties.put("hibernate.show_sql", environment.getRequiredProperty("db.show-sql"));
    factory.setHibernateProperties(properties);
    return factory;
  }

  @Bean
  public HibernateTransactionManager transactionManager(LocalSessionFactoryBean sessionFactory) {
    return new HibernateTransactionManager(sessionFactory.getObject());
  }

  @Bean
  public ObjectMapper objectMapper() {
    return Jackson2ObjectMapperBuilder.json().findModulesViaServiceLoader(true).build();
  }
}
