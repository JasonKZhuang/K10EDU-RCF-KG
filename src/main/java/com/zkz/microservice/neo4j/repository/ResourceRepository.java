package com.zkz.microservice.neo4j.repository;

import com.zkz.microservice.neo4j.entity.education.ResourceNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.List;

public interface ResourceRepository extends Neo4jRepository<ResourceNode, Long> {
    List<ResourceNode> findByTitle(String title);

    void deleteByTitle(String title);
}