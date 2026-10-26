package com.rescue.disaster.service;

import com.rescue.disaster.dto.ReliefDonationDto;
import com.rescue.disaster.entity.*;
import com.rescue.disaster.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LogisticsService {

    private final WarehouseRepository warehouseRepository;
    private final SupplyRepository supplyRepository;
    private final WarehouseInventoryRepository inventoryRepository;
    private final ReliefDonationRepository donationRepository;
    private final ShelterRepository shelterRepository;

    @Autowired
    public LogisticsService(WarehouseRepository warehouseRepository,
                            SupplyRepository supplyRepository,
                            WarehouseInventoryRepository inventoryRepository,
                            ReliefDonationRepository donationRepository,
                            ShelterRepository shelterRepository) {
        this.warehouseRepository = warehouseRepository;
        this.supplyRepository = supplyRepository;
        this.inventoryRepository = inventoryRepository;
        this.donationRepository = donationRepository;
        this.shelterRepository = shelterRepository;
    }

    public List<Warehouse> getAllWarehouses() {
        return warehouseRepository.findAll();
    }

    public List<Supply> getAllSupplies() {
        return supplyRepository.findAll();
    }

    public List<WarehouseInventory> getInventoriesByWarehouse(Long warehouseId) {
        return inventoryRepository.findByWarehouse_WarehouseId(warehouseId);
    }

    // Cập nhật tồn kho (Nhập thêm hoặc Xuất đi)
    @Transactional
    public WarehouseInventory adjustStock(Long warehouseId, Integer supplyId, int changeAmount) {
        WarehouseInventory inventory = inventoryRepository
                .findByWarehouse_WarehouseIdAndSupply_SupplyId(warehouseId, supplyId)
                .orElseGet(() -> {
                    Warehouse w = warehouseRepository.findById(warehouseId)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy kho"));
                    Supply s = supplyRepository.findById(supplyId)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy vật tư"));
                    return WarehouseInventory.builder().warehouse(w).supply(s).quantity(0).build();
                });

        int newQty = inventory.getQuantity() + changeAmount;
        if (newQty < 0) {
            throw new IllegalArgumentException("Số lượng xuất kho không thể vượt quá số lượng hiện có!");
        }

        inventory.setQuantity(newQty);
        inventory.setLastUpdated(LocalDateTime.now());
        return inventoryRepository.save(inventory);
    }

    // Tiếp nhận đăng ký từ nhà hảo tâm
    @Transactional
    public ReliefDonation registerDonation(ReliefDonationDto dto) {
        Shelter shelter = null;
        if (dto.getDestinationShelterId() != null) {
            shelter = shelterRepository.findById(dto.getDestinationShelterId()).orElse(null);
        }

        ReliefDonation donation = ReliefDonation.builder()
                .donorName(dto.getDonorName())
                .phoneNumber(dto.getPhoneNumber())
                .organization(dto.getOrganization())
                .supplyType(dto.getSupplyType())
                .quantityDetail(dto.getQuantityDetail())
                .destinationShelter(shelter)
                .note(dto.getNote())
                .status("REGISTERED")
                .build();

        return donationRepository.save(donation);
    }

    public List<ReliefDonation> getAllDonations() {
        return donationRepository.findAllWithShelter();
    }
}