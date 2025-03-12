/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.referencedata.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.MoreExecutors;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.transaction.Transactional;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openlmis.referencedata.Application;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Dispensable;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.OrderableDisplayCategory;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.openlmis.referencedata.domain.TradeItem;
import org.openlmis.referencedata.dto.DispensableDto;
import org.openlmis.referencedata.dto.ImportResponseDto;
import org.openlmis.referencedata.repository.OrderableDisplayCategoryRepository;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.repository.ProgramOrderableRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.TradeItemRepository;
import org.openlmis.referencedata.service.export.DataImportService;
import org.openlmis.referencedata.service.export.OrderableImportPersister;
import org.openlmis.referencedata.service.export.ProgramOrderableImportPersister;
import org.openlmis.referencedata.service.export.TradeItemImportPersister;
import org.openlmis.referencedata.testbuilder.OrderableDataBuilder;
import org.openlmis.referencedata.testbuilder.OrderableDisplayCategoryDataBuilder;
import org.openlmis.referencedata.testbuilder.ProgramDataBuilder;
import org.openlmis.referencedata.testbuilder.ProgramOrderableDataBuilder;
import org.openlmis.referencedata.testbuilder.TradeItemDataBuilder;
import org.openlmis.referencedata.util.TransactionUtils;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class, DataImportService.class})
@ActiveProfiles({"test", "test-run"})
@Transactional
@SuppressWarnings({"PMD.TooManyMethods"})
public class DataImportServiceIntegrationTest {

  private static final String ORDERABLES_FILE = "orderable.csv";
  private static final String PROGRAM_ORDERABLES_FILE = "programOrderable.csv";
  private static final String TRADE_ITEM_FILE = "tradeItem.csv";

  private static final List<String> ORDERABLE_CORRECT_HEADERS = Arrays.asList(
      "productCode", "name", "description", "packRoundingThreshold",
      "packSize", "roundToZero", "dispensable");

  private static final List<String> PROGRAM_ORDERABLE_CORRECT_HEADERS = Arrays.asList(
      "program", "code", "dosesPerPatient", "active",
      "category", "fullSupply", "displayOrder", "pricePerPack");

  private static final List<String> TRADE_ITEM_CORRECT_HEADERS = Arrays.asList(
      "productCode", "manufacturerOfTradeItem");

