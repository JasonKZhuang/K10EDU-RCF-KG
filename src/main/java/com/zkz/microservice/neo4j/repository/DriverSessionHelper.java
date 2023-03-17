/**
 * Project: SpringBootNeo4j
 * Date: 28/11/2022
 * Author: Jason
 */
package com.zkz.microservice.neo4j.repository;

import com.zkz.microservice.neo4j.configuration.NodeLabel;
import com.zkz.microservice.neo4j.dto.ChapterBean;
import com.zkz.microservice.neo4j.dto.ResourceBean;
import com.zkz.microservice.neo4j.dto.TopicBean;
import com.zkz.microservice.neo4j.dto.UnitBean;
import com.zkz.microservice.neo4j.exception.ServiceException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@AllArgsConstructor
@Slf4j
public class DriverSessionHelper {
    private final Driver driver;

    /**
     * @param statement
     * @param params
     */
    public void delete(String statement, Map<String, Object> params) {
        try (Session session = driver.session()) {
            Result myResult = session.writeTransaction(tx -> {
                Result result = tx.run(statement, params);
                return result;
            });
            log.info(myResult.consume().toString());
        } catch (Exception ex) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * @param nodeType
     * @param argId
     * @return
     */
    public Object getNodeById(NodeLabel nodeType, Long argId) {
        String statement = String.format(" MATCH (n:%s) WHERE ID(n) = %d  RETURN n", nodeType.getNodeType(), argId);
        return getNodeByStatement(nodeType, statement);
    }

    /**
     * @param nodeType
     * @param argTitle
     * @return
     */
    public Object getNodeByTitle(NodeLabel nodeType, String argTitle) {
        String statement = String.format(" MATCH (n:%s) WHERE n.title = %s  RETURN n", nodeType.getNodeType(), argTitle);
        return getNodeByStatement(nodeType, statement);
    }

    /**
     * @param nodeType
     * @param statement
     * @return
     */
    private Object getNodeByStatement(NodeLabel nodeType, String statement){
        Object myResult = null;
        try {
            Session session = driver.session();
            myResult = session.readTransaction(tx -> {
                Result result = tx.run(statement);
                while (result.hasNext()) {
                    org.neo4j.driver.Record record = result.next();
                    Node tmpNode = record.get(0).asNode();
                    if (tmpNode!=null){
                        return convertNode(tmpNode,nodeType);
                    }
                }
                return null;
            });
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        return myResult;
    }

    /**
     * @param argNode
     * @param nodeType
     * @return
     */
    private Object convertNode(Node argNode, NodeLabel nodeType) {
        // get node id
        long tmpId = argNode.id();
        // get nodeId
        String tmpNodeId = argNode.get("nodeId").asString();
        // get title
        String tmpTitle = argNode.get("title").asString();

        // other properties
        ChapterBean chapterBean;
        UnitBean unitBean;
        TopicBean topicBean;
        ResourceBean resourceBean;
        String tmpFocusArea, tmpSequence, tmpContext, tmpDescription, tmpType, tmpSourceURI;
        Long parentId;
        if (nodeType.getNodeType().equals(NodeLabel.CHAPTER)) {
            tmpFocusArea = argNode.get("focusArea").asString();
            tmpSequence = argNode.get("sequence").asString();

            chapterBean = new ChapterBean(tmpId,tmpNodeId, tmpTitle,tmpSequence, tmpFocusArea);
            return chapterBean;
        }

        if (nodeType.getNodeType().equals(NodeLabel.UNIT)) {
            tmpContext = argNode.get("context").asString();
            unitBean = new UnitBean(tmpId, tmpTitle, tmpContext);
            return unitBean;
        }

        if (nodeType.getNodeType().equals(NodeLabel.TOPIC) ||
                nodeType.getNodeType().equals(NodeLabel.SUBTOPIC)) {
            tmpDescription = argNode.get("description").asString();
            topicBean = new TopicBean(tmpId,tmpNodeId, tmpTitle, tmpDescription);
            return topicBean;
        }

        if (nodeType.getNodeType().equals(NodeLabel.RESOURCE)) {
            tmpType = argNode.get("type").asString();
            tmpDescription = argNode.get("description").asString();
            tmpSourceURI = argNode.get("sourceURI").asString();
            resourceBean = new ResourceBean(tmpId, tmpTitle, tmpType, tmpDescription, tmpSourceURI);
            return resourceBean;
        }

        return null;
    }
}
