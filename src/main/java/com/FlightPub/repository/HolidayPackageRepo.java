package com.FlightPub.repository;

import com.FlightPub.model.HolidayPackage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HolidayPackageRepo extends MongoRepository<HolidayPackage, String> {
}