  private static final String TEST_NAME = "test-name";
  private static final String ORDERABLE_CODE_1 = "0002-1975";
  private static final String ORDERABLE_CODE_2 = "0002-8400";
  private static final String ORDERABLE_CODE_3 = "0009-0050";
  private static final String ORDERABLE_NAME_1 = "Levonorgestrel";
  private static final String ORDERABLE_NAME_2 = "Glucagon";
  private static final String ORDERABLE_NAME_3 = "Medrol";
  private static final String ORDERABLE_DESCRIPTION = "Product description goes here.";
  private static final long ORDERABLE_PACK_ROUNDING_THRESHOLD_1 = 0;
  private static final long ORDERABLE_PACK_ROUNDING_THRESHOLD_2 = 3;
  private static final long ORDERABLE_PACK_ROUNDING_THRESHOLD_3 = 12;
  private static final long ORDERABLE_PACK_SIZE_1 = 1;
  private static final long ORDERABLE_PACK_SIZE_2 = 94;
  private static final long ORDERABLE_PACK_SIZE_3 = 150;
  private static final boolean ORDERABLE_IS_ROUND_TO_ZERO = false;
  private static final String DISPENSING_UNIT_KEY = "dispensingUnit";
  private static final String SIZE_CODE_KEY = "sizeCode";
  private static final String DISPENSING_UNIT_VALUE = "10 tab strip";
  private static final String SIZE_CODE_VALUE = "2 dose";
  private static final Dispensable TEST_DISPENSABLE =
      createDispensable(DISPENSING_UNIT_KEY, TEST_NAME);
  private static final Dispensable ORDERABLE_DISPENSABLE_1 =
      createDispensable(DISPENSING_UNIT_KEY, DISPENSING_UNIT_VALUE);
  private static final Dispensable ORDERABLE_DISPENSABLE_2 =
      createDispensable(SIZE_CODE_KEY, SIZE_CODE_VALUE);
  private static final String PROGRAM_CODE = "PRG002";
  private static final String ORDERABLE_DISPLAY_CATEGORY = "C1";
  private static final Integer PROGRAM_ORDERABLE_DOSES_PER_PATIENT_1 = 10;
  private static final Integer PROGRAM_ORDERABLE_DOSES_PER_PATIENT_2 = 3;
  private static final boolean PROGRAM_ORDERABLE_IS_ACTIVE_1 = true;
  private static final boolean PROGRAM_ORDERABLE_IS_ACTIVE_2 = false;
  private static final boolean PROGRAM_ORDERABLE_IS_FULL_SUPPLY_1 = true;
  private static final boolean PROGRAM_ORDERABLE_IS_FULL_SUPPLY_2 = false;
  private static final int PROGRAM_ORDERABLE_DISPLAY_ORDER_1 = 1;
  private static final int PROGRAM_ORDERABLE_DISPLAY_ORDER_2 = 2;
  private static final double PRICE_PER_PACK_1 = 3.16;
  private static final double PRICE_PER_PACK_2 = 2.13;
  private static final Money PROGRAM_ORDERABLE_PRICE_PER_PACK_1 =
      Money.of(CurrencyUnit.USD, PRICE_PER_PACK_1);
  private static final Money PROGRAM_ORDERABLE_PRICE_PER_PACK_2 =
      Money.of(CurrencyUnit.USD, PRICE_PER_PACK_2);
  private static final String TEST_MANUFACTURER = "TestManufacturer";
  private static final String ITEM_MANUFACTURER_1 = "GlaxoSmithKline";
  private static final String ITEM_MANUFACTURER_2 = "Scandinavian Formulas";
  private static final String ITEM_MANUFACTURER_3 = "Merck";
  private static final String TRADE_ITEM = "tradeItem";
  private static final String COMMODITY_TYPE = "commodityType";

  private static final List<List<String>> ORDERABLE_CORRECT_RECORDS = Arrays.asList(
      Arrays.asList(
          ORDERABLE_CODE_1, ORDERABLE_NAME_1, ORDERABLE_DESCRIPTION,
          String.valueOf(ORDERABLE_PACK_ROUNDING_THRESHOLD_1),
          String.valueOf(ORDERABLE_PACK_SIZE_1),
          String.valueOf(ORDERABLE_IS_ROUND_TO_ZERO),
          DISPENSING_UNIT_KEY + ":" + DISPENSING_UNIT_VALUE),
      Arrays.asList(
          ORDERABLE_CODE_2, ORDERABLE_NAME_2, ORDERABLE_DESCRIPTION,
          String.valueOf(ORDERABLE_PACK_ROUNDING_THRESHOLD_2),
          String.valueOf(ORDERABLE_PACK_SIZE_2),
          String.valueOf(ORDERABLE_IS_ROUND_TO_ZERO),
          SIZE_CODE_KEY + ":" + SIZE_CODE_VALUE),
      Arrays.asList(
          ORDERABLE_CODE_3, ORDERABLE_NAME_3, ORDERABLE_DESCRIPTION,
          String.valueOf(ORDERABLE_PACK_ROUNDING_THRESHOLD_3),
          String.valueOf(ORDERABLE_PACK_SIZE_3),
          String.valueOf(ORDERABLE_IS_ROUND_TO_ZERO),
          DISPENSING_UNIT_KEY + ":" + DISPENSING_UNIT_VALUE)
  );

  private static final List<List<String>> PROGRAM_ORDERABLE_CORRECT_RECORDS = Arrays.asList(
      Arrays.asList(
          PROGRAM_CODE, ORDERABLE_CODE_1, String.valueOf(PROGRAM_ORDERABLE_DOSES_PER_PATIENT_1),
          String.valueOf(PROGRAM_ORDERABLE_IS_ACTIVE_1), ORDERABLE_DISPLAY_CATEGORY,
          String.valueOf(PROGRAM_ORDERABLE_IS_FULL_SUPPLY_1),
          String.valueOf(PROGRAM_ORDERABLE_DISPLAY_ORDER_1),
          String.valueOf(PRICE_PER_PACK_1)),
      Arrays.asList(
          PROGRAM_CODE, ORDERABLE_CODE_2, String.valueOf(PROGRAM_ORDERABLE_DOSES_PER_PATIENT_2),
          String.valueOf(PROGRAM_ORDERABLE_IS_ACTIVE_2), ORDERABLE_DISPLAY_CATEGORY,
          String.valueOf(PROGRAM_ORDERABLE_IS_FULL_SUPPLY_2),
          String.valueOf(PROGRAM_ORDERABLE_DISPLAY_ORDER_2),
          String.valueOf(PRICE_PER_PACK_2))
  );

