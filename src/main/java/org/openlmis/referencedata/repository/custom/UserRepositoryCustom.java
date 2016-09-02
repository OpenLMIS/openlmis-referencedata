package org.openlmis.referencedata.repository.custom;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.User;

import java.util.List;

public interface UserRepositoryCustom {

  List<User> searchUsers(
      String username, String firstName, String lastName,
      Facility homeFacility, Boolean active, Boolean verified);
}
