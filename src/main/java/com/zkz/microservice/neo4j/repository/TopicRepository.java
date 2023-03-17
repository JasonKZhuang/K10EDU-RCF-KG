/**
 * Project: SpringBootNeo4j
 * Date: 22/11/2022
 * Author: Jason
 */
package com.zkz.microservice.neo4j.repository;

import com.zkz.microservice.neo4j.entity.education.TopicNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.List;

public interface TopicRepository extends Neo4jRepository<TopicNode, Long> {
    List<TopicNode> findByTitle(String title);

    void deleteByTitle(String title);
}
