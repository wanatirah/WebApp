package com.example.WebApp.Temperature;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TemperatureDataService {

    @Autowired
    private TemperatureDataRepository repository;

    public TemperatureData findByTemperature(Double temperature) {
        return repository.findByTemperature(temperature);
    }

    public TemperatureData findByDate(Long unixMilliSeconds) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        TemperatureDataRequest request = new TemperatureDataRequest();
        request.getDate();

        Date date = sdf.parse(String.valueOf(request));
        unixMilliSeconds = date.getTime();

        return repository.findByDate(unixMilliSeconds);
    }

    public TemperatureData findByDateToDate(Long unix1, Long unix2) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        TemperatureDataRequest request = new TemperatureDataRequest();

        Date date1 = sdf.parse(String.valueOf(request.getDate1()));
        unix1 = date1.getTime();

        Date date2 = sdf.parse(String.valueOf(request.getDate1()));
        unix2 = date2.getTime();

        return repository.findByDateToDate(unix1, unix2);
    }
}
