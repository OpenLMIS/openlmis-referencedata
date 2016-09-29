package org.openlmis.referencedata.validate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Arrays;
import java.util.Collection;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(Parameterized.class)
public class RequisitionGroupValidatorParametrizedTest extends BaseValidatorTest {

  @Mock
  private SupervisoryNodeRepository supervisoryNodes;

  @InjectMocks
  private Validator validator = new RequisitionGroupValidator();

  private String expectedValue;
  private RequisitionGroup requisitionGroup;
  private Errors errors;

  /**
   * Create new instance of parametrized test.
   *
   * @param value expected value for code or name property of {@link RequisitionGroup}
   */
  public RequisitionGroupValidatorParametrizedTest(String value) {
    expectedValue = value;

    requisitionGroup = new RequisitionGroup();
    requisitionGroup.setCode("TestRequisitionGroupCode");
    requisitionGroup.setName("TestRequisitionGroupName");

    errors = new BeanPropertyBindingResult(requisitionGroup, "requisitionGroup");
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
    requisitionGroup.setCode(expectedValue);

    validator.validate(requisitionGroup, errors);

    assertErrorMessage(errors, "code", "The Requisition Group Code is required");
  }

  @Test
  public void shouldRejectIfNameIsEmpty() throws Exception {
    requisitionGroup.setName(expectedValue);

    validator.validate(requisitionGroup, errors);

    assertErrorMessage(errors, "name", "The requisition Group Name is required");
  }

}