  private static final List<List<String>> TRADE_ITEM_CORRECT_RECORDS = Arrays.asList(
      Arrays.asList(ORDERABLE_CODE_1, ITEM_MANUFACTURER_1),
      Arrays.asList(ORDERABLE_CODE_2, ITEM_MANUFACTURER_2),
      Arrays.asList(ORDERABLE_CODE_3, ITEM_MANUFACTURER_3)
  );

  private Program persistedProgram;
  private OrderableDisplayCategory persistedOrderableDisplayCategory;

  @Rule
  public EnvironmentVariables environmentVariables = new EnvironmentVariables();

  @Autowired
  private DataImportService dataImportService;

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private OrderableRepository orderableRepository;

  @Autowired
  private OrderableDisplayCategoryRepository orderableDisplayCategoryRepository;

  @Autowired
  private ProgramOrderableRepository programOrderableRepository;

  @Autowired
  private TradeItemRepository tradeItemRepository;

  @Autowired
  private OrderableImportPersister orderableImportPersister;

  @Autowired
  private ProgramOrderableImportPersister programOrderableImportPersister;

  @Autowired
  private TradeItemImportPersister tradeItemImportPersister;

  @Mock
  private Profiler profiler;

  @Mock
  private TransactionUtils transactionUtils;

  @Before
  public void setUp() {
    environmentVariables.set("CURRENCY_CODE", "USD");
    persistedProgram = createAndPersistProgram(PROGRAM_CODE);
    persistedOrderableDisplayCategory =
        createAndPersistOrderableDisplayCategory(ORDERABLE_DISPLAY_CATEGORY);

    when(profiler.startNested(anyString())).thenReturn(profiler);
    when(transactionUtils.runInOwnTransaction(any(Supplier.class)))
        .thenAnswer(invocation -> ((Supplier) invocation.getArgument(0)).get());

    ReflectionTestUtils.setField(
        orderableImportPersister,
        "importExecutorService",
        MoreExecutors.newDirectExecutorService());
    ReflectionTestUtils.setField(
        programOrderableImportPersister,
        "importExecutorService",
        MoreExecutors.newDirectExecutorService());
    ReflectionTestUtils.setField(
        tradeItemImportPersister,
        "importExecutorService",
        MoreExecutors.newDirectExecutorService());

    ReflectionTestUtils.setField(
        orderableImportPersister, "transactionUtils", transactionUtils);
    ReflectionTestUtils.setField(
        programOrderableImportPersister, "transactionUtils", transactionUtils);
    ReflectionTestUtils.setField(
        tradeItemImportPersister, "transactionUtils", transactionUtils);
  }

