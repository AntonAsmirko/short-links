package anton.asmirko;

import anton.asmirko.app.ShortLinksApp;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
  public static void main(String[] args) {
    System.setProperty("java.util.logging.config.file", "build/resources/main/logging.properties");
    ApplicationContext context = new AnnotationConfigApplicationContext("anton.asmirko.app");
    ShortLinksApp shortLinksApp = context.getBean(ShortLinksApp.class);
    shortLinksApp.run(args);
  }
}
