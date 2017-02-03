package org.openlmis.referencedata;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.flywaydb.core.Flyway;
import org.javers.spring.auditable.AuthorProvider;
import org.openlmis.referencedata.domain.ProgramProductBuilder;
import org.openlmis.referencedata.i18n.ExposedMessageSourceImpl;
import org.openlmis.referencedata.repository.ProductCategoryRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.security.UserNameProvider;
import org.openlmis.referencedata.serializer.ProgramProductBuilderDeserializer;
import org.openlmis.referencedata.validate.ProcessingPeriodValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import java.util.Locale;
import java.util.Objects;

@SpringBootApplication(scanBasePackages = "org.openlmis")
@ImportResource("applicationContext.xml")
public class Application {

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private ProductCategoryRepository productCategoryRepository;

  private Logger logger = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  /**
   * Creates new LocaleResolver.
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


  @Bean
  public AuthorProvider authorProvider() {
    //return new SpringSecurityAuthorProvider();
    return new UserNameProvider();
  }

  /**
   * Configures the Flyway migration strategy to clean the DB before migration first.  This is used
   * as the default unless the Spring Profile "production" is active.
   * @return the clean-migrate strategy
   */
  @Bean
  @Profile("!production")
  public FlywayMigrationStrategy cleanMigrationStrategy() {
    FlywayMigrationStrategy strategy = new FlywayMigrationStrategy() {
      @Override
      public void migrate(Flyway flyway) {
        logger.info("Using clean-migrate flyway strategy -- production profile not active");
        flyway.clean();
        flyway.migrate();
      }
    };

    return strategy;
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
      public JsonDeserializer<?> modifyDeserializer(
          DeserializationConfig config,BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
        Objects.requireNonNull(deserializer, "Jackson passed a null deserializer");
        Objects.requireNonNull(programRepository,
            "Spring Boot didn't autowire the Program Repository");
        Objects.requireNonNull(productCategoryRepository,
            "Spring Boot didn't autowire the Product Category Repository");

        if (beanDesc.getBeanClass() == ProgramProductBuilder.class) {
          return new ProgramProductBuilderDeserializer(
              deserializer, programRepository, productCategoryRepository);
        }

        return deserializer;
      }
    });

    return module;
  }
}