  @Test
  public void shouldImportOrderablesFromValidCsvFile() throws IOException, InterruptedException {
    // given
    createAndPersistOrderable(ORDERABLE_CODE_1, TEST_NAME,
        TEST_DISPENSABLE);

    MockMultipartFile multipartFile = createZippedCsv(ORDERABLE_CORRECT_RECORDS,
        ORDERABLE_CORRECT_HEADERS, ORDERABLES_FILE);

    // when
    List<ImportResponseDto.ImportDetails> result =
        dataImportService.importData(multipartFile, profiler);

    // then check if result is present
    assertNotNull(result);
    assertEquals((Integer) ORDERABLE_CORRECT_RECORDS.size(),
        result.get(0).getSuccessfulEntriesCount());

    // then fetch imported objects
    Orderable importedOrderable1 = orderableRepository
        .findFirstByProductCodeOrderByIdentityVersionNumberDesc(Code.code(ORDERABLE_CODE_1));
    Orderable importedOrderable2 = orderableRepository
        .findFirstByProductCodeOrderByIdentityVersionNumberDesc(Code.code(ORDERABLE_CODE_2));

    // then check if orderable version numbers were set or updated
    assertEquals((long) importedOrderable1.getVersionIdentity().getVersionNumber(), 2);
    assertEquals((long) importedOrderable2.getVersionIdentity().getVersionNumber(), 1);

    // then check if orderable was updated
    assertOrderables(importedOrderable1, ORDERABLE_NAME_1, ORDERABLE_DESCRIPTION,
        ORDERABLE_PACK_ROUNDING_THRESHOLD_1, ORDERABLE_PACK_SIZE_1,
        ORDERABLE_IS_ROUND_TO_ZERO, ORDERABLE_DISPENSABLE_1);

    // then check if orderable was created
    assertOrderables(importedOrderable2, ORDERABLE_NAME_2, ORDERABLE_DESCRIPTION,
        ORDERABLE_PACK_ROUNDING_THRESHOLD_2, ORDERABLE_PACK_SIZE_2,
        ORDERABLE_IS_ROUND_TO_ZERO, ORDERABLE_DISPENSABLE_2);
  }

  @Test
  public void shouldImportProgramOrderablesFromValidCsvFile()
      throws IOException, InterruptedException {
    // given
    Orderable persistedOrderable = createAndPersistOrderable(
        ORDERABLE_CODE_1, TEST_NAME, TEST_DISPENSABLE);
    createAndPersistOrderable(ORDERABLE_CODE_2, TEST_NAME, TEST_DISPENSABLE);

    createAndPersistProgramOrderable(persistedProgram, persistedOrderable,
        persistedOrderableDisplayCategory);

    MockMultipartFile multipartFile = createZippedCsv(PROGRAM_ORDERABLE_CORRECT_RECORDS,
        PROGRAM_ORDERABLE_CORRECT_HEADERS, PROGRAM_ORDERABLES_FILE);

    // when
    List<ImportResponseDto.ImportDetails> result =
        dataImportService.importData(multipartFile, profiler);

    // then check if result is present
    assertNotNull(result);
    assertEquals((Integer) PROGRAM_ORDERABLE_CORRECT_RECORDS.size(),
        result.get(0).getSuccessfulEntriesCount());

    // then fetch imported objects
    ProgramOrderable importedProgramOrderable1 = programOrderableRepository
        .findByProgramCodeOrderableCodeCategoryCode(
            PROGRAM_CODE, ORDERABLE_CODE_1, ORDERABLE_DISPLAY_CATEGORY);
    ProgramOrderable importedProgramOrderable2 = programOrderableRepository
        .findByProgramCodeOrderableCodeCategoryCode(
            PROGRAM_CODE, ORDERABLE_CODE_2, ORDERABLE_DISPLAY_CATEGORY);

    // then check if programOrderable was updated
    assertProgramOrderables(importedProgramOrderable1, persistedProgram,
        PROGRAM_ORDERABLE_DOSES_PER_PATIENT_1, PROGRAM_ORDERABLE_IS_ACTIVE_1,
        persistedOrderableDisplayCategory, PROGRAM_ORDERABLE_IS_FULL_SUPPLY_1,
        PROGRAM_ORDERABLE_DISPLAY_ORDER_1, PROGRAM_ORDERABLE_PRICE_PER_PACK_1);

    // then check if programOrderable was created
    assertProgramOrderables(importedProgramOrderable2, persistedProgram,
        PROGRAM_ORDERABLE_DOSES_PER_PATIENT_2, PROGRAM_ORDERABLE_IS_ACTIVE_2,
        persistedOrderableDisplayCategory, PROGRAM_ORDERABLE_IS_FULL_SUPPLY_2,
        PROGRAM_ORDERABLE_DISPLAY_ORDER_2, PROGRAM_ORDERABLE_PRICE_PER_PACK_2);
  }

