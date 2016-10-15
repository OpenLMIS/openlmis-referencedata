package org.openlmis.referencedata.validate;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.openlmis.referencedata.validate.RequisitionGroupValidator.CODE;
import static org.openlmis.referencedata.validate.RequisitionGroupValidator.CODE_IS_REQUIRED;
import static org.openlmis.referencedata.validate.RequisitionGroupValidator.NAME;
import static org.openlmis.referencedata.validate.RequisitionGroupValidator.NAME_IS_REQUIRED;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.dto.RequisitionGroupDto;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class RequisitionGroupValidatorParametrizedTest extends BaseValidatorTest {

  @Mock
  private SupervisoryNodeRepository supervisoryNodes;

  @InjectMocks
  private Validator validator = new RequisitionGroupValidator();

  private String expectedValue;
  private RequisitionGroupDto requisitionGroupDto;
  private Errors errors;

  /**
   * Create new instance of parametrized test.
   *
   * @param value expected value for code or name property of {@link RequisitionGroup}
   */
  public RequisitionGroupValidatorParametrizedTest(String value) {
    expectedValue = value;

    RequisitionGroup requisitionGroup = new RequisitionGroup();
    requisitionGroup.setCode("TestRequisitionGroupCode");
    requisitionGroup.setName("TestRequisitionGroupName");

    requisitionGroupDto = new RequisitionGroupDto();
    requisitionGroup.export(requisitionGroupDto);

    errors = new BeanPropertyBindingResult(requisitionGroupDto, "requisitionGroup");
  }

  /**
   * This method is used to pass arguments from each array into constructor.
   *
   * @return list of array that contains data that will be passed into constructor.
   */
  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {null},
        {""},
        {"  "}
    });

  }

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    doReturn(mock(SupervisoryNode.class))
        .when(supervisoryNodes)
        .findByCode("TestSupervisoryNodeCode");
  }

  @Test
  public void shouldRejectIfCodeIsEmpty() throws Exception {
    requisitionGroupDto.setCode(expectedValue);

    validator.validate(requisitionGroupDto, errors);

    assertErrorMessage(errors, CODE, CODE_IS_REQUIRED);
  }

  @Test
  public void shouldRejectIfNameIsEmpty() throws Exception {
    requisitionGroupDto.setName(expectedValue);

    validator.validate(requisitionGroupDto, errors);

    assertErrorMessage(errors, NAME, NAME_IS_REQUIRED);
  }

}
