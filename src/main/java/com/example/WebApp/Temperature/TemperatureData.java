package com.example.WebApp.Temperature;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Setter
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@Entity
public class TemperatureData {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid4")
    private String id;

    private Long unixMilliSeconds;

    private Double temperature;

}
