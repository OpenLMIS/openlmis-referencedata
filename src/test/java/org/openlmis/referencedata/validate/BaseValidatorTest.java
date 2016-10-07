package org.openlmis.referencedata.validate;

import org.springframework.validation.Errors;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public abstract class BaseValidatorTest {

  protected void assertErrorMessage(Errors errors, String field, String expectedMessage) {
    assertThat("There is no errors for field: " + field, errors.hasFieldErrors(field), is(true));

    boolean match = errors.getFieldErrors(field)
        .stream()
        .anyMatch(e -> e.getField().equals(field) && e.getDefaultMessage().equals(expectedMessage));

    assertThat("There is no error with default message: " + expectedMessage, match, is(true));
  }

}
