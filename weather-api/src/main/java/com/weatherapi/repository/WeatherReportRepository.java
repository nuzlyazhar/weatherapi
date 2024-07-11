package com.weatherapi.repository;

import com.weatherapi.model.WeatherReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WeatherReportRepository extends JpaRepository<WeatherReport, Long> {
    Optional<WeatherReport> findFirstByCityAndCountryOrderByTimestampDesc(String city, String country);
}