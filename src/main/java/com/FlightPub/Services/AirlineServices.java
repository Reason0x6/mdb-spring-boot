package com.FlightPub.Services;

import com.FlightPub.model.Airlines;
import com.FlightPub.repository.AirlineRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("AirlineServices")
public class AirlineServices {
    private AirlineRepo airlineRepo;

    @Autowired
    public AirlineServices(AirlineRepo airlineRepo) {
        this.airlineRepo = airlineRepo;
    }

    public List<Airlines> getSponsoredAirlines() {
        List<Airlines> airlines = airlineRepo.findBySponsoredIsTrue();
        if(!airlines.isEmpty())
            return airlines;
        else
            return null;
    }
}