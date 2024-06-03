package org.openlmis.referencedata.validate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openlmis.referencedata.util.messagekeys.UnitOfOrderableMessageKeys.DISPLAY_ORDER;
import static org.openlmis.referencedata.util.messagekeys.UnitOfOrderableMessageKeys.FACTOR;
import static org.openlmis.referencedata.util.messagekeys.UnitOfOrderableMessageKeys.NAME;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.UnitOfOrderable;
import org.openlmis.referencedata.dto.UnitOfOrderableDto;
import org.openlmis.referencedata.testbuilder.UnitOfOrderableBuilder;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

public class UnitOfOrderableValidatorTest {

  private UnitOfOrderableValidator validator = new UnitOfOrderableValidator();

  private UnitOfOrderableDto unitOfOrderableDto;
  private Errors errors;

  @Before
  public void setUp() {

    UnitOfOrderable unitOfOrderable = new UnitOfOrderableBuilder()
        .withName("testName")
        .withDescription("sample description")
        .withDisplayOrder(0)
        .withFactor(10)
        .build();
    unitOfOrderableDto = UnitOfOrderableDto.newInstance(unitOfOrderable);

    errors = new BeanPropertyBindingResult(unitOfOrderableDto, "unitOfOrderableDto");
  }

  @Test
  public void shouldNotFindErrorsWhenUnitOfOrderableIsValid() {
    //given
    //when
    validator.validate(unitOfOrderableDto, errors);

    //then
    assertThat(errors.getErrorCount()).isZero();
  }

  @Test
  public void shouldNotRejectIfDisplayOrderAndFactorArePositiveOrZero() {
    //given
    //when
    validator.validate(unitOfOrderableDto, errors);

    //then
    assertThat(errors.hasFieldErrors(DISPLAY_ORDER)).isFalse();
    assertThat(errors.hasFieldErrors(FACTOR)).isFalse();
  }

  @Test
  public void shouldRejectIfNameIsNotGiven() {
    //given
    unitOfOrderableDto.setName(null);

    //when
    validator.validate(unitOfOrderableDto, errors);

    //then
    assertThat(errors.hasFieldErrors(NAME)).isTrue();
  }

  @Test
  public void shouldRejectIfDisplayOrderIsNegative() {
    //given
    unitOfOrderableDto.setDisplayOrder(-1);

    //when
    validator.validate(unitOfOrderableDto, errors);

    //then
    assertThat(errors.hasFieldErrors(DISPLAY_ORDER)).isTrue();
  }

  @Test
  public void shouldRejectIfFactorIsNegative() {
    //given
    unitOfOrderableDto.setFactor(-1);

    //when
    validator.validate(unitOfOrderableDto, errors);

    //then
    assertThat(errors.hasFieldErrors(FACTOR)).isTrue();
  }
}
