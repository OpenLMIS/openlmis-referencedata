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

package org.openlmis.referencedata.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.Getter;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.SupplyPartner;
import org.openlmis.referencedata.repository.custom.SupplyPartnerRepositoryCustom;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.testbuilder.FacilityTypeDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicLevelDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicZoneDataBuilder;
import org.openlmis.referencedata.testbuilder.OrderableDataBuilder;
import org.openlmis.referencedata.testbuilder.ProgramDataBuilder;
import org.openlmis.referencedata.testbuilder.SupervisoryNodeDataBuilder;
import org.openlmis.referencedata.testbuilder.SupplyPartnerAssociationDataBuilder;
import org.openlmis.referencedata.testbuilder.SupplyPartnerDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public class SupplyPartnerRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<SupplyPartner> {

  @Autowired
  private SupplyPartnerRepository supplyPartnerRepository;

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private SupervisoryNodeRepository supervisoryNodeRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private OrderableRepository orderableRepository;

  @Autowired
  private FacilityTypeRepository facilityTypeRepository;

  @Autowired
  private GeographicLevelRepository geographicLevelRepository;

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  private SupplyPartner[] supplyPartners;
  private Pageable pageable = new PageRequest(0, 10);

  @Override
  CrudRepository<SupplyPartner, UUID> getRepository() {
    return supplyPartnerRepository;
  }

  @Override
  SupplyPartner generateInstance() {
    FacilityType facilityType = facilityTypeRepository.save(
        new FacilityTypeDataBuilder()
            .buildAsNew());

    GeographicLevel geographicLevel = geographicLevelRepository.save(
        new GeographicLevelDataBuilder()
            .buildAsNew());

    GeographicZone geographicZone = geographicZoneRepository.save(
        new GeographicZoneDataBuilder()
            .withLevel(geographicLevel)
            .buildAsNew());

    Program program = programRepository.save(
        new ProgramDataBuilder()
            .withoutId()
            .build());
    Facility facility = facilityRepository.save(
        new FacilityDataBuilder()
            .withType(facilityType)
            .withGeographicZone(geographicZone)
            .withoutOperator()
            .buildAsNew());
    SupervisoryNode supervisoryNode = supervisoryNodeRepository
        .save(new SupervisoryNodeDataBuilder()
            .withFacility(facility)
            .withoutId()
            .build());
    Orderable orderable = orderableRepository.save(
        new OrderableDataBuilder()
            .buildAsNew());

    return new SupplyPartnerDataBuilder()
        .withCode("supply-partner-" + getNextInstanceNumber())
        .withAssociation(new SupplyPartnerAssociationDataBuilder()
            .withProgram(program)
            .withSupervisoryNode(supervisoryNode)
            .withFacility(facility)
            .withOrderable(orderable)
            .buildAsNew())
        .buildAsNew();
  }

  @Before
  public void setUp() {
    supplyPartners = IntStream
        .range(0, 10)
        .mapToObj(idx -> generateInstance())
        .peek(supplyPartnerRepository::save)
        .toArray(SupplyPartner[]::new);
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldRejectIfFacilityCodeIsNotUniqueCaseInsensitive() {
    SupplyPartner withUpperCaseCode = new SupplyPartnerDataBuilder()
        .withCode("CODE")
        .buildAsNew();

    SupplyPartner withLowerCaseCode = new SupplyPartnerDataBuilder()
        .withCode("code")
        .buildAsNew();

    supplyPartnerRepository.saveAndFlush(withUpperCaseCode);
    supplyPartnerRepository.saveAndFlush(withLowerCaseCode);
  }

  @Test
  public void shouldReturnAllSupplyPartnersIfNoParamsWereSet() {
    SupplyPartnerRepositoryCustom.SearchParams searchParams = new TestSearchParams();

    Page<SupplyPartner> search = supplyPartnerRepository.search(searchParams, pageable);
    assertThat(search.getContent()).contains(supplyPartners);
  }

  @Test
  public void shouldReturnSupplyPartnersForGivenIds() {
    SupplyPartnerRepositoryCustom.SearchParams searchParams = new TestSearchParams(
        Sets.newHashSet(
            supplyPartners[0].getId(), supplyPartners[3].getId(),
            supplyPartners[5].getId(), supplyPartners[9].getId()),
        Collections.emptySet()
    );

    Page<SupplyPartner> search = supplyPartnerRepository.search(searchParams, pageable);
    assertThat(search.getContent())
        .hasSize(4)
        .contains(supplyPartners[0], supplyPartners[3], supplyPartners[5], supplyPartners[9]);
  }

  @Test
  public void shouldReturnSupplyPartnersForGivenSupervisoryNodeIds() {
    SupplyPartnerRepositoryCustom.SearchParams searchParams = new TestSearchParams(
        Collections.emptySet(),
        Sets.newHashSet(
            supplyPartners[0].getAssociations().get(0).getSupervisoryNode().getId(),
            supplyPartners[7].getAssociations().get(0).getSupervisoryNode().getId())
    );

    Page<SupplyPartner> search = supplyPartnerRepository.search(searchParams, pageable);
    assertThat(search.getContent())
        .hasSize(2)
        .contains(supplyPartners[0], supplyPartners[7]);
  }

  @Test
  public void shouldReturnSupplyPartnersThatMatchAllSearchParams() {
    SupplyPartnerRepositoryCustom.SearchParams searchParams = new TestSearchParams(
        Sets.newHashSet(
            supplyPartners[0].getId(),
            supplyPartners[5].getId(),
            supplyPartners[9].getId()),
        Sets.newHashSet(
            supplyPartners[0].getAssociations().get(0).getSupervisoryNode().getId(),
            supplyPartners[2].getAssociations().get(0).getSupervisoryNode().getId(),
            supplyPartners[5].getAssociations().get(0).getSupervisoryNode().getId(),
            supplyPartners[8].getAssociations().get(0).getSupervisoryNode().getId())
    );

    Page<SupplyPartner> search = supplyPartnerRepository.search(searchParams, pageable);
    assertThat(search.getContent())
        .hasSize(2)
        .contains(supplyPartners[0], supplyPartners[5]);
  }

  @Getter
  private static final class TestSearchParams
      implements SupplyPartnerRepositoryCustom.SearchParams {

    private Set<UUID> ids;
    private Set<UUID> supervisoryNodeIds;

    TestSearchParams() {
      this(Collections.emptySet(), Collections.emptySet());
    }

    TestSearchParams(Set<UUID> ids, Set<UUID> supervisoryNodeIds) {
      this.ids = Optional
          .ofNullable(ids)
          .orElse(Collections.emptySet());

      this.supervisoryNodeIds = Optional
          .ofNullable(supervisoryNodeIds)
          .orElse(Collections.emptySet());
    }
  }
}
