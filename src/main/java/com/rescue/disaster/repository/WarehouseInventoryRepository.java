package com.rescue.disaster.repository;

import com.rescue.disaster.entity.WarehouseInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseInventoryRepository extends JpaRepository<WarehouseInventory, Long> {
    List<WarehouseInventory> findByWarehouse_WarehouseId(Long warehouseId);
    Optional<WarehouseInventory> findByWarehouse_WarehouseIdAndSupply_SupplyId(Long warehouseId, Integer supplyId);
}