  @Test
  public void shouldImportTradeItemFromValidCsvFile() throws IOException, InterruptedException {
    // given
    final TradeItem persistedTradeItem1 = createAndPersistTradeItem(TEST_MANUFACTURER);
    final TradeItem persistedTradeItem2 = createAndPersistTradeItem(TEST_MANUFACTURER);

    createAndPersistOrderable(
        ORDERABLE_CODE_1, TEST_NAME, TEST_DISPENSABLE,
        Pair.of(COMMODITY_TYPE, persistedTradeItem1.getId().toString()));

    createAndPersistOrderable(
        ORDERABLE_CODE_2, TEST_NAME, TEST_DISPENSABLE,
        Pair.of(TRADE_ITEM, persistedTradeItem2.getId().toString()));

    createAndPersistOrderable(
        ORDERABLE_CODE_3, TEST_NAME, TEST_DISPENSABLE);

    MockMultipartFile multipartFile = createZippedCsv(TRADE_ITEM_CORRECT_RECORDS,
        TRADE_ITEM_CORRECT_HEADERS, TRADE_ITEM_FILE);

    // when
    List<ImportResponseDto.ImportDetails> result =
        dataImportService.importData(multipartFile, profiler);

    // then check if result is present
    assertNotNull(result);
    assertEquals((Integer) ORDERABLE_CORRECT_RECORDS.size(),
        result.get(0).getSuccessfulEntriesCount());

    // then fetch imported objects
    final Orderable importedOrderable1 = orderableRepository
        .findFirstByProductCodeOrderByIdentityVersionNumberDesc(Code.code(ORDERABLE_CODE_1));
    final TradeItem importedTradeItem1 = tradeItemRepository.findById(
        UUID.fromString(importedOrderable1.getTradeItemIdentifier())).get();

    final Orderable importedOrderable2 = orderableRepository
        .findFirstByProductCodeOrderByIdentityVersionNumberDesc(Code.code(ORDERABLE_CODE_2));
    final TradeItem importedTradeItem2 = tradeItemRepository.findById(
        UUID.fromString(importedOrderable2.getTradeItemIdentifier())).get();

    final Orderable importedOrderable3 = orderableRepository
        .findFirstByProductCodeOrderByIdentityVersionNumberDesc(Code.code(ORDERABLE_CODE_3));
    final TradeItem importedTradeItem3 = tradeItemRepository.findById(
        UUID.fromString(importedOrderable3.getTradeItemIdentifier())).get();

    // then check if orderable with commodityType identifier was updated
    assertTrue(importedOrderable1.getIdentifiers().containsKey(TRADE_ITEM));
    assertEquals(importedOrderable1.getTradeItemIdentifier(),
        importedTradeItem1.getId().toString());
    assertEquals(importedTradeItem1.getManufacturerOfTradeItem(), ITEM_MANUFACTURER_1);

    // then check if orderable with existing trade item was updated
    assertTrue(importedOrderable2.getIdentifiers().containsKey(TRADE_ITEM));
    assertEquals(importedOrderable2.getTradeItemIdentifier(),
        importedTradeItem2.getId().toString());
    assertEquals(importedTradeItem2.getManufacturerOfTradeItem(), ITEM_MANUFACTURER_2);

    // then check if orderable without any identifiers was updated
    assertTrue(importedOrderable3.getIdentifiers().containsKey(TRADE_ITEM));
    assertEquals(importedOrderable3.getTradeItemIdentifier(),
        importedTradeItem3.getId().toString());
    assertEquals(importedTradeItem3.getManufacturerOfTradeItem(), ITEM_MANUFACTURER_3);
  }

  private void assertOrderables(Orderable importedOrderable, String name, String description,
                                long packRoundingThreshold, long packSize,
                                boolean isRoundToZero, Dispensable dispensable) {
    assertEquals(name, importedOrderable.getFullProductName());
    assertEquals(description, importedOrderable.getDescription());
    assertEquals(packRoundingThreshold, importedOrderable.getPackRoundingThreshold());
    assertEquals(packSize, importedOrderable.getNetContent());
    assertEquals(isRoundToZero, importedOrderable.isRoundToZero());
    assertEquals(dispensable, importedOrderable.getDispensable());
  }

