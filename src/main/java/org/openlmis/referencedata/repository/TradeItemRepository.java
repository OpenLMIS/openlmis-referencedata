package org.openlmis.referencedata.repository;

import org.openlmis.referencedata.domain.GlobalProduct;
import org.openlmis.referencedata.domain.TradeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TradeItemRepository extends CrudRepository<TradeItem, UUID>,
    JpaRepository<TradeItem, UUID> {

  @Query("SELECT p FROM TradeItem p WHERE p.globalProduct = :globalProduct")
  List<TradeItem> findForGlobalProduct(@Param("globalProduct") GlobalProduct product);
}
