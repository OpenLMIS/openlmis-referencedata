package org.openlmis.referencedata.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.openlmis.referencedata.domain.BaseEntity;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.User;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
public class UserDto extends BaseEntity implements User.Exporter, User.Importer {

  @Getter
  @Setter
  private String username;

  @Getter
  @Setter
  private String firstName;

  @Getter
  @Setter
  private String lastName;

  @Getter
  @Setter
  private String email;

  @Getter
  @Setter
  private String timezone;

  @Setter
  private String homeFacilityCode;

  @Getter
  @Setter
  private Facility homeFacility;

  @Getter
  @Setter
  private boolean verified;

  @Getter
  @Setter
  private boolean active;

  public String fetchHomeFacilityCode() {
    return homeFacilityCode;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof UserDto)) {
      return false;
    }
    UserDto userDto = (UserDto) obj;
    return Objects.equals(username, userDto.username);
  }

  @Override
  public int hashCode() {
    return Objects.hash(username);
  }
}
