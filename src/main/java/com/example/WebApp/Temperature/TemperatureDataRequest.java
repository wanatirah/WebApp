package com.example.WebApp.Temperature;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class TemperatureDataRequest {

    private Date date;

    private Date date1;

    private Date date2;
}
