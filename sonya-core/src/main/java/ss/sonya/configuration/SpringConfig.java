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

import javax.sql.DataSource;
import javax.validation.Validator;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBuilder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    @Bean(name = "dataSource")
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
     * Hibernate session factory.
     * @param dataSource - data source.
     * @return - session factory.
     */
    @Bean(name = "sessionFactory")
    public SessionFactory getSessionFactory(final DataSource dataSource) {
        LocalSessionFactoryBuilder sessionBuilder =
                new LocalSessionFactoryBuilder(dataSource());
        sessionBuilder.scanPackages("ss.sonya.entity");
        for (String key : SonyaConfig.getKeys()) {
            if (key.startsWith("hibernate")) {
                LOG.info("key [" + key + "] value [" + SonyaConfig.setting(
                        SonyaProperty.getConstantByKey(key)) + "]");
                sessionBuilder.setProperty(key,
                        SonyaConfig.setting(
                                SonyaProperty.getConstantByKey(key)));
            }
        }
        return sessionBuilder.buildSessionFactory();
    }
    /**
     * Transaction manager.
     * @param sessionFactory - session factory.
     * @return - hibernate transaction manager.
     */
    @Bean(name = "transactionManager")
    public HibernateTransactionManager getTransactionManager(
            final SessionFactory sessionFactory) {
        HibernateTransactionManager transactionManager =
                new HibernateTransactionManager(sessionFactory);
        return transactionManager;
    }
    /**
     * Hibernate validator.
     * @return - validator.
     */
    @Bean(name = "validator")
    public Validator localValidatorFactoryBean() {
        return new LocalValidatorFactoryBean();
    }
    /**
     * Password encoder.
     * @return - password encoder.
     */
    @Bean(name = "passwordEncoder")
    public PasswordEncoder passwordEncoder() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder;
    }
}
