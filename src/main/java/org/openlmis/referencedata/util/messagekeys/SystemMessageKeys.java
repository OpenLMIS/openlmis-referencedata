package org.openlmis.referencedata.util.messagekeys;

public abstract class SystemMessageKeys extends MessageKeys {
  public static final String ERROR_UNAUTHORIZED = join(SERVICE_ERROR, UNAUTHORIZED);
  public static final String ERROR_UNAUTHORIZED_GENERIC = join(ERROR_UNAUTHORIZED, GENERIC);

  public static final String ACCOUNT_CREATED_EMAIL_SUBJECT = join(ACCOUNT, CREATED, EMAIL, SUBJECT);
  public static final String PASSWORD_RESET_EMAIL_BODY = join(PASSWORD, RESET, EMAIL, BODY);
}
