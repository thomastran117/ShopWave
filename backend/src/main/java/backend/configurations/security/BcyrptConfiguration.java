package backend.configurations.security;

import backend.configurations.environment.EnvironmentSetting;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BcyrptConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder(EnvironmentSetting env) {
        int strength = env.getSecurity().getBcryptStrength();
        return new BCryptPasswordEncoder(strength);
    }
}
