package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightQuery;
import org.openlmis.referencedata.domain.RightType;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.repository.UserRepository;
import org.openlmis.referencedata.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

@Service
public class RightService {
  
  public static final String MESSAGEKEY_ERROR_UNAUTHORIZED = "referencedata.error.unauthorized";
  
  @Autowired
  UserRepository userRepository;

  /**
   * Check the client has the admin right specified.
   * 
   * @param rightName the name of the right to check
   */
  public void checkAdminRight(String rightName) {
    OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext()
        .getAuthentication();
    if (authentication.isClientOnly()) { // trusted client
      return;
    } else { // user-based client, check if user has right
      String username = ((User) authentication.getPrincipal()).getUsername();
      User user = userRepository.findOneByUsername(username);

      if (user.hasRight(
          new RightQuery(Right.newRight(rightName, RightType.GENERAL_ADMIN)))) {
        return;
      }
    }
    
    // at this point, user is unauthorized
    throw new UnauthorizedException(new Message(MESSAGEKEY_ERROR_UNAUTHORIZED, rightName));
  }
}
