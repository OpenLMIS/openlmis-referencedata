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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.openlmis.referencedata.dto.VersionIdentityDto;
import org.openlmis.referencedata.repository.custom.FacilityTypeApprovedProductRepositoryCustom;
import org.openlmis.referencedata.util.Pagination;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public final class FacilityTypeApprovedProductSearchParams implements
    FacilityTypeApprovedProductRepositoryCustom.SearchParams {

  private List<String> facilityTypeCodes;
  private String programCode;
  private Boolean active;
  private List<VersionIdentityDto> identities;
  private Integer page;
  private Integer size;

  /**
   * Retrieve identifiers as a set of id-versionId pairs.
   */
  @JsonIgnore
  public Set<Pair<UUID, Long>> getIdentityPairs() {
    return Optional
        .ofNullable(identities)
        .orElse(Collections.emptyList())
        .stream()
        .map(identity -> ImmutablePair.of(identity.getId(), identity.getVersionId()))
        .collect(Collectors.toSet());
  }

  /**
   * Retrieve a {@link Pageable} instance with correct page and size parameters.
   */
  @JsonIgnore
  public Pageable getPageable() {
    return new PageRequest(
        Optional.ofNullable(page).orElse(Pagination.DEFAULT_PAGE_NUMBER),
        Optional.ofNullable(size).orElse(Pagination.NO_PAGINATION)
    );
  }

}
