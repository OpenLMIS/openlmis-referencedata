package org.openlmis.referencedata.util.messagekeys;

public abstract class ProductCategoryMessageKeys extends MessageKeys {
  private static final String ERROR = join(SERVICE_ERROR, PRODUCT_CATEGORY);

  public static final String ERROR_NOT_FOUND = join(ERROR, NOT_FOUND);
  public static final String ERROR_NOT_FOUND_WITH_ID = join(ERROR_NOT_FOUND, WITH, ID);
  public static final String ERROR_DELETING_WITH_ID = join(ERROR, DELETING, WITH, ID);
}
