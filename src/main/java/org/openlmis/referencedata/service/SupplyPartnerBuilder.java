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

import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.SupplyPartner;
import org.openlmis.referencedata.domain.SupplyPartnerAssociation;
import org.openlmis.referencedata.dto.SupplyPartnerAssociationDto;
import org.openlmis.referencedata.dto.SupplyPartnerDto;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.repository.SupplyPartnerRepository;
import org.openlmis.referencedata.util.messagekeys.FacilityMessageKeys;
import org.openlmis.referencedata.util.messagekeys.OrderableMessageKeys;
import org.openlmis.referencedata.util.messagekeys.ProgramMessageKeys;
import org.openlmis.referencedata.util.messagekeys.SupervisoryNodeMessageKeys;
import org.openlmis.referencedata.util.messagekeys.SupplyPartnerMessageKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class SupplyPartnerBuilder
    implements DomainResourceBuilder<SupplyPartnerDto, SupplyPartner> {

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

  @Override
  public SupplyPartner build(SupplyPartnerDto importer) {
    SupplyPartner supplyPartner;

    if (null == importer.getId()) {
      supplyPartner = new SupplyPartner();
    } else {
      supplyPartner = supplyPartnerRepository.findOne(importer.getId());

      if (null == supplyPartner) {
        supplyPartner = new SupplyPartner();
        supplyPartner.setId(importer.getId());
      }
    }

    supplyPartner.updateFrom(importer);
    addAssociations(importer.getAssociations(), supplyPartner);

    return supplyPartner;
  }

  private void addAssociations(List<SupplyPartnerAssociationDto> associations,
      SupplyPartner supplyPartner) {
    supplyPartner.removeAllAssociations();

    if (CollectionUtils.isEmpty(associations)) {
      return;
    }

    associations
        .stream()
        .map(this::createAssociation)
        .forEach(supplyPartner::addAssociation);
  }

  private SupplyPartnerAssociation createAssociation(SupplyPartnerAssociationDto dto) {
    if (dto.getFacilityIds().isEmpty()) {
      throw new ValidationMessageException(SupplyPartnerMessageKeys.ERROR_MISSING_FACILITIES);
    }

    if (dto.getOrderableIds().isEmpty()) {
      throw new ValidationMessageException(SupplyPartnerMessageKeys.ERROR_MISSING_ORDERABLES);
    }

    Program program = findResource(programRepository::findOne,
        dto.getProgram(), ProgramMessageKeys.ERROR_NOT_FOUND);

    SupervisoryNode supervisoryNode = findResource(supervisoryNodeRepository::findOne,
        dto.getSupervisoryNode(), SupervisoryNodeMessageKeys.ERROR_NOT_FOUND);

    List<Facility> facilities = findResources(facilityRepository::findAll,
        dto.getFacilityIds(), FacilityMessageKeys.ERROR_NOT_FOUND);

    List<Orderable> orderables = findResources(
        ids -> orderableRepository.findAllLatestByIds(ids, new PageRequest(0, ids.size())),
        dto.getOrderableIds(), OrderableMessageKeys.ERROR_NOT_FOUND);

    return new SupplyPartnerAssociation(program, supervisoryNode, facilities, orderables);
  }
}
