package anton.asmirko.app.model.commands;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PruneCommandKeyTest {

  @Test
  void userKey_acceptsValidUuid() {
    assertTrue(PruneCommand.Key.USER.isValid("123e4567-e89b-12d3-a456-426614174000"));
  }

  @Test
  void userKey_acceptsAdminLiteral() {
    assertTrue(PruneCommand.Key.USER.isValid("admin"));
  }

  @Test
  void userKey_rejectsInvalidUuidFormat() {
    assertFalse(PruneCommand.Key.USER.isValid("123e4567e89b12d3a456426614174000")); // без дефисов
    assertFalse(
        PruneCommand.Key.USER.isValid("123e4567-e89b-12d3-a456-42661417400")); // слишком короткий
    assertFalse(
        PruneCommand.Key.USER.isValid("123e4567-e89b-12d3-a456-42661417400Z")); // неверный символ
    assertFalse(PruneCommand.Key.USER.isValid("Admin")); // неверный регистр
    assertFalse(PruneCommand.Key.USER.isValid("user-uuid"));
    assertFalse(PruneCommand.Key.USER.isValid(""));
  }

  @Test
  void getArgCount_returnsOne_whenPatternIsPresent() {
    assertEquals(1, PruneCommand.Key.USER.getArgCount());
  }

  @Test
  void toString_returnsKeyRepresentation() {
    assertEquals("-u", PruneCommand.Key.USER.toString());
  }
}
