/**
 * Project: EducationMicroservice
 * Date: 10/3/2023
 * Author: Jason
 */
package com.zkz.microservice.neo4j.repository;

import com.zkz.microservice.neo4j.entity.education.ElementNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface ElementRepository extends Neo4jRepository<ElementNode, Long> {

}
