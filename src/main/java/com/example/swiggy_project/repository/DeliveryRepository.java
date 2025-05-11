package com.example.swiggy_project.repository;

import com.example.swiggy_project.model.Delivery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryRepository extends MongoRepository<Delivery, String> {
    List<Delivery> findByDeliveryBoyId(String deliveryBoyId);
}