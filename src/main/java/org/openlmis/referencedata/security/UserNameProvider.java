package org.openlmis.referencedata.security;

import org.javers.spring.auditable.AuthorProvider;
import org.openlmis.referencedata.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserNameProvider implements AuthorProvider {

  @Override
  public String provide() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null) {
      return "unauthenticated user";
    }

    try {
      User user = (User) auth.getPrincipal();
      return user.getUsername();
    } catch (Exception ex) {
      return "unknown user";
    }
  }

}
