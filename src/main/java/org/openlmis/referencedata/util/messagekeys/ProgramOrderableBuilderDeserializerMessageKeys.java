package org.openlmis.referencedata.util.messagekeys;

public abstract class ProgramOrderableBuilderDeserializerMessageKeys extends MessageKeys {
  private static final String PROGRAM_ORDERABLE_BUILDER_DESERIALIZER =
      "programOrderableBuilderDeserializer";
  private static final String NOT_INJECTED = "notInjected";
  private static final String ERROR = join(SERVICE_ERROR, PROGRAM_ORDERABLE_BUILDER_DESERIALIZER);

  public static final String ERROR_PROGRAM_REPOSITORY_NULL = join(ERROR, PROGRAM_REPOSITORY, NULL);
  public static final String ERROR_DEFAULT_SERIALIZER_NULL = join(ERROR, "defaultSerializer", NULL);
  public static final String ERROR_ORDERABLE_DISPLAY_CATEGORY_REPOSITORY_NULL =
      join(ERROR, ORDERABLE_DISPLAY_CATEGORY_REPOSITORY, NULL);
  public static final String ERROR_PROGRAM_REPOSITORY_NOT_INJECTED =
      join(ERROR, PROGRAM_REPOSITORY, NOT_INJECTED);
}
