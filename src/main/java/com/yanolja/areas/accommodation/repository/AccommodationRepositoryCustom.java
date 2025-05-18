package com.yanolja.areas.accommodation.repository;

import com.yanolja.areas.accommodation.entity.Accommodation;

import java.util.List;
import java.util.Optional;

public interface AccommodationRepositoryCustom {
    
    List<Accommodation> findAllActive();
    
    Optional<Accommodation> findByIdAndNotDeleted(Long id);
} 