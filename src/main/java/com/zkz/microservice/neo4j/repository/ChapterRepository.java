/**
 * Project: SpringBootNeo4j
 * Date: 23/10/2022
 * Author: Jason
 */
package com.zkz.microservice.neo4j.repository;

import com.zkz.microservice.neo4j.entity.education.ChapterNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.List;

public interface ChapterRepository extends Neo4jRepository<ChapterNode, Long> {
    List<ChapterNode> findByTitle(String title);
    void deleteByTitle(String title);

}
