package anton.asmirko.app.model.commands;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ShortenLinkCommandKeyTest {

  @Test
  void userKey_acceptsValidUuid() {
    assertTrue(ShortenLinkCommand.Key.USER.isValid("123e4567-e89b-12d3-a456-426614174000"));
  }

  @Test
  void userKey_acceptsAdminLiteral() {
    assertTrue(ShortenLinkCommand.Key.USER.isValid("admin"));
  }

  @Test
  void userKey_rejectsInvalidUuidFormat() {
    assertFalse(ShortenLinkCommand.Key.USER.isValid("123e4567-e89b-12d3-a456-42661417400Z"));
    assertFalse(ShortenLinkCommand.Key.USER.isValid("123e4567-e89b-12d3-a456-42661417400"));
    assertFalse(ShortenLinkCommand.Key.USER.isValid("some_user"));
  }

  @Test
  void ttlKey_acceptsPositiveNumbers() {
    assertTrue(ShortenLinkCommand.Key.TTL.isValid("1"));
    assertTrue(ShortenLinkCommand.Key.TTL.isValid("3600"));
  }

  @Test
  void ttlKey_rejectsZeroOrNegativeOrNonNumeric() {
    assertFalse(ShortenLinkCommand.Key.TTL.isValid("0"));
    assertFalse(ShortenLinkCommand.Key.TTL.isValid("-5"));
    assertFalse(ShortenLinkCommand.Key.TTL.isValid("abc"));
    assertFalse(ShortenLinkCommand.Key.TTL.isValid(""));
  }

  @Test
  void numQueriesKey_acceptsPositiveNumbers() {
    assertTrue(ShortenLinkCommand.Key.NUM_QUERIES.isValid("5"));
    assertTrue(ShortenLinkCommand.Key.NUM_QUERIES.isValid("10000"));
  }

  @Test
  void numQueriesKey_rejectsInvalidValues() {
    assertFalse(ShortenLinkCommand.Key.NUM_QUERIES.isValid("0"));
    assertFalse(ShortenLinkCommand.Key.NUM_QUERIES.isValid("-1"));
    assertFalse(ShortenLinkCommand.Key.NUM_QUERIES.isValid("five"));
    assertFalse(ShortenLinkCommand.Key.NUM_QUERIES.isValid(""));
  }
}
