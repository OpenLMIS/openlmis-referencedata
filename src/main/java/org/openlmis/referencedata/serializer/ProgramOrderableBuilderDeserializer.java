package org.openlmis.referencedata.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import org.openlmis.referencedata.domain.ProgramOrderableBuilder;
import org.openlmis.referencedata.repository.OrderableDisplayCategoryRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.util.messagekeys.ProgramOrderableBuilderDeserializerMessageKeys;

import java.io.IOException;
import java.util.Objects;

/**
 * JSON Deserializer for {@link ProgramOrderableBuilder}.
 * To be used with Jackson's modified default bean deserializer.  This deserializer is needed so
 * that we may inject into the ProgramOrderableBuilder a JPA managed repository that will convert
 * UUID's in the JSON into JPA managed entities.
 */
public class ProgramOrderableBuilderDeserializer extends StdDeserializer<ProgramOrderableBuilder>
    implements ResolvableDeserializer {

  private static final long serialVersionUID = 2923585097168641465L;
  private JsonDeserializer<?> defaultDeserializer;
  private ProgramRepository programRepository;
  private OrderableDisplayCategoryRepository orderableDisplayCategoryRepository;

  private ProgramOrderableBuilderDeserializer() {
    super(ProgramOrderableBuilder.class);
  }

  /**
   * Create deserializer for {@link ProgramOrderableBuilder}.  Builder requires a
   * {@link ProgramRepository} so inject it here.
   * @param defaultDeserializer the default deserializer that will be utilized for all standard bean
   *                            deserialization
   * @param programRepository a JPA instation of a {@link ProgramRepository}.
   * @throws NullPointerException is either parameter is null.
   */
  public ProgramOrderableBuilderDeserializer(JsonDeserializer<?> defaultDeserializer,
                                             ProgramRepository programRepository,
                                             OrderableDisplayCategoryRepository
                                                 orderableDisplayCategoryRepository) {
    super(ProgramOrderableBuilder.class);

    Objects.requireNonNull(defaultDeserializer,
        ProgramOrderableBuilderDeserializerMessageKeys.ERROR_DEFAULT_SERIALIZER_NULL);
    Objects.requireNonNull(programRepository,
        ProgramOrderableBuilderDeserializerMessageKeys.ERROR_PROGRAM_REPOSITORY_NULL);
    Objects.requireNonNull(orderableDisplayCategoryRepository,
        ProgramOrderableBuilderDeserializerMessageKeys
            .ERROR_ORDERABLE_DISPLAY_CATEGORY_REPOSITORY_NULL);
    this.defaultDeserializer = defaultDeserializer;
    this.programRepository = programRepository;
    this.orderableDisplayCategoryRepository = orderableDisplayCategoryRepository;
  }

  @Override
  public ProgramOrderableBuilder deserialize(JsonParser jsonParser, DeserializationContext ctxt)
      throws IOException {
    Objects.requireNonNull(programRepository,
        ProgramOrderableBuilderDeserializerMessageKeys.ERROR_PROGRAM_REPOSITORY_NOT_INJECTED);

    // default bean deserialization
    ProgramOrderableBuilder ppBuilder = (ProgramOrderableBuilder) defaultDeserializer.deserialize(
        jsonParser,
        ctxt);

    // inject repositories into builder so that it may lookup entity ids
    ppBuilder.setProgramRepository(programRepository);
    ppBuilder.setOrderableDisplayCategoryRepository(orderableDisplayCategoryRepository);

    return ppBuilder;
  }

  @Override
  public void resolve(DeserializationContext ctxt) throws JsonMappingException {
    ((ResolvableDeserializer) defaultDeserializer).resolve(ctxt);
  }
}
