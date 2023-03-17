/**
 * Project: SpringBootNeo4j
 * Date: 22/11/2022
 * Author: Jason
 */
package com.zkz.microservice.neo4j.repository;

import com.zkz.microservice.neo4j.entity.education.SubTopicNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.List;

public interface SubTopicRepository extends Neo4jRepository<SubTopicNode, Long> {
    List<SubTopicNode> findByTitle(String title);

    void deleteByTitle(String title);
}
