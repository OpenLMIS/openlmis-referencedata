package org.openlmis.referencedata.util.messagekeys;

public abstract class ProcessingPeriodMessageKeys extends MessageKeys {
  private static final String ERROR = join(SERVICE_ERROR, PROCESSING_PERIOD);
  private static final String START_DATE = "startDate";
  private static final String END_DATE = "endDate";

  public static final String ERROR_FACILITY_ID_NULL = join(ERROR, FACILITY, ID, NULL);

  public static final String ERROR_PROGRAM_ID_NULL = join(ERROR, PROGRAM, ID, NULL);

  public static final String ERROR_START_DATE_NULL = join(ERROR, START_DATE, NULL);

  public static final String ERROR_END_DATE_NULL = join(ERROR, END_DATE, NULL);

  public static final String ERROR_START_DATE_AFTER_END_DATE =
      join(ERROR, START_DATE, "after", END_DATE);

  public static final String ERROR_END_DATE_BEFORE_START_DATE =
      join(ERROR, END_DATE, "before", START_DATE);

  public static final String ERROR_GAP_BETWEEN_LAST_END_DATE_AND_START_DATE =
      join(ERROR, "gap", "between", "lastEndDate", AND, START_DATE);
}
