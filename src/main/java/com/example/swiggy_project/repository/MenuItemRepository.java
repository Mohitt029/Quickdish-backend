package com.example.swiggy_project.repository;

import com.example.swiggy_project.model.MenuItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface MenuItemRepository extends MongoRepository<MenuItem, String> {
    List<MenuItem> findByFoodMenuIdAndCuisineType(String foodMenuId, String cuisineType);

    List<MenuItem> findByFoodMenuIdAndMealType(String foodMenuId, String mealType);

    List<MenuItem> findByCuisineTypeIn(List<String> cuisineTypes);

    @Query(value = "{}", sort = "{'numberOfTimesOrdered': -1}")
    List<MenuItem> findTop5ByOrderByNumberOfTimesOrderedDesc();

    List<MenuItem> findByFoodMenuId(String id);
}