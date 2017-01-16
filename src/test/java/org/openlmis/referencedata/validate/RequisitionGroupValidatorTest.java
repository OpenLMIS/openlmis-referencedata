package org.openlmis.referencedata.validate;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.openlmis.referencedata.validate.RequisitionGroupValidator.CODE;
import static org.openlmis.referencedata.validate.RequisitionGroupValidator.DESCRIPTION;
import static org.openlmis.referencedata.validate.RequisitionGroupValidator.MEMBER_FACILITIES;
import static org.openlmis.referencedata.validate.RequisitionGroupValidator.NAME;
import static org.openlmis.referencedata.validate.RequisitionGroupValidator.SUPERVISORY_NODE;

import com.google.common.collect.Sets;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.dto.FacilityDto;
import org.openlmis.referencedata.dto.RequisitionGroupDto;
import org.openlmis.referencedata.dto.SupervisoryNodeBaseDto;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.RequisitionGroupRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Collections;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings({"PMD.TooManyMethods"})
public class RequisitionGroupValidatorTest extends BaseValidatorTest {

  @Mock
  private SupervisoryNodeRepository supervisoryNodes;

  @Mock
  private RequisitionGroupRepository requisitionGroups;

  @Mock
  private FacilityRepository facilities;

  @InjectMocks
  private Validator validator = new RequisitionGroupValidator();

  private SupervisoryNode supervisoryNode;
  private RequisitionGroupDto requisitionGroupDto;
  private Errors errors;

  @Before
  public void setUp() throws Exception {
    Facility facility = new Facility("TestFacilityCode");
    facility.setId(UUID.randomUUID());

    supervisoryNode = new SupervisoryNode();
    supervisoryNode.setId(UUID.randomUUID());

    RequisitionGroup requisitionGroup = new RequisitionGroup();
    requisitionGroup.setId(UUID.randomUUID());
    requisitionGroup.setCode(RandomStringUtils.randomAlphanumeric(50));
    requisitionGroup.setName(RandomStringUtils.randomAlphanumeric(50));
    requisitionGroup.setDescription(RandomStringUtils.randomAlphanumeric(250));
    requisitionGroup.setSupervisoryNode(supervisoryNode);
    requisitionGroup.setMemberFacilities(Sets.newHashSet(facility));

    requisitionGroupDto = new RequisitionGroupDto();
    requisitionGroup.export(requisitionGroupDto);

    errors = new BeanPropertyBindingResult(requisitionGroupDto, "requisitionGroup");

    doReturn(mock(SupervisoryNode.class))
        .when(supervisoryNodes)
        .findOne(supervisoryNode.getId());

    doReturn(mock(Facility.class))
        .when(facilities)
        .findOne(facility.getId());
  }

  @Test
  public void shouldNotFindErrors() throws Exception {
    validator.validate(requisitionGroupDto, errors);

    assertThat(errors.getErrorCount(), is(equalTo(0)));
  }

  @Test
  public void shouldRejectIfSupervisoryNodeIsEmpty() throws Exception {
    requisitionGroupDto.setSupervisoryNode((SupervisoryNodeBaseDto) null);

    validator.validate(requisitionGroupDto, errors);

    assertErrorMessage(errors, SUPERVISORY_NODE,
        "referenceData.error.requisitionGroup.supervisoryNode.required");
  }

  @Test
  public void shouldRejectIfRequisitionGroupCodeIsDuplicated() throws Exception {
    doReturn(mock(RequisitionGroup.class))
        .when(requisitionGroups)
        .findByCode(requisitionGroupDto.getCode());

    validator.validate(requisitionGroupDto, errors);

    assertErrorMessage(errors, CODE, "referenceData.error.requisitionGroup.code.duplicated");
  }

  @Test
  public void shouldRejectIfSupervisoryHasNoId() throws Exception {
    requisitionGroupDto.getSupervisoryNode().setId(null);

    validator.validate(requisitionGroupDto, errors);

    assertErrorMessage(errors, SUPERVISORY_NODE,
        "referenceData.error.requisitionGroup.supervisoryNode.id.required");
  }

