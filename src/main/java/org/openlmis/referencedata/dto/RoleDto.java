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
  public void setRights(Set<Right> rights) {
    for (Right right : rights) {
      RightDto rightDto = new RightDto();
      right.export(rightDto);
      this.rights.add(rightDto);
    }
  }

  @Override
  public Set<Right.Importer> getRights() {
    Set<Right.Importer> rights = new HashSet<>();
    rights.addAll(this.rights);
    return rights;
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
