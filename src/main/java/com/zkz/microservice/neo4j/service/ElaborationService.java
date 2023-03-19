/**
 * Project: EducationMicroservice
 * Date: 18/3/2023
 * Author: Jason
 */
package com.zkz.microservice.neo4j.service;

import com.zkz.microservice.neo4j.configuration.AppProperties;
import com.zkz.microservice.neo4j.dto.ElaborationBean;
import lombok.RequiredArgsConstructor;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ElaborationService {
    private final AppProperties appProperties;
    private final Driver driver;

    public List<ElaborationBean> getElaborationNodesByName(String argName) {
        List<ElaborationBean> myResult = new ArrayList<>();
        try {
            Session session = driver.session();
            myResult = session.readTransaction(tx -> {
                List<ElaborationBean> myBeans = new ArrayList<>();
                String nodeType = "Elaboration";
                Result result = tx.run(
                        "MATCH (t:" + nodeType + ") WHERE t.title ='" + argName + " RETURN t ORDER BY t.id ASC");
                while (result.hasNext()) {
                    org.neo4j.driver.Record record = result.next();
                    Node tmpNode = record.get(0).asNode();
                    // get node id
                    long tmpId = tmpNode.id();
                    // get nodeId properties
                    String nodeId = tmpNode.get("nodeId").asString();
                    // get title properties
                    String tmpName = tmpNode.get("title").asString();
                    // get description properties
                    String tmpDescription = tmpNode.get("description").asString();
                    // get notation properties
                    String tmpNotation = tmpNode.get("notation").asString();
                    // get subSubject properties
                    String tmpSubSubject = tmpNode.get("subSubject").asString();
                    // get yearLevel properties
                    String tmpYearLevel = tmpNode.get("yearLevel").asString();
                    // create return single bean
                    ElaborationBean bean = new ElaborationBean(nodeId,tmpName,tmpDescription,tmpNotation,tmpSubSubject,tmpYearLevel);
                    myBeans.add(bean);
                }
                return myBeans;
            });
            session.close();
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        return myResult;
    }

}
