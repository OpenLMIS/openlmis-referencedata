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

package org.openlmis.referencedata.web;

import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.SupportedProgram;
import org.openlmis.referencedata.dto.FacilityDto;
import org.openlmis.referencedata.fhir.FhirClient;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.service.FacilityBuilder;
import org.openlmis.referencedata.service.RegenerateRightAssignmentsEvent;
import org.openlmis.referencedata.service.RightService;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.testbuilder.ProgramDataBuilder;
import org.openlmis.referencedata.testbuilder.SupportedProgramDataBuilder;
import org.openlmis.referencedata.validate.FacilityValidator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.validation.BindingResult;

@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.TooManyMethods"})
public class FacilityControllerTest {

  @Mock
  private FacilityRepository facilityRepository;

  @Mock
  private FacilityValidator facilityValidator;

  @Mock
  private FacilityBuilder facilityBuilder;

  @Mock
  private FhirClient fhirClient;

  @Mock
  private RightService rightService;

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  @InjectMocks
  private FacilityController controller = new FacilityController();

  private final BindingResult bindingResult = mock(BindingResult.class);

  private Program program;
  private Program otherProgram;
  private Facility existing;
  private Facility updated;
  private FacilityDto facilityDto;

  @Before
  public void setUp() {
    initMocks(this);

    program = new ProgramDataBuilder().build();
    otherProgram = new ProgramDataBuilder().build();
    existing = new FacilityDataBuilder().build();
    existing.setSupportedPrograms(newHashSet(supportedProgram(existing, program, true)));

    updated = new FacilityDataBuilder().withName("New name").build();
    updated.setId(existing.getId());
    facilityDto = FacilityDto.newInstance(existing);

    when(facilityRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
    when(facilityBuilder.build(facilityDto)).thenReturn(updated);
    when(facilityRepository.saveAndFlush(updated)).thenReturn(updated);
  }

  @Test
  public void shouldLockFacilityBeforeAnythingElseReadsIt() {
    updated.setSupportedPrograms(newHashSet(supportedProgram(updated, program, true)));

    controller.saveFacility(facilityDto, existing.getId(), bindingResult);

    InOrder inOrder = inOrder(facilityRepository, facilityValidator, facilityBuilder);
    inOrder.verify(facilityRepository).findByIdForUpdate(existing.getId());
    inOrder.verify(facilityValidator).validate(facilityDto, bindingResult);
    inOrder.verify(facilityRepository).findById(existing.getId());
    inOrder.verify(facilityBuilder).build(facilityDto);
  }

  @Test
  public void shouldNotRegenerateRightAssignmentsWhenSupportedProgramsAreUnchanged() {
    updated.setSupportedPrograms(newHashSet(supportedProgram(updated, program, true)));

    controller.saveFacility(facilityDto, existing.getId(), bindingResult);

    verify(facilityRepository).saveAndFlush(updated);
    verify(applicationEventPublisher, never())
        .publishEvent(any(RegenerateRightAssignmentsEvent.class));
  }

  @Test
  public void shouldRegenerateRightAssignmentsWhenSupportedProgramIsAdded() {
    updated.setSupportedPrograms(newHashSet(
        supportedProgram(updated, program, true),
        supportedProgram(updated, otherProgram, true)));

    controller.saveFacility(facilityDto, existing.getId(), bindingResult);

    verify(applicationEventPublisher).publishEvent(any(RegenerateRightAssignmentsEvent.class));
  }

  @Test
  public void shouldRegenerateRightAssignmentsWhenSupportedProgramIsRemoved() {
    existing.setSupportedPrograms(newHashSet(
        supportedProgram(existing, program, true),
        supportedProgram(existing, otherProgram, true)));
    updated.setSupportedPrograms(newHashSet(supportedProgram(updated, program, true)));

    controller.saveFacility(facilityDto, existing.getId(), bindingResult);

    verify(applicationEventPublisher).publishEvent(any(RegenerateRightAssignmentsEvent.class));
  }

  @Test
  public void shouldRegenerateRightAssignmentsWhenSupportActiveFlagChanges() {
    updated.setSupportedPrograms(newHashSet(supportedProgram(updated, program, false)));

    controller.saveFacility(facilityDto, existing.getId(), bindingResult);

    verify(applicationEventPublisher).publishEvent(any(RegenerateRightAssignmentsEvent.class));
  }

  @Test
  public void shouldRegenerateRightAssignmentsWhenBuilderChangesTheLoadedFacilityInPlace() {
    when(facilityBuilder.build(facilityDto)).thenAnswer(invocation -> {
      existing.setSupportedPrograms(newHashSet(supportedProgram(existing, program, false)));
      return existing;
    });
    when(facilityRepository.saveAndFlush(existing)).thenReturn(existing);

    controller.saveFacility(facilityDto, existing.getId(), bindingResult);

    verify(applicationEventPublisher).publishEvent(any(RegenerateRightAssignmentsEvent.class));
  }

  @Test
  public void shouldRegenerateRightAssignmentsWhenUpdateCreatesFacilityWithSupportedPrograms() {
    when(facilityRepository.findById(existing.getId())).thenReturn(Optional.empty());
    updated.setSupportedPrograms(newHashSet(supportedProgram(updated, program, true)));

    controller.saveFacility(facilityDto, existing.getId(), bindingResult);

    verify(applicationEventPublisher).publishEvent(any(RegenerateRightAssignmentsEvent.class));
  }

  private SupportedProgram supportedProgram(Facility facility, Program program, boolean active) {
    return new SupportedProgramDataBuilder()
        .withFacility(facility)
        .withProgram(program)
        .withActiveFlag(active)
        .build();
  }
}
