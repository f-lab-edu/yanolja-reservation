package com.yanolja.areas.accommodation.repository;

import com.yanolja.areas.accommodation.entity.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;
import java.util.Optional;

public interface AccommodationRepository extends JpaRepository<Accommodation, Long>, AccommodationRepositoryCustom {

}