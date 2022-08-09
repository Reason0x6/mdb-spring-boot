package com.FlightPub.repository;


import com.FlightPub.model.Price;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Date;
import java.util.List;

public interface PriceRepo extends MongoRepository<Price, String> {

    @Query(value="{ 'FlightNumber' : { '$regex' : ?0 , $options: 'i'},  'StartDate' : ?1 }", fields="{ 'destination' : 1 }")
    List<Price> findPrices(String FlightNumber, Date StartDate);

    @Query(value="{ 'FlightNumber' : ?0,  'TicketCode' :  ?1 }")
    List<Price> findPriceByClassTicketCode(String FlightNumber, String TicketCode);
}