  @Test
  public void shouldRejectIfSupervisoryNodeCanNotBeFound() throws Exception {
    doReturn(null)
        .when(supervisoryNodes)
        .findOne(any(UUID.class));

    validator.validate(requisitionGroupDto, errors);

    assertErrorMessage(errors, SUPERVISORY_NODE,
        "referenceData.error.requisitionGroup.supervisoryNode.nonExistent");
  }

  @Test
  public void shouldRejectIfFacilityIsNull() throws Exception {
    requisitionGroupDto.setMemberFacilityDtos(Collections.singleton(null));

    validator.validate(requisitionGroupDto, errors);

    assertErrorMessage(errors, MEMBER_FACILITIES,
        "referenceData.error.requisitionGroup.facility.null");
  }

  @Test
  public void shouldRejectIfFacilityHasNoId() throws Exception {
    requisitionGroupDto.setMemberFacilityDtos(Collections.singleton(new FacilityDto()));

    validator.validate(requisitionGroupDto, errors);

    assertErrorMessage(errors, MEMBER_FACILITIES,
        "referenceData.error.requisitionGroup.facility.id.required");
  }

  @Test
  public void shouldRejectIfFacilityCanNotBeFound() throws Exception {
    requisitionGroupDto.setMemberFacilityDtos(
        Collections.singleton(new FacilityDto(UUID.randomUUID())));

    validator.validate(requisitionGroupDto, errors);

    assertErrorMessage(errors, MEMBER_FACILITIES,
        "referenceData.error.requisitionGroup.facility.nonExistent");
  }

  @Test
  public void shouldNotThrowExceptionIfMemberFacilitiesIsNullOrEmpty() throws Exception {
    requisitionGroupDto.setMemberFacilities(null);

    validator.validate(requisitionGroupDto, errors);
    assertThat(errors.hasFieldErrors(MEMBER_FACILITIES), is(false));
  }

  @Test
  public void shouldRejectIfCodeIsTooLong() throws Exception {
    requisitionGroupDto.setCode(RandomStringUtils.randomAlphanumeric(51));

    validator.validate(requisitionGroupDto, errors);
    assertErrorMessage(errors, CODE, "referenceData.error.requisitionGroup.code.tooLong");
  }

  @Test
  public void shouldRejectIfNameIsTooLong() throws Exception {
    requisitionGroupDto.setName(RandomStringUtils.randomAlphanumeric(51));

    validator.validate(requisitionGroupDto, errors);
    assertErrorMessage(errors, NAME, "referenceData.error.requisitionGroup.name.tooLong");
  }

  @Test
  public void shouldRejectIfDescriptionIsTooLong() throws Exception {
    requisitionGroupDto.setDescription(RandomStringUtils.randomAlphanumeric(251));

    validator.validate(requisitionGroupDto, errors);
    assertErrorMessage(errors, DESCRIPTION,
        "referenceData.error.requisitionGroup.description.tooLong");
  }

  @Test
  public void shouldNotRejectIfCodeIsDuplicatedAndIdsAreSame() throws Exception {
    RequisitionGroup old = new RequisitionGroup();
    old.setId(requisitionGroupDto.getId());
    old.setCode(requisitionGroupDto.getCode());
    old.setName(RandomStringUtils.randomAlphanumeric(50));
    old.setDescription(RandomStringUtils.randomAlphanumeric(250));
    old.setSupervisoryNode(supervisoryNode);

    doReturn(old)
        .when(requisitionGroups)
        .findByCode(requisitionGroupDto.getCode());

    validator.validate(requisitionGroupDto, errors);
    assertThat(errors.hasFieldErrors(CODE), is(false));
  }

  @Test
  public void shouldRejectIfCodeIsDuplicatedAndIdsAreDifferent() throws Exception {
    RequisitionGroup old = new RequisitionGroup();
    old.setId(UUID.randomUUID());
    old.setCode(requisitionGroupDto.getCode());
    old.setName(RandomStringUtils.randomAlphanumeric(50));
    old.setDescription(RandomStringUtils.randomAlphanumeric(250));
    old.setSupervisoryNode(supervisoryNode);

    doReturn(old)
        .when(requisitionGroups)
        .findByCode(requisitionGroupDto.getCode());

    validator.validate(requisitionGroupDto, errors);
    assertErrorMessage(errors, CODE, "referenceData.error.requisitionGroup.code.duplicated");
  }
}
