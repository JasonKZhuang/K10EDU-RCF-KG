/**
 * Project: SpringBootNeo4j
 * Date: 23/10/2022
 * Author: Jason
 */
package com.zkz.microservice.neo4j.repository;

import com.zkz.microservice.neo4j.entity.education.OutcomeNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.List;

public interface OutcomeRepository extends Neo4jRepository<OutcomeNode, Long> {
    List<OutcomeNode> findByTitle(String title);

    void deleteByTitle(String title);
}
