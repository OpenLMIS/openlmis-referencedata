package org.openlmis.referencedata.util.messagekeys;

public abstract class UserMessageKeys extends MessageKeys {
  private static final String ERROR = join(SERVICE_ERROR, USER);
  private static final String RIGHTS = "rights";
  private static final String CREATE_TOKEN = "createToken";
  private static final String SEND_MESSAGE = "sendMessage";
  private static final String ASSIGNED_ROLE = "assignedRole";
  private static final String HOME_FACILITY = "homeFacility";
  private static final String RESET_PASSWORD = "resetPassword";
  private static final String CHANGE_PASSWORD = "changePassword";

  public static final String ERROR_ROLE_ID_NULL = join(ERROR, ROLE, ID, NULL);
  public static final String ERROR_NOT_FOUND = join(ERROR, NOT_FOUND);
  public static final String ERROR_NOT_FOUND_WITH_ID = join(ERROR_NOT_FOUND, WITH, ID);
  public static final String ERROR_SAVING = join(ERROR, SAVING);
  public static final String ERROR_HOME_FACILITY_NON_EXISTENT =
      join(ERROR, HOME_FACILITY, NON_EXISTENT);
  public static final String ERROR_PROGRAM_WITHOUT_FACILITY =
      join(ERROR, PROGRAM, WITHOUT, FACILITY);
  public static final String ERROR_ASSIGNED_ROLE_RIGHTS_EMPTY =
      join(ERROR, ASSIGNED_ROLE, RIGHTS, EMPTY);
  public static final String ERROR_EXTERNAL_RESET_PASSWORD_CREATE_TOKEN_FAILED =
      join(ERROR, EXTERNAL, RESET_PASSWORD, CREATE_TOKEN, FAILED);
  public static final String ERROR_EXTERNAL_RESET_PASSWORD_SEND_MESSAGE_FAILED =
      join(ERROR, EXTERNAL, RESET_PASSWORD, SEND_MESSAGE, FAILED);
  public static final String ERROR_EXTERNAL_CHANGE_PASSWORD_FAILED =
      join(ERROR, EXTERNAL, CHANGE_PASSWORD, FAILED);
  public static final String ERROR_EXTERNAL_RESET_PASSWORD_FAILED =
      join(ERROR, EXTERNAL, RESET_PASSWORD, FAILED);
}
