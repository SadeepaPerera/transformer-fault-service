package com.example.transformer_fault_service.repository;

import com.example.transformer_fault_service.model.TmsImageFaultAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TmsImageFaultAnalysisRepository extends JpaRepository<TmsImageFaultAnalysis, Long> {
    Optional<TmsImageFaultAnalysis> findByImageId(Long imageId);
}
