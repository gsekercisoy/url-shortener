package com.gsekercisoy.urlshortener;

import com.gsekercisoy.urlshortener.service.CodeGenerator;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodeGeneratorTest {

  private final CodeGenerator generator = new CodeGenerator();

  @Test
  void generatesBase62CodesOfLengthSeven() {
    String code = generator.generate();
    assertEquals(7, code.length());
    assertTrue(code.matches("[A-Za-z0-9]{7}"));
  }

  @Test
  void generatesUniqueCodes() {
    Set<String> seen = new HashSet<>();
    for (int i = 0; i < 1000; i++) {
      seen.add(generator.generate());
    }
    assertTrue(seen.size() > 990);
  }
}
