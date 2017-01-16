package org.openlmis.referencedata.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import org.openlmis.referencedata.domain.ProgramProductBuilder;
import org.openlmis.referencedata.repository.ProductCategoryRepository;
import org.openlmis.referencedata.repository.ProgramRepository;

import java.io.IOException;
import java.util.Objects;

/**
 * JSON Deserializer for {@link ProgramProductBuilder}.  To be used with Jackson's modified default
 * bean deserializer.  This deserializer is needed so that we may inject into the
 * ProgramProductBuilder a JPA managed repository that will convert UUID's in the JSON into JPA
 * managed entities.
 */
public class ProgramProductBuilderDeserializer extends StdDeserializer<ProgramProductBuilder>
    implements ResolvableDeserializer {

  private static final long serialVersionUID = 2923585097168641465L;
  private JsonDeserializer<?> defaultDeserializer;
  private ProgramRepository programRepository;
  private ProductCategoryRepository productCategoryRepository;

  private ProgramProductBuilderDeserializer() {
    super(ProgramProductBuilder.class);
  }

  /**
   * Create deserializer for {@link ProgramProductBuilder}.  Builder requires a
   * {@link ProgramRepository} so inject it here.
   * @param defaultDeserializer the default deserializer that will be utilized for all standard bean
   *                            deserialization
   * @param programRepository a JPA instation of a {@link ProgramRepository}.
   * @throws NullPointerException is either parameter is null.
   */
  public ProgramProductBuilderDeserializer(JsonDeserializer<?> defaultDeserializer,
                                           ProgramRepository programRepository,
                                           ProductCategoryRepository productCategoryRepository) {
    super(ProgramProductBuilder.class);

    Objects.requireNonNull(defaultDeserializer,
        "referenceData.error.programProductBuilderDeserializer.defaultSerializer.null");
    Objects.requireNonNull(programRepository,
        "referenceData.error.programProductBuilderDeserializer.programRepository.null");
    Objects.requireNonNull(productCategoryRepository,
        "referenceData.error.programProductBuilderDeserializer.productCategoryRepository.null");
    this.defaultDeserializer = defaultDeserializer;
    this.programRepository = programRepository;
    this.productCategoryRepository = productCategoryRepository;
  }

  @Override
  public ProgramProductBuilder deserialize(JsonParser jsonParser, DeserializationContext ctxt)
      throws IOException {
    Objects.requireNonNull(programRepository,
        "referenceData.error.programProductBuilderDeserializer.programRepository.notInjected");

    // default bean deserialization
    ProgramProductBuilder ppBuilder = (ProgramProductBuilder) defaultDeserializer.deserialize(
        jsonParser,
        ctxt);

    // inject repositories into builder so that it may lookup entity ids
    ppBuilder.setProgramRepository(programRepository);
    ppBuilder.setProductCategoryRepository(productCategoryRepository);

    return ppBuilder;
  }

  @Override
  public void resolve(DeserializationContext ctxt) throws JsonMappingException {
    ((ResolvableDeserializer) defaultDeserializer).resolve(ctxt);
  }
}
