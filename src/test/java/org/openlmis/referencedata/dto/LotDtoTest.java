package org.openlmis.referencedata.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.Lot;
import org.openlmis.referencedata.testbuilder.LotDataBuilder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class LotDtoTest {

  @Test
  public void newInstanceShouldCreateListOfDtosFromLots() {
    Lot lotOne = new LotDataBuilder().build();
    Lot lotTwo = new LotDataBuilder().build();

    List<LotDto> result = LotDto.newInstance(Arrays.asList(lotOne, lotTwo));

    assertEquals(lotOne.getId(), result.get(0).getId());
    assertEquals(lotOne.getExpirationDate(), result.get(0).getExpirationDate());
    assertEquals(lotOne.getLotCode(), result.get(0).getLotCode());
    assertEquals(lotOne.getManufactureDate(), result.get(0).getManufactureDate());
    assertEquals(lotOne.getTradeItem().getId(), result.get(0).getTradeItemId());

    assertEquals(lotTwo.getId(), result.get(1).getId());
    assertEquals(lotTwo.getExpirationDate(), result.get(1).getExpirationDate());
    assertEquals(lotTwo.getLotCode(), result.get(1).getLotCode());
    assertEquals(lotTwo.getManufactureDate(), result.get(1).getManufactureDate());
    assertEquals(lotTwo.getTradeItem().getId(), result.get(1).getTradeItemId());
  }

  @Test
  public void newInstanceShouldCreateDtoFromLot() {
    Lot lot = new LotDataBuilder().build();

    LotDto result = LotDto.newInstance(lot);

    assertEquals(lot.getId(), result.getId());
    assertEquals(lot.getExpirationDate(), result.getExpirationDate());
    assertEquals(lot.getLotCode(), result.getLotCode());
    assertEquals(lot.getManufactureDate(), result.getManufactureDate());
    assertEquals(lot.getTradeItem().getId(), result.getTradeItemId());
  }

  @Test
  public void equalsShouldReturnTrueIfLotCodesAreEqual() {
    LotDto left = LotDto.newInstance(
        new LotDataBuilder()
            .withExpirationDate(LocalDate.now())
            .withLotCode("LC01")
            .build()
    );

    LotDto right = LotDto.newInstance(
        new LotDataBuilder()
            .withExpirationDate(LocalDate.now().plusDays(1))
            .withLotCode("LC01")
            .build()
    );

    assertTrue(left.equals(right));
  }

  @Test
  public void equalsShouldReturnFalseIfLotCodesAreNotEqual() {
    LotDto left = LotDto.newInstance(
        new LotDataBuilder()
            .withExpirationDate(LocalDate.now())
            .withLotCode("LC01")
            .build()
    );

    LotDto right = LotDto.newInstance(
        new LotDataBuilder()
            .withExpirationDate(LocalDate.now().plusDays(1))
            .withLotCode("LC02")
            .build()
    );

    assertFalse(left.equals(right));
  }

}