  private void assertProgramOrderables(ProgramOrderable importedProgramOrderable, Program program,
                                       Integer dosesPerPatient, boolean active,
                                       OrderableDisplayCategory category, boolean fullSupply,
                                       int displayOrder, Money pricePerPack) {
    assertEquals(program, importedProgramOrderable.getProgram());
    assertEquals(dosesPerPatient, importedProgramOrderable.getDosesPerPatient());
    assertEquals(active, importedProgramOrderable.isActive());
    assertEquals(category, importedProgramOrderable.getOrderableDisplayCategory());
    assertEquals(fullSupply, importedProgramOrderable.isFullSupply());
    assertEquals(displayOrder, importedProgramOrderable.getDisplayOrder());
    assertEquals(pricePerPack, importedProgramOrderable.getPricePerPack());
  }

  private static Dispensable createDispensable(String key, String value) {
    Map<String, String> map = new HashMap<>();
    map.put(key, value);
    DispensableDto dto = new DispensableDto();
    dto.setAttributes(map);
    return Dispensable.createNew(dto);
  }

  private Program createAndPersistProgram(String code) {
    Program program = new ProgramDataBuilder()
        .withCode(code)
        .build();
    programRepository.save(program);
    return program;
  }

  private Orderable createAndPersistOrderable(String code, String name, Dispensable dispensable) {
    Orderable orderable = new OrderableDataBuilder().withProductCode(Code.code(code))
        .withFullProductName(name)
        .withDispensable(dispensable)
        .buildAsNew();
    orderableRepository.saveAndFlush(orderable);
    return orderable;
  }

  private Orderable createAndPersistOrderable(String code, String name, Dispensable dispensable,
                                              Pair<String, Object> identifier) {
    Orderable orderable = new OrderableDataBuilder().withProductCode(Code.code(code))
        .withFullProductName(name)
        .withDispensable(dispensable)
        .withIdentifier(identifier.getFirst(), identifier.getSecond())
        .buildAsNew();
    orderableRepository.saveAndFlush(orderable);
    return orderable;
  }

  private OrderableDisplayCategory createAndPersistOrderableDisplayCategory(String code) {
    OrderableDisplayCategory orderableDisplayCategory =
        new OrderableDisplayCategoryDataBuilder().withCode(Code.code(code)).buildAsNew();
    orderableDisplayCategoryRepository.save(orderableDisplayCategory);
    return orderableDisplayCategory;
  }

  private ProgramOrderable createAndPersistProgramOrderable(
      Program program, Orderable orderable, OrderableDisplayCategory orderableDisplayCategory) {
    ProgramOrderable programOrderable = new ProgramOrderableDataBuilder()
        .withProgram(program).withProduct(orderable).withOrderableDisplayCategory(
              orderableDisplayCategory).buildAsNew();
    programOrderableRepository.saveAndFlush(programOrderable);
    return programOrderable;
  }

  private TradeItem createAndPersistTradeItem(String manufacturer) {
    TradeItem tradeItem = new TradeItemDataBuilder()
        .withManufacturerOfTradeItem(manufacturer)
        .buildAsNew();
    tradeItemRepository.saveAndFlush(tradeItem);
    return tradeItem;
  }

  private MockMultipartFile createZippedCsv(List<List<String>> fields,
                                            List<String> headers,
                                            String fileName) throws IOException {
    ByteArrayOutputStream csvOutputStream = new ByteArrayOutputStream();
    OutputStreamWriter csvWriter = new OutputStreamWriter(csvOutputStream);

    CSVPrinter csvPrinter = new CSVPrinter(csvWriter, CSVFormat.DEFAULT);
    csvPrinter.printRecord(headers);
    for (List<String> row: fields) {
      csvPrinter.printRecord(row);
    }
    csvPrinter.close();

    ByteArrayOutputStream zipOutputStream = new ByteArrayOutputStream();
    ZipOutputStream zip = new ZipOutputStream(zipOutputStream);

    ZipEntry zipEntry = new ZipEntry(fileName);
    zip.putNextEntry(zipEntry);
    zip.write(csvOutputStream.toByteArray());
    zip.closeEntry();

    return new MockMultipartFile("test.zip", "test.zip",
        "application/zip", zipOutputStream.toByteArray());
  }

}
