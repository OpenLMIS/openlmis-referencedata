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

package org.openlmis.referencedata.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.referencedata.domain.Dispensable;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.repository.OrderableRepository;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrderableDto extends BaseDto implements Orderable.Importer, Orderable.Exporter {

  public static final String META_KEY_VERSION_ID = "versionId";
  public static final String META_KEY_LAST_UPDATED = "lastUpdated";

  private String productCode;

  private DispensableDto dispensable;

  private String fullProductName;

  private String description;

  private Long netContent;

  private Long packRoundingThreshold;

  private Boolean roundToZero;

  private Set<ProgramOrderableDto> programs;

  private Map<String, String> identifiers;

  private Map<String, String> extraData;

  private Map<String, String> meta = new HashMap<>();
  
  @JsonIgnore
  private OrderableRepository orderableRepository;

  /**
   * Create new set of OrderableDto based on given iterable of {@link Orderable}.
   *
   * @param orderables list of {@link Orderable}
   * @return new list of OrderableDto.
   */
  public static List<OrderableDto> newInstance(Iterable<Orderable> orderables) {
    List<OrderableDto> orderableDtos = new LinkedList<>();
    orderables.forEach(oe -> orderableDtos.add(newInstance(oe)));
    return orderableDtos;
  }

  /**
   * Creates new instance based on given {@link Orderable}.
   *
   * @param po instance of Orderable.
   * @return new instance of OrderableDto.
   */
  public static OrderableDto newInstance(Orderable po) {
    if (po == null) {
      return null;
    }
    OrderableDto orderableDto = new OrderableDto();
    po.export(orderableDto);

    return orderableDto;
  }

  @JsonSetter("dispensable")
  public void setDispensable(DispensableDto dispensable) {
    this.dispensable = dispensable;
  }

  @Override
  public void setDispensable(Dispensable dispensable) {
    this.dispensable = new DispensableDto();
    dispensable.export(this.dispensable);
  }
  
  @Override
  @JsonIgnore
  public Long getVersionId() {
    if (null == orderableRepository) {
      return 1L;
    } else {
      Orderable latestOrderable = orderableRepository
          .findFirstByIdentityIdOrderByIdentityVersionIdDesc(getId());
      return latestOrderable.getVersionId();
    }
  }
  
  @Override
  public void setVersionId(Long versionId) {
    meta.put(META_KEY_VERSION_ID, versionId.toString());
  }

  @Override
  public void setLastUpdated(ZonedDateTime lastUpdated) {
    meta.put(META_KEY_LAST_UPDATED, lastUpdated.toString());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof OrderableDto)) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }
    OrderableDto that = (OrderableDto) obj;
    return Objects.equals(productCode, that.productCode)
        && Objects.equals(dispensable, that.dispensable)
        && Objects.equals(fullProductName, that.fullProductName)
        && Objects.equals(description, that.description)
        && Objects.equals(netContent, that.netContent)
        && Objects.equals(packRoundingThreshold, that.packRoundingThreshold)
        && Objects.equals(roundToZero, that.roundToZero)
        && Objects.equals(programs, that.programs)
        && Objects.equals(identifiers, that.identifiers)
        && Objects.equals(extraData, that.extraData)
        && isMetaEquals(that);
  }

  @Override
  public int hashCode() {

    return Objects
        .hash(super.hashCode(), productCode, dispensable, fullProductName, description, netContent,
            packRoundingThreshold, roundToZero, programs, identifiers, extraData, meta);
  }
  
  boolean isMetaEquals(OrderableDto that) {
    for (Map.Entry<String, String> metaEntry : meta.entrySet()) {
      String metaKey = metaEntry.getKey();
      Object metaValue = metaEntry.getValue();
      if (null != metaKey && metaKey.equalsIgnoreCase(META_KEY_LAST_UPDATED)) {
        Instant instantThis = ZonedDateTime.parse(metaValue.toString()).toInstant();
        Instant instantThat = ZonedDateTime.parse(that.meta.get(META_KEY_LAST_UPDATED)).toInstant();
        if (!instantThis.equals(instantThat)) {
          return false;
        }
      } else {
        if (!metaValue.equals(that.meta.get(metaKey))) {
          return false;
        }
      }
    }
    
    return true;
  }
}
