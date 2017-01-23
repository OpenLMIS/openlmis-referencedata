package org.openlmis.referencedata.util.messagekeys;

public abstract class ProgramProductBuilderMessageKeys extends MessageKeys {
  private static final String PROGRAM_PRODUCT_BUILDER = "programProductBuilder";
  private static final String ERROR = join(SERVICE_ERROR, PROGRAM_PRODUCT_BUILDER);

  public static final String ERROR_PRODUCT_NULL = join(ERROR, PRODUCT, NULL);
  public static final String ERROR_PROGRAM_REPOSITORY_NULL = join(ERROR, PROGRAM_REPOSITORY, NULL);
  public static final String ERROR_PRODUCT_CATEGORY_REPOSITORY_NULL =
      join(ERROR, PRODUCT_CATEGORY_REPOSITORY, NULL);
}
