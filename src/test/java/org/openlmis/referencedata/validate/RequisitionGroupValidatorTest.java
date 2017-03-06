/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

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
import static org.openlmis.referencedata.validate.ValidationTestUtils.assertErrorMessage;

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
import org.openlmis.referencedata.util.messagekeys.RequisitionGroupMessageKeys;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Collections;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings({"PMD.TooManyMethods"})
public class RequisitionGroupValidatorTest {

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
        RequisitionGroupMessageKeys.ERROR_SUPERVISORY_NODE_REQUIRED);
  }

  @Test
  public void shouldRejectIfRequisitionGroupCodeIsDuplicated() throws Exception {
    doReturn(mock(RequisitionGroup.class))
        .when(requisitionGroups)
        .findByCode(requisitionGroupDto.getCode());

    validator.validate(requisitionGroupDto, errors);

    assertErrorMessage(errors, CODE, RequisitionGroupMessageKeys.ERROR_CODE_DUPLICATED);
  }

  @Test
  public void shouldRejectIfSupervisoryHasNoId() throws Exception {
    requisitionGroupDto.getSupervisoryNode().setId(null);

    validator.validate(requisitionGroupDto, errors);

    assertErrorMessage(errors, SUPERVISORY_NODE,
        RequisitionGroupMessageKeys.ERROR_SUPERVISORY_NODE_ID_REQUIRED);
  }

  @Test
  public void shouldRejectIfSupervisoryNodeCanNotBeFound() throws Exception {
    doReturn(null)
        .when(supervisoryNodes)
        .findOne(any(UUID.class));

    validator.validate(requisitionGroupDto, errors);

    assertErrorMessage(errors, SUPERVISORY_NODE,
        RequisitionGroupMessageKeys.ERROR_SUPERVISORY_NODE_NON_EXISTENT);
  }

  @Test
  public void shouldRejectIfFacilityIsNull() throws Exception {
    requisitionGroupDto.setMemberFacilityDtos(Collections.singleton(null));

    validator.validate(requisitionGroupDto, errors);

    assertErrorMessage(errors, MEMBER_FACILITIES,
        RequisitionGroupMessageKeys.ERROR_FACILITY_NULL);
  }

  @Test
  public void shouldRejectIfFacilityHasNoId() throws Exception {
    requisitionGroupDto.setMemberFacilityDtos(Collections.singleton(new FacilityDto()));

    validator.validate(requisitionGroupDto, errors);

    assertErrorMessage(errors, MEMBER_FACILITIES,
        RequisitionGroupMessageKeys.ERROR_FACILITY_ID_REQUIRED);
  }

  @Test
  public void shouldRejectIfFacilityCanNotBeFound() throws Exception {
    requisitionGroupDto.setMemberFacilityDtos(
        Collections.singleton(new FacilityDto(UUID.randomUUID())));

    validator.validate(requisitionGroupDto, errors);

    assertErrorMessage(errors, MEMBER_FACILITIES,
        RequisitionGroupMessageKeys.ERROR_FACILITY_NON_EXISTENT);
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
    assertErrorMessage(errors, CODE, RequisitionGroupMessageKeys.ERROR_CODE_TOO_LONG);
  }

  @Test
  public void shouldRejectIfNameIsTooLong() throws Exception {
    requisitionGroupDto.setName(RandomStringUtils.randomAlphanumeric(51));

    validator.validate(requisitionGroupDto, errors);
    assertErrorMessage(errors, NAME, RequisitionGroupMessageKeys.ERROR_NAME_TOO_LONG);
  }

  @Test
  public void shouldRejectIfDescriptionIsTooLong() throws Exception {
    requisitionGroupDto.setDescription(RandomStringUtils.randomAlphanumeric(251));

    validator.validate(requisitionGroupDto, errors);
    assertErrorMessage(errors, DESCRIPTION,
        RequisitionGroupMessageKeys.ERROR_DESCRIPTION_TOO_LONG);
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
    assertErrorMessage(errors, CODE, RequisitionGroupMessageKeys.ERROR_CODE_DUPLICATED);
  }
}
