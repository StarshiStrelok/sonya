/*
 * Copyright (C) 2017 ss
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ss.sonya.configuration;

import java.util.Properties;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import javax.validation.Validator;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import ss.sonya.constants.SonyaProperty;

/**
 * Spring configuration.
 * @author ss
 */
@Configuration
@EnableTransactionManagement
@ComponentScan("ss.sonya.inject")
public class SpringConfig {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(SpringConfig.class);
    /**
     * Data source.
     * @return - data source.
     */
    @Bean
    public DataSource dataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(
                SonyaConfig.setting(SonyaProperty.DS_DRIVER));
        dataSource.setUrl(SonyaConfig.setting(SonyaProperty.DS_URL));
        dataSource.setUsername(SonyaConfig.setting(SonyaProperty.DS_USER));
        dataSource.setPassword(SonyaConfig.setting(SonyaProperty.DS_PASSWORD));
        return dataSource;
    }
    /**
     * Entity manager factory.
     * @return entity manager.
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em =
                new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan(new String[] {
            "ss.sonya.entity"
        });
        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        Properties props = new Properties();
        for (String key : SonyaConfig.getKeys()) {
            if (key.startsWith("hibernate")) {
                LOG.info("key [" + key + "] value [" + SonyaConfig.setting(
                        SonyaProperty.getConstantByKey(key)) + "]");
                props.setProperty(key, SonyaConfig.setting(
                        SonyaProperty.getConstantByKey(key)));
            }
        }
        em.setJpaProperties(props);
        return em;
    }
    /**
     * Transaction manager.
     * @param emf entity manager factory.
     * @return transaction manager.
     */
    @Bean
    public PlatformTransactionManager transactionManager(
            final EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }
    /**
     * Persistence exception translator.
     * @return translation processor.
     */
    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }
    /**
     * Hibernate validator.
     * @return - validator.
     */
    @Bean
    public Validator localValidatorFactoryBean() {
        return new LocalValidatorFactoryBean();
    }
    /**
     * Password encoder.
     * @return - password encoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder;
    }
}
