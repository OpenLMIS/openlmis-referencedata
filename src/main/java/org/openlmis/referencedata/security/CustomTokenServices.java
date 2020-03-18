package org.openlmis.referencedata.security;

import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;

public class CustomTokenServices extends RemoteTokenServices {

  private int invalidTokenRetryLimit;

  public CustomTokenServices(int invalidTokenRetryLimit) {
    super();
    this.invalidTokenRetryLimit = invalidTokenRetryLimit;
  }

  @Override
  public OAuth2Authentication loadAuthentication(String accessToken) {
    return loadAuthentication(accessToken, 0);
  }

  private OAuth2Authentication loadAuthentication(String accessToken, int attempt) {
    try {
      return super.loadAuthentication(accessToken);
    } catch (InvalidTokenException e) {
      if (attempt < invalidTokenRetryLimit) {
        attempt++;
        logger.debug("Retrying authentication load. Retry number: " + attempt);
        return loadAuthentication(accessToken, attempt);
      }
      else throw e;
    }
  }
}