package org.openlmis.referencedata.util.messagekeys;

public abstract class ProgramProductBuilderDeserializerMessageKeys extends MessageKeys {
  private static final String PROGRAM_PRODUCT_BUILDER_DESERIALIZER =
      "programProductBuilderDeserializer";
  private static final String NOT_INJECTED = "notInjected";
  private static final String ERROR = join(SERVICE_ERROR, PROGRAM_PRODUCT_BUILDER_DESERIALIZER);

  public static final String ERROR_PROGRAM_REPOSITORY_NULL = join(ERROR, PROGRAM_REPOSITORY, NULL);
  public static final String ERROR_DEFAULT_SERIALIZER_NULL = join(ERROR, "defaultSerializer", NULL);
  public static final String ERROR_PRODUCT_CATEGORY_REPOSITORY_NULL =
      join(ERROR, PRODUCT_CATEGORY_REPOSITORY, NULL);
  public static final String ERROR_PROGRAM_REPOSITORY_NOT_INJECTED =
      join(ERROR, PROGRAM_REPOSITORY, NOT_INJECTED);
}
