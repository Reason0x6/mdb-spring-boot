package com.FlightPub.repository;

import com.FlightPub.model.Airlines;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface AirlineRepo extends MongoRepository<Airlines, String> {

    List<Airlines> findBySponsoredIsTrue();

    @Query(value = "{'_id' : ?0}")
    List<Airlines> findAirline(String id);
}
