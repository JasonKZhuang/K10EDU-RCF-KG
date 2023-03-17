/**
 * Project: SpringBootNeo4j
 * Date: 23/10/2022
 * Author: Jason
 */
package com.zkz.microservice.neo4j.entity.education;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.*;

@Data
@RelationshipProperties
public class LearnedOutcome {

    @Id
    @GeneratedValue
    private Long id;

    @Property("nodeId")
    private String nodeId;

    @Property("name")
    private String name;

    @TargetNode
    private OutcomeNode outcome;
}
