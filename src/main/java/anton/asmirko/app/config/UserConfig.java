package anton.asmirko.app.config;

import anton.asmirko.app.model.user.UserFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserConfig {

  @Bean
  public UserFactory userFactory(AdminConfig adminConfig) {
    return new UserFactory(adminConfig);
  }
}
