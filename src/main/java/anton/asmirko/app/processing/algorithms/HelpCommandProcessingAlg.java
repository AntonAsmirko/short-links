package anton.asmirko.app.processing.algorithms;

import anton.asmirko.app.model.commands.HelpCommand;
import org.springframework.stereotype.Component;

@Component(HelpCommand.STR_REP)
public class HelpCommandProcessingAlg implements CLICommandProcessingAlg<HelpCommand> {
  @Override
  public void process(HelpCommand cliCommand) {
    System.out.println(
        """
                ShortLinks CLI — инструмент для управления короткими ссылками
                ------------------------------------------------------------

                Использование:
                  java -jar shortlinks.jar <команда> [опции] [аргументы]

                Доступные команды:

                  shorten     \s
                      Создать короткую ссылку из указанного URL.
                      Принимает не обязательный идентификатор пользователя (-u) и исходный URL.
                      Если UUID не задан, то создается новый пользователь.
                      Принимает не обязательные парамметры -ttl -- ttl ссылки, конфигурируемый пользователем
                      в секундах, имеет приоретет ниже глобального ttl, -nq -- максимальное число запросов по
                      ссылке. Если -ttl или -nq не заданы, то считается, что нет ограничений пользователя на число
                      переходов и время жизни ссылки.
                      Возвращает короткий идентификатор, связанный с этой ссылкой.
                      Пример:
                        java -jar shortlinks.jar shorten -u 550e8400-e29b-41d4-a716-446655440000 \\
                            https://yandex.ru/dev/weather/doc/ru/concepts/forecast-rest
                        java -jar shortlinks.jar shorten -u 550e8400-e29b-41d4-a716-446655440000 \\
                            -ttl 1111 \\
                            https://yandex.ru/dev/weather/doc/ru/concepts/forecast-rest
                        java -jar shortlinks.jar shorten -u 550e8400-e29b-41d4-a716-446655440000 \\
                            -nq 10 \\
                            https://yandex.ru/dev/weather/doc/ru/concepts/forecast-rest

                  prune      \s
                      Удалить короткую ссылку.
                      После удаления переход по этой ссылке станет невозможен.
                      Принимает не обязательный идентификатор пользователя (-u) и сокращенный URL.
                      Если UUID не задан, то утилита предложит Вам авторизоваться под учетной записью админа.
                      Пример:
                        java -jar shortlinks.jar prune -u 550e8400-e29b-41d4-a716-446655440000 clck.ru/B
                            Удаление ссылки clck.ru/B пользователя 550e8400-e29b-41d4-a716-446655440000.
                        java -jar shortlinks.jar prune clck.ru/B
                            Удаление сслыки clck.ru/B администратором, необходим пароль администратора,
                            задается в переменной admin.password в файле admin.properties.
                        java -jar shortlinks.jar prune -u 550e8400-e29b-41d4-a716-446655440000
                            Удаление всех ссылок пользователя 550e8400-e29b-41d4-a716-446655440000.

                  open        \s
                      Перейти по сокращенной ссылке.
                      Открывает исходную ссылку в браузере по-умолчанию.
                      Принимает обязательный идентификатор пользователя (-u) и сокращенный URL.
                      Пример:
                        java -jar shortlinks.jar open -u 550e8400-e29b-41d4-a716-446655440000 clck.ru/B
                            Открывает в браузере исходную ссылку clck.ru/B.

                 ttlGlobal
                      Задать глобальный ttl жизни ссылок в секундах.
                      Принимает обязательны параметр ttl, требует выполнения под учетной записью
                      администратора.
                      Пример
                      java -jar shortlinks.jar ttlGlobal 1111111
                            Задает глобальный ttl жизни ссылок равным 1111111 секундам.

                  help        \s
                      Показать справку по программе.
                      Пример:
                        java -jar shortlinks.jar help

                ------------------------------------------------------------
                """);
  }
}
