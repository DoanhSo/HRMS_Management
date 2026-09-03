package com.ng_doanh.hr_management_system.common.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(prefix = "spring.flyway", name = "enabled", matchIfMissing = true)
public class FlywayConfig {

    @Bean(name = "flyway")
    public Flyway flyway(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(
                        "classpath:db/migration/auth",
                        "classpath:db/migration/employee",
                        "classpath:db/migration/department",
                        "classpath:db/migration/position",
                        "classpath:db/migration/attendance",
                        "classpath:db/migration/leave",
                        "classpath:db/migration/payroll",
                        "classpath:db/migration/audit",
                        "classpath:db/migration/notification",
                        "classpath:db/migration/seed"
                )
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .load();
        flyway.repair();
        flyway.migrate();
        return flyway;
    }

    @Bean
    public static BeanFactoryPostProcessor entityManagerDependsOnFlywayPostProcessor() {
        return (ConfigurableListableBeanFactory beanFactory) -> {
            for (String name : beanFactory.getBeanNamesForType(jakarta.persistence.EntityManagerFactory.class)) {
                if (beanFactory.containsBeanDefinition(name)) {
                    beanFactory.getBeanDefinition(name).setDependsOn("flyway");
                }
            }
        };
    }
}
