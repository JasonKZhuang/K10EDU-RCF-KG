/**
 * Project: SpringBootNeo4j
 * Date: 31/10/2022
 * Author: Jason
 */
package com.zkz.microservice.neo4j.service;

import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Service;

@Service
public class NodeService {

    public String getLabelOfNode( Node node )
    {
        Iterable<String> labels = node.labels();
        if (labels.iterator().hasNext()){
            return labels.iterator().next();
        }
        return "";
    }
}
