package org.openlmis.referencedata.repository;

import org.openlmis.referencedata.domain.Role;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface RoleRepository extends PagingAndSortingRepository<Role, UUID> {

  Role findFirstByName(String name);
}
