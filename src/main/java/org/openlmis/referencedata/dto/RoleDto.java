package org.openlmis.referencedata.dto;

import lombok.Getter;
import lombok.Setter;

import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightType;
import org.openlmis.referencedata.domain.Role;

import java.util.HashSet;
import java.util.Set;

public class RoleDto extends BaseDto implements Role.Exporter, Role.Importer {

  @Getter
  @Setter
  private String name;

  @Getter
  @Setter
  private String description;

  private Set<RightDto> rights = new HashSet<>();

  @Override
  public Right.Exporter provideRightExporter() {
    return new RightDto();
  }

  @Override
  public void addRight(Right.Exporter rightExporter) {
    rights.add((RightDto) rightExporter);
  }

  @Override
  public Set<Right.Importer> getRights() {
    Set<Right.Importer> rightDtos = new HashSet<>();
    rightDtos.addAll(this.rights);
    return rightDtos;
  }

  public RightType getRightType() {
    return rights.iterator().next().getType();
  }
}
