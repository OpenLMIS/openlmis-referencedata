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

package org.openlmis.referencedata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.SupplyPartner;
import org.openlmis.referencedata.dto.ObjectReferenceDto;
import org.openlmis.referencedata.dto.SupplyPartnerDto;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.repository.SupplyPartnerRepository;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.testbuilder.OrderableDataBuilder;
import org.openlmis.referencedata.testbuilder.ProgramDataBuilder;
import org.openlmis.referencedata.testbuilder.SupervisoryNodeDataBuilder;
import org.openlmis.referencedata.testbuilder.SupplyPartnerAssociationDataBuilder;
import org.openlmis.referencedata.testbuilder.SupplyPartnerDataBuilder;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.util.messagekeys.FacilityMessageKeys;
import org.openlmis.referencedata.util.messagekeys.OrderableMessageKeys;
import org.openlmis.referencedata.util.messagekeys.ProgramMessageKeys;
import org.openlmis.referencedata.util.messagekeys.SupervisoryNodeMessageKeys;
import org.openlmis.referencedata.util.messagekeys.SupplyPartnerMessageKeys;
import org.springframework.data.domain.PageRequest;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class SupplyPartnerBuilderTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Mock
  private SupplyPartnerRepository supplyPartnerRepository;

  @Mock
  private ProgramRepository programRepository;

  @Mock
  private SupervisoryNodeRepository supervisoryNodeRepository;

  @Mock
  private FacilityRepository facilityRepository;

  @Mock
  private OrderableRepository orderableRepository;

  @InjectMocks
  private SupplyPartnerBuilder builder;

  private Program program = new ProgramDataBuilder().build();
  private SupervisoryNode supervisoryNode = new SupervisoryNodeDataBuilder().build();
  private Facility facility = new FacilityDataBuilder().build();
  private Orderable orderable = new OrderableDataBuilder().build();
  private SupplyPartner supplyPartner = new SupplyPartnerDataBuilder()
      .withAssociation(
          new SupplyPartnerAssociationDataBuilder()
              .withProgram(program)
              .withSupervisoryNode(supervisoryNode)
              .withFacility(facility)
              .withOrderable(orderable)
              .build())
      .build();

  private SupplyPartnerDto importer = new SupplyPartnerDto();

  @Before
  public void setUp() {
    supplyPartner.export(importer);
    importer.setId(null);

    when(programRepository.findOne(program.getId())).thenReturn(program);
    when(supervisoryNodeRepository.findOne(supervisoryNode.getId())).thenReturn(supervisoryNode);
    when(facilityRepository.findAll(Sets.newHashSet(facility.getId())))
        .thenReturn(Lists.newArrayList(facility));
    when(orderableRepository.findAllLatestByIds(
        Sets.newHashSet(orderable.getId()), new PageRequest(0, 1)))
        .thenReturn(Pagination.getPage(Lists.newArrayList(orderable)));
  }

  @Test
  public void shouldBuildDomainObjectBasedOnDataFromImporter() {
    SupplyPartner built = builder.build(importer);

    assertBuiltResource(built, null);
  }

  @Test
  public void shouldUpdateExistingDomainObjectBasedOnDataFromImporter() {
    SupplyPartner existing = new SupplyPartner();
    existing.setId(supplyPartner.getId());

    when(supplyPartnerRepository.findOne(supplyPartner.getId())).thenReturn(existing);

    importer.setId(supplyPartner.getId());

    SupplyPartner built = builder.build(importer);

    assertBuiltResource(built, importer.getId());
  }

  @Test
  public void shouldBuildDomainObjectWithGivenIdBasedOnDataFromImporter() {
    importer.setId(supplyPartner.getId());
    when(supplyPartnerRepository.findOne(supplyPartner.getId())).thenReturn(null);

    SupplyPartner built = builder.build(importer);

    assertBuiltResource(built, importer.getId());
  }

  @Test
  public void shouldBuildDomainObjectWithoutAssociations() {
    importer.setAssociations(null);

    SupplyPartner built = builder.build(importer);

    assertThat(built.getAssociations()).isEmpty();
  }

  @Test
  public void shouldThrowExceptionIfProgramCouldNotBeFound() {
    exception.expect(ValidationMessageException.class);
    exception.expectMessage(ProgramMessageKeys.ERROR_NOT_FOUND);

    when(programRepository.findOne(program.getId())).thenReturn(null);

    builder.build(importer);
  }

  @Test
  public void shouldThrowExceptionIfProgramIdIsNull() {
    exception.expect(ValidationMessageException.class);
    exception.expectMessage(ProgramMessageKeys.ERROR_NOT_FOUND);

    importer.getAssociations().get(0).getProgram().setId(null);

    builder.build(importer);
  }

  @Test
  public void shouldThrowExceptionIfProgramImporterIsNull() {
    exception.expect(ValidationMessageException.class);
    exception.expectMessage(ProgramMessageKeys.ERROR_NOT_FOUND);

    importer.getAssociations().get(0).setProgram((ObjectReferenceDto) null);

    builder.build(importer);
  }

  @Test
  public void shouldThrowExceptionIfSupervisoryNodeCouldNotBeFound() {
    exception.expect(ValidationMessageException.class);
    exception.expectMessage(SupervisoryNodeMessageKeys.ERROR_NOT_FOUND);

    when(supervisoryNodeRepository.findOne(supervisoryNode.getId())).thenReturn(null);

    builder.build(importer);
  }

  @Test
  public void shouldThrowExceptionIfSupervisoryNodeIdIsNull() {
    exception.expect(ValidationMessageException.class);
    exception.expectMessage(SupervisoryNodeMessageKeys.ERROR_NOT_FOUND);

    importer.getAssociations().get(0).getSupervisoryNode().setId(null);

    builder.build(importer);
  }

  @Test
  public void shouldThrowExceptionIfSupervisoryNodeImporterIsNull() {
    exception.expect(ValidationMessageException.class);
    exception.expectMessage(SupervisoryNodeMessageKeys.ERROR_NOT_FOUND);

    importer.getAssociations().get(0).setSupervisoryNode((ObjectReferenceDto) null);

    builder.build(importer);
  }

  @Test
  public void shouldThrowExceptionIfFacilityCouldNotBeFound() {
    exception.expect(ValidationMessageException.class);
    exception.expectMessage(FacilityMessageKeys.ERROR_NOT_FOUND);

    when(facilityRepository.findAll(Sets.newHashSet(facility.getId())))
        .thenReturn(Collections.emptyList());

    builder.build(importer);
  }

  @Test
  public void shouldThrowExceptionIfOrderableCouldNotBeFound() {
    exception.expect(ValidationMessageException.class);
    exception.expectMessage(OrderableMessageKeys.ERROR_NOT_FOUND);

    when(orderableRepository.findAllLatestByIds(
        Sets.newHashSet(orderable.getId()), new PageRequest(0, 1)))
        .thenReturn(Pagination.getPage(Collections.emptyList()));

    builder.build(importer);
  }

  @Test
  public void shouldThrowExceptionIfNoFacilitiesWereSelected() {
    exception.expect(ValidationMessageException.class);
    exception.expectMessage(SupplyPartnerMessageKeys.ERROR_MISSING_FACILITIES);

    importer.getAssociations().get(0).setFacilities(Collections.emptyList());

    builder.build(importer);
  }

  @Test
  public void shouldThrowExceptionIfNoOrderablesWereSelected() {
    exception.expect(ValidationMessageException.class);
    exception.expectMessage(SupplyPartnerMessageKeys.ERROR_MISSING_ORDERABLES);

    importer.getAssociations().get(0).setOrderables(Collections.emptyList());

    builder.build(importer);
  }

  private void assertBuiltResource(SupplyPartner built, UUID id) {
    assertThat(built)
        .hasFieldOrPropertyWithValue("id", id)
        .hasFieldOrPropertyWithValue("code", importer.getCode())
        .hasFieldOrPropertyWithValue("name", importer.getName());

    assertThat(built.getAssociations())
        .hasSize(1);

    assertThat(built.getAssociations().get(0))
        .hasFieldOrPropertyWithValue("program", program)
        .hasFieldOrPropertyWithValue("supervisoryNode", supervisoryNode)
        .hasFieldOrPropertyWithValue("facilities", Lists.newArrayList(facility))
        .hasFieldOrPropertyWithValue("orderables", Lists.newArrayList(orderable));
  }
}
