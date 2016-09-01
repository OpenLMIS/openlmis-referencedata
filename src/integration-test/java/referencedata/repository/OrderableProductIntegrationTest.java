package referencedata.repository;

import org.openlmis.referencedata.domain.GlobalProduct;
import org.openlmis.referencedata.domain.OrderableProduct;

import org.openlmis.referencedata.repository.OrderableProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public class OrderableProductIntegrationTest extends BaseCrudRepositoryIntegrationTest<OrderableProduct> {

  @Autowired
  private OrderableProductRepository repository;

  @Override
  CrudRepository<OrderableProduct, UUID> getRepository() {
    return repository;
  }

  @Override
  OrderableProduct generateInstance() {
    return GlobalProduct.newGlobalProduct("abcd", "test", 10);
  }
}
