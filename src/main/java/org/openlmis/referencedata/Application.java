package org.openlmis.referencedata;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.javers.core.Javers;
import org.javers.core.MappingStyle;
import org.javers.core.diff.ListCompareAlgorithm;
import org.javers.hibernate.integration.HibernateUnproxyObjectAccessHook;
import org.javers.repository.sql.ConnectionProvider;
import org.javers.repository.sql.DialectName;
import org.javers.repository.sql.JaversSqlRepository;
import org.javers.repository.sql.SqlRepositoryBuilder;
import org.javers.spring.auditable.AuthorProvider;
import org.javers.spring.boot.sql.JaversProperties;
import org.javers.spring.jpa.TransactionalJaversBuilder;
import org.openlmis.referencedata.domain.ProgramOrderableBuilder;
import org.openlmis.referencedata.i18n.ExposedMessageSourceImpl;
import org.openlmis.referencedata.repository.OrderableDisplayCategoryRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.security.UserNameProvider;
import org.openlmis.referencedata.serializer.ProgramOrderableBuilderDeserializer;
import org.openlmis.referencedata.validate.ProcessingPeriodValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.transaction.PlatformTransactionManager;
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
  private OrderableDisplayCategoryRepository orderableDisplayCategoryRepository;

  @Autowired
  DialectName dialectName;

  @Autowired
  private JaversProperties javersProperties;

  @Value("${spring.jpa.properties.hibernate.default_schema}")
  private String preferredSchema;


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



  /**
   * Create and return a UserNameProvider. By default, if we didn't do so, an instance of
   * SpringSecurityAuthorProvider would automatically be created and returned instead.
   */
  @Bean
  public AuthorProvider authorProvider() {
    return new UserNameProvider();
  }

  /**
   * Create and return an instance of JaVers precisely configured as necessary.
   * This is particularly helpful for getting JaVers to create and use tables
   * within a particular schema (specified via the withSchema method).
   *
   * @See <a href="https://github.com/javers/javers/blob/master/javers-spring-boot-starter-sql/src
   * /main/java/org/javers/spring/boot/sql/JaversSqlAutoConfiguration.java">
   * JaversSqlAutoConfiguration.java</a> for the default configuration upon which this code is based
   */
  @Bean
  public Javers javersProvidor(ConnectionProvider connectionProvider,
                               PlatformTransactionManager transactionManager) {
    JaversSqlRepository sqlRepository = SqlRepositoryBuilder
            .sqlRepository()
            .withConnectionProvider(connectionProvider)
            .withDialect(dialectName)
            .withSchema(preferredSchema)
            .build();

    return TransactionalJaversBuilder
            .javers()
            .withTxManager(transactionManager)
            .registerJaversRepository(sqlRepository)
            .withObjectAccessHook(new HibernateUnproxyObjectAccessHook())
            .withListCompareAlgorithm(
                    ListCompareAlgorithm.valueOf(javersProperties.getAlgorithm().toUpperCase()))
            .withMappingStyle(
                    MappingStyle.valueOf(javersProperties.getMappingStyle().toUpperCase()))
            .withNewObjectsSnapshot(javersProperties.isNewObjectSnapshot())
            .withPrettyPrint(javersProperties.isPrettyPrint())
            .withTypeSafeValues(javersProperties.isTypeSafeValues())
            .withPackagesToScan(javersProperties.getPackagesToScan())
            .build();
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
   * {@link com.fasterxml.jackson.databind.ObjectMapper} for {@link ProgramOrderableBuilder}.  This
   * is useful for overriding the default deserializer that SpringBoot is using.
   * @return a Jackson module that SpringBoot will register with Jackson.
   */
  @Bean
  public Module registerProgramOrderableBuilderDeserializer() {
    SimpleModule module = new SimpleModule();
    module.setDeserializerModifier(new BeanDeserializerModifier() {
      @Override
      public JsonDeserializer<?> modifyDeserializer(
          DeserializationConfig config,BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
        Objects.requireNonNull(deserializer, "Jackson passed a null deserializer");
        Objects.requireNonNull(programRepository,
            "Spring Boot didn't autowire the Program Repository");
        Objects.requireNonNull(orderableDisplayCategoryRepository,
            "Spring Boot didn't autowire the Orderable Display Category Repository");

        if (beanDesc.getBeanClass() == ProgramOrderableBuilder.class) {
          return new ProgramOrderableBuilderDeserializer(
              deserializer, programRepository, orderableDisplayCategoryRepository);
        }

        return deserializer;
      }
    });

    return module;
  }
}