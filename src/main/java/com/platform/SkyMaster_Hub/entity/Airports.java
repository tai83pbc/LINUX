package com.platform.SkyMaster_Hub.entity;
import jakarta.persistence.*;
import lombok.Data;

@Entity 
@Table(name ="airports") 
@Data() 
public class Airports {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column()
    private String icaoCode;
    
    @Column()
    private String iataCode;
    
    private String name;
    private Double lat;
    private Double lng;
    
    @Column()
    private String countryCode;
    
    private String city;
}
