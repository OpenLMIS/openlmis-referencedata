package org.openlmis.referencedata;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.openlmis.referencedata.domain.ProgramProductBuilder;
import org.openlmis.referencedata.i18n.ExposedMessageSourceImpl;
import org.openlmis.referencedata.repository.ProductCategoryRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.validate.ProcessingPeriodValidator;
import org.openlmis.referencedata.web.ProgramProductBuilderDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import java.util.Locale;
import java.util.Objects;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "org.openlmis")
@ImportResource("applicationContext.xml")
public class Application {

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private ProductCategoryRepository productCategoryRepository;

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  /**
   * Creates new LocaleResolve.
   *
   * @return Created LocalResolver.
   */
  @Bean
  public LocaleResolver localeResolver() {
    CookieLocaleResolver lr = new CookieLocaleResolver();
    lr.setCookieName("lang");
    lr.setDefaultLocale(Locale.ENGLISH);
    return lr;
  }

  /**
   * Creates new MessageSource.
   *
   * @return Created MessageSource.
   */
  @Bean
  public ExposedMessageSourceImpl messageSource() {
    ExposedMessageSourceImpl messageSource = new ExposedMessageSourceImpl();
    messageSource.setBasename("classpath:messages");
    messageSource.setDefaultEncoding("UTF-8");
    messageSource.setUseCodeAsDefaultMessage(true);
    return messageSource;
  }

  @Bean
  public ProcessingPeriodValidator beforeCreatePeriodValidator() {
    return new ProcessingPeriodValidator();
  }

  @Bean
  public ProcessingPeriodValidator beforeSavePeriodValidator() {
    return new ProcessingPeriodValidator();
  }

  /**
   * Registers a modified deserializer with Jackson's
   * {@link com.fasterxml.jackson.databind.ObjectMapper} for {@link ProgramProductBuilder}.  This
   * is useful for overriding the default deserializer that SpringBoot is using.
   * @return a Jackson module that SpringBoot will register with Jackson.
   */
  @Bean
  public Module registerProgramProductBuilderDeserializer() {
    SimpleModule module = new SimpleModule();
    module.setDeserializerModifier(new BeanDeserializerModifier() {
      @Override
      public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
                                                    BeanDescription beanDesc,
                                                    JsonDeserializer<?> deserializer) {
        Objects.requireNonNull(deserializer, "Jackson passed a null deserializer");
        Objects.requireNonNull(programRepository, "Spring Boot didn't autowire the "
            + "Program Repository");
        Objects.requireNonNull(productCategoryRepository, "Spring didn't autowire the product "
            + "category repository");

        if (beanDesc.getBeanClass() == ProgramProductBuilder.class) {
          return new ProgramProductBuilderDeserializer(deserializer,
            programRepository,
            productCategoryRepository);
        }

        return deserializer;
      }
    } );

    return module;
  }
}