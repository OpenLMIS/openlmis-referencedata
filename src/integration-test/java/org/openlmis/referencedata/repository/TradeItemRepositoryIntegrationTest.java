package org.openlmis.referencedata.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.openlmis.referencedata.domain.GlobalProduct;
import org.openlmis.referencedata.domain.TradeItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public class TradeItemRepositoryIntegrationTest extends
    BaseCrudRepositoryIntegrationTest<TradeItem> {

  @Autowired
  private TradeItemRepository repository;

  @Autowired
  private GlobalProductRepository globalProductRepository;

  @Override
  CrudRepository<TradeItem, UUID> getRepository() {
    return repository;
  }

  @Override
  TradeItem generateInstance() {
    return TradeItem.newTradeItem("advil" + getNextInstanceNumber(), "each",
        "Advil" + getNextInstanceNumber(), 10, 5, false);
  }

  @Test
  public void findByGlobalProductShouldReturnIds() {
    // setup some trade items for the global product and some not
    TradeItem tradeItem1 = generateInstance();
    TradeItem tradeItem2 = generateInstance();
    List<TradeItem> forFulfillment = Lists.newArrayList(tradeItem1, tradeItem2);
    repository.save(forFulfillment);
    TradeItem tradeItemOther = generateInstance();
    repository.save(tradeItemOther);
    assertEquals(3, repository.count());

    // save the global product with the trade items
    GlobalProduct ibuprofen = GlobalProduct.newGlobalProduct(
        "ibuprofen", "each", "Ibuprofen", "test", 1, 0, false);
    ibuprofen.setTradeItems(Sets.newHashSet(forFulfillment));
    globalProductRepository.save(ibuprofen);

    // find the trade items for the global product above and ensure its right
    List<TradeItem> foundTradeItems = repository.findForGlobalProduct(ibuprofen);
    assertNotNull(foundTradeItems);
    assertEquals(2, foundTradeItems.size());
    assertTrue(foundTradeItems.containsAll(forFulfillment));
  }
}
