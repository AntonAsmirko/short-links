package anton.asmirko.app.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class Base62EncodeTest {

  @Test
  void encode_returnsZero_whenInputIsZero() {
    assertEquals("0", Base62.encode(0));
  }

  @Test
  void encode_encodesSingleDigitNumbersCorrectly() {
    for (int i = 0; i <= 9; i++) {
      assertEquals(String.valueOf(i), Base62.encode(i));
    }
    assertEquals("A", Base62.encode(10));
    assertEquals("Z", Base62.encode(35));
    assertEquals("a", Base62.encode(36));
    assertEquals("z", Base62.encode(61));
  }

  @Test
  void encode_encodesValuesCrossingBaseBoundary() {
    assertEquals("10", Base62.encode(62));
    assertEquals("11", Base62.encode(63));
    assertEquals("20", Base62.encode(124));
  }

  @Test
  void encode_encodesLargerNumbersCorrectly() {
    assertEquals("ZZ", Base62.encode(35 * 62 + 35));
    assertEquals("100", Base62.encode(62 * 62));
    assertEquals("zz", Base62.encode(61 * 62 + 61));
  }

  @Test
  void encode_encodesNegativeNumbersWithMinusSign() {
    assertEquals("-1", Base62.encode(-1));
    assertEquals("-10", Base62.encode(-62));
    assertTrue(Base62.encode(-9999).startsWith("-"));
  }

  @Test
  void encode_producesUniqueStringsForDifferentInputs() {
    String a = Base62.encode(12345);
    String b = Base62.encode(12346);
    assertNotEquals(a, b);
  }
}
