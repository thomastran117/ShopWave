package backend.configurations.resources;

import backend.configurations.environment.EnvironmentSetting;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class AppDatabase {

    private final EnvironmentSetting env;

    public AppDatabase(EnvironmentSetting env) {
        this.env = env;
    }

    @Bean
    public DataSource dataSource() {
        EnvironmentSetting.Database db = env.getDatabase();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(db.getUrl());
        config.setUsername(db.getUsername());
        config.setPassword(db.getPassword());
        config.setDriverClassName(db.getDriverClassName());
        config.setMaximumPoolSize(db.getMaximumPoolSize());
        config.setMinimumIdle(db.getMinimumIdle());
        config.setConnectionTimeout(db.getConnectionTimeout());
        config.setIdleTimeout(db.getIdleTimeout());
        config.setPoolName("EasyFoodHikariPool");

        return new HikariDataSource(config);
    }
}
