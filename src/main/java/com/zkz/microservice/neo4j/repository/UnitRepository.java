/**
 * Project: SpringBootNeo4j
 * Date: 30/10/2022
 * Author: Jason
 */
package com.zkz.microservice.neo4j.repository;

import com.zkz.microservice.neo4j.entity.education.UnitNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.List;

public interface UnitRepository extends Neo4jRepository<UnitNode, Long> {
    List<UnitNode> findByTitle(String title);

    void deleteByTitle(String title);
}
