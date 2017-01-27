package org.openlmis.referencedata.repository;

import org.openlmis.referencedata.domain.CommodityType;
import org.openlmis.referencedata.domain.Orderable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public class OrderableIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<Orderable> {

  @Autowired
  private OrderableRepository repository;

  @Override
  CrudRepository<Orderable, UUID> getRepository() {
    return repository;
  }

  @Override
  Orderable generateInstance() {
    return CommodityType.newCommodityType("abcd", "each", "Abcd", "test", 10, 5, false);
  }
}
