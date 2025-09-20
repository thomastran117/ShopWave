package backend.resources;

import backend.configs.EnvConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class AppDatabase {

    private final EnvConfig env;

    public AppDatabase(EnvConfig env) {
        this.env = env;
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(env.getDbUrl());
        dataSource.setUsername(env.getDbUser());
        dataSource.setPassword(env.getDbPassword());
        return dataSource;
    }
}
