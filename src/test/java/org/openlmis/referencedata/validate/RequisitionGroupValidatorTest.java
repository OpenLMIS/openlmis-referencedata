package org.openlmis.referencedata.validate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.repository.RequisitionGroupRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class RequisitionGroupValidatorTest {

  @Mock
  private SupervisoryNodeRepository supervisoryNodes;

  @Mock
  private RequisitionGroupRepository requisitionGroups;

  @InjectMocks
  private Validator validator = new RequisitionGroupValidator();

  private RequisitionGroup requisitionGroup;
  private Errors errors;

  @Before
  public void setUp() throws Exception {
    SupervisoryNode supervisoryNode = new SupervisoryNode();
    supervisoryNode.setCode("TestSupervisoryNodeCode");

    requisitionGroup = new RequisitionGroup();
    requisitionGroup.setCode("TestRequisitionGroupCode");
    requisitionGroup.setName("TestRequisitionGroupName");
    requisitionGroup.setDescription("TestRequisitionGroupDescription");
    requisitionGroup.setSupervisoryNode(supervisoryNode);

    errors = new BeanPropertyBindingResult(requisitionGroup, "requisitionGroup");
  }

  @Test
  public void shouldNotFindErrors() throws Exception {
    doReturn(mock(SupervisoryNode.class))
        .when(supervisoryNodes)
        .findByCode("TestSupervisoryNodeCode");

    validator.validate(requisitionGroup, errors);
    assertThat(errors.getErrorCount(), is(equalTo(0)));
  }

  @Test
  public void shouldRejectIfCodeIsEmpty() throws Exception {
    String[] strings = new String[]{null, "", "  "};

    for (int i = 0, length = strings.length; i < length; ++i) {
      String code = strings[i];
      requisitionGroup.setCode(code);

      validator.validate(requisitionGroup, errors);
      assertThat(errors.getErrorCount(), is(equalTo(i + 1)));
      assertThat(errors.getFieldError("code"), is(notNullValue()));
    }
  }

  @Test
  public void shouldRejectIfNameIsEmpty() throws Exception {
    String[] strings = new String[]{null, "", "  "};

    for (int i = 0, length = strings.length; i < length; ++i) {
      String name = strings[i];
      requisitionGroup.setName(name);

      validator.validate(requisitionGroup, errors);
      assertThat(errors.getErrorCount(), is(equalTo(i + 1)));
      assertThat(errors.getFieldError("name"), is(notNullValue()));
    }
  }

  @Test
  public void shouldRejectIfSupervisoryNodeIsEmpty() throws Exception {
    requisitionGroup.setSupervisoryNode(null);

    validator.validate(requisitionGroup, errors);
    assertThat(errors.getErrorCount(), is(equalTo(1)));
    assertThat(errors.getFieldError("supervisoryNode"), is(notNullValue()));
  }

  @Test
  public void shouldRejectIfRequisitionGroupCodeIsDuplicated() throws Exception {
    doReturn(mock(RequisitionGroup.class))
        .when(requisitionGroups)
        .findByCode(requisitionGroup.getCode());

    doReturn(mock(SupervisoryNode.class))
        .when(supervisoryNodes)
        .findByCode("TestSupervisoryNodeCode");

    validator.validate(requisitionGroup, errors);
    assertThat(errors.getErrorCount(), is(equalTo(1)));
    assertThat(errors.getFieldError("code"), is(notNullValue()));
  }

  @Test
  public void shouldRejectIfSupervisoryNodeCanNotBeFound() throws Exception {
    doReturn(null)
        .when(supervisoryNodes)
        .findByCode(anyString());

    validator.validate(requisitionGroup, errors);
    assertThat(errors.getErrorCount(), is(equalTo(1)));
    assertThat(errors.getFieldError("supervisoryNode"), is(notNullValue()));
  }
}
