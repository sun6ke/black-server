package com.example.daos;

import com.example.models.College;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by yy on 8/30/15.
 */
@Repository
public interface CollegeRepository extends CrudRepository<College, Integer> {

}
