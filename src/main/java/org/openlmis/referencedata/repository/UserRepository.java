package org.openlmis.referencedata.repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.repository.custom.UserRepositoryCustom;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends
    PagingAndSortingRepository<User, UUID>,
    UserRepositoryCustom {

  @Override
  <S extends User> S save(S entity);

  @Override
  <S extends User> Iterable<S> save(Iterable<S> entities);

  User findOneByUsername(@Param("username") String username);

  @Query(value = "SELECT u.*"
      + " FROM referencedata.users u"
      + " WHERE u.extradata @> (:extraData)\\:\\:jsonb",
      nativeQuery = true
  )
  List<User> findByExtraData(@Param("extraData") String extraData);

  @Query(value = "SELECT DISTINCT u.*" 
      + " FROM referencedata.users u" 
      + "   JOIN referencedata.role_assignments ra ON ra.userid = u.id" 
      + "   JOIN referencedata.roles r ON r.id = ra.roleid" 
      + "   JOIN referencedata.role_rights rr ON rr.roleid = r.id"
      + " WHERE rr.rightid = :right" 
      + "   AND ra.supervisorynodeid = :supervisoryNode" 
      + "   AND ra.programid = :program",
      nativeQuery = true)
  Set<User> findSupervisingUsersBy(@Param("right") Right right,
      @Param("supervisoryNode") SupervisoryNode supervisoryNode,
      @Param("program") Program program);
}
