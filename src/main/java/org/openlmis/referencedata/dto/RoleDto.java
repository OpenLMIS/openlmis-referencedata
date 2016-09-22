package org.openlmis.referencedata.dto;

import lombok.Getter;
import lombok.Setter;

import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.Role;

import java.util.HashSet;
import java.util.Objects;
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

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof RoleDto)) {
      return false;
    }
    RoleDto roleDto = (RoleDto) obj;
    return Objects.equals(name, roleDto.name)
        && Objects.equals(rights, roleDto.rights);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, rights);
  }
}
