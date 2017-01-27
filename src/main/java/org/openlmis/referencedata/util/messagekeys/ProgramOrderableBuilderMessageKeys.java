package org.openlmis.referencedata.util.messagekeys;

public abstract class ProgramOrderableBuilderMessageKeys extends MessageKeys {
  private static final String PROGRAM_ORDERABLE_BUILDER = "programOrderableBuilder";
  private static final String ERROR = join(SERVICE_ERROR, PROGRAM_ORDERABLE_BUILDER);

  public static final String ERROR_PRODUCT_NULL = join(ERROR, PRODUCT, NULL);
  public static final String ERROR_PROGRAM_REPOSITORY_NULL = join(ERROR, PROGRAM_REPOSITORY, NULL);
  public static final String ERROR_ORDERABLE_DISPLAY_CATEGORY_REPOSITORY_NULL =
      join(ERROR, ORDERABLE_DISPLAY_CATEGORY_REPOSITORY, NULL);
}
