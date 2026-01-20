package com.platform.SkyMaster_Hub.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "countries")
@Data
public class Countries {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String code;
    
    @Column()
    private String code3;
    
    @Column(nullable = false)
    private String name;
}
