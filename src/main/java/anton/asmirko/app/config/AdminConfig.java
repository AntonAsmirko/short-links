package anton.asmirko.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:admin.properties")
public class AdminConfig {

  @Value("${admin.password}")
  public String password;
}
