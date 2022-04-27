package com.example.WebApp.Temperature;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemperatureDataRepository extends JpaRepository<TemperatureData, String> {

    TemperatureData findByTemperature(Double temperature);

    TemperatureData findByDate(Long unixMilliSecond);

    TemperatureData findByDateToDate(Long unix1, Long unix2);

}
