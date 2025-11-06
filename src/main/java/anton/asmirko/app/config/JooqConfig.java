package anton.asmirko.app.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class JooqConfig {

  private final SqliteConfig sqliteConfig;

  @Bean
  public DSLContext dsl() throws SQLException {
    Connection conn =
        DriverManager.getConnection(String.format("jdbc:sqlite:%s", sqliteConfig.path));
    return DSL.using(conn, SQLDialect.SQLITE, new Settings().withExecuteLogging(false));
  }
}
