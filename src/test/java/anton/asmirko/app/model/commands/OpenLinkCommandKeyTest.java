package anton.asmirko.app.model.commands;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OpenLinkCommandKeyTest {

  @Test
  void userKey_acceptsValidUuid() {
    assertTrue(OpenLinkCommand.Key.USER.isValid("123e4567-e89b-12d3-a456-426614174000"));
  }

  @Test
  void userKey_acceptsAdminLiteral() {
    assertTrue(OpenLinkCommand.Key.USER.isValid("admin"));
  }

  @Test
  void userKey_rejectsInvalidUuidFormat() {
    assertFalse(OpenLinkCommand.Key.USER.isValid("123e4567e89b12d3a456426614174000")); // no dashes
    assertFalse(
        OpenLinkCommand.Key.USER.isValid("123e4567-e89b-12d3-a456-42661417400")); // too short
    assertFalse(
        OpenLinkCommand.Key.USER.isValid("123e4567-e89b-12d3-a456-42661417400Z")); // invalid char
    assertFalse(OpenLinkCommand.Key.USER.isValid("Admin")); // case sensitive
    assertFalse(OpenLinkCommand.Key.USER.isValid("user-uuid"));
    assertFalse(OpenLinkCommand.Key.USER.isValid(""));
  }

  @Test
  void getArgCount_returnsOne_whenPatternPresent() {
    assertEquals(1, OpenLinkCommand.Key.USER.getArgCount());
  }

  @Test
  void toString_returnsCorrectKeyRepresentation() {
    assertEquals("-u", OpenLinkCommand.Key.USER.toString());
  }
}
