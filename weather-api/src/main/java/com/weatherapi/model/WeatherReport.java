package com.weatherapi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public record WeatherReport(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        Long id,
        String city,
        String country,
        String description,
        Long timestamp
) {}