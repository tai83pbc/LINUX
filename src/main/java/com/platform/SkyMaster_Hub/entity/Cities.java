package com.platform.SkyMaster_Hub.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "cities")
@Data
public class Cities {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;
    
    @Column(name = "city_code", unique = true, nullable = false)
    private String cityCode;

    @Column(nullable = false)
    private String name;
    
    @Column()
    private String countryCode;
    
    @Column()
    private Double lat;
    
    @Column()
    private Double lng;
}
