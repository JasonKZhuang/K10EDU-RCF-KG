/**
 * Project: SpringBootNeo4j
 * Date: 22/11/2022
 * Author: Jason
 */
package com.zkz.microservice.neo4j.service;

import com.zkz.microservice.neo4j.configuration.AppProperties;
import com.zkz.microservice.neo4j.configuration.NodeLabel;
import com.zkz.microservice.neo4j.dto.ElementBean;
import com.zkz.microservice.neo4j.dto.TopicBean;
import com.zkz.microservice.neo4j.exception.ServiceException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.ClientException;
import org.neo4j.driver.types.Node;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class TopicService {
    private final AppProperties appProperties;
    private final Driver driver;
    private final NodeService nodeService;

    private final ElementService elementService;

    /**
     * @param bean    TopicBean
     * @param subFlag true or false
     * @return TopicBean
     */
    public TopicBean createSimpleTopic(TopicBean bean, boolean subFlag) {
        // CQL
        String nodeType = "";
        if (subFlag) {
            nodeType = "SubTopic ";
        } else {
            nodeType = "Topic ";
        }
        String statement = " CREATE (n:" + nodeType + "{ " +
                "nodeId: apoc.create.uuid()," +
                "title: $title, " +
                "description: $description " +
                "}) RETURN n";

        // Open a new session
        Session session = driver.session();
        try {
            // execute write transaction
            var tmpNode = session.writeTransaction(tx -> {
                var res = tx.run(statement,
                        Values.parameters("title", bean.getTitle(),
                                "description", bean.getDescription())
                );
                return res.single().get("n").asNode();
            });

            // return new UnitBean for this method
            session.close();
            return new TopicBean(
                    tmpNode.id(),
                    tmpNode.get("nodeId").asString(),
                    tmpNode.get("title").asString(),
                    tmpNode.get("description").asString()
            );
        } catch (ClientException cExp) {
            session.close();
            throw new ServiceException(HttpStatus.CONFLICT, cExp.getMessage());
        } catch (Exception exp) {
            session.close();
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, exp.getMessage());
        }
    }

    /**
     * @param bean
     * @param subFlag
     * @return
     */
    public TopicBean updateSimpleTopic(TopicBean bean, boolean subFlag) {
        // CQL
        String nodeType = "";
        if (subFlag) {
            nodeType = "SubTopic";
        } else {
            nodeType = "Topic";
        }
        String statement = " MATCH ( n:" + nodeType + " ) " +
                " WHERE id(n) = $topicId " +
                " SET n.title = $title, n.description = $description " +
                " RETURN n ";
        // Open a new session
        Session session = driver.session();
        try {
            // execute update transaction
            var tmpNode = session.writeTransaction(tx -> {
                var res = tx.run(statement,
                        Values.parameters("topicId", bean.getId(),
                                "title", bean.getTitle(),
                                "description", bean.getDescription())
                );
                return res.single().get("n").asNode();
            });
            // return new UnitBean for this method
            session.close();
            return new TopicBean(
                    tmpNode.id(),
                    tmpNode.get("nodeId").asString(),
                    tmpNode.get("title").asString(),
                    tmpNode.get("description").asString()
            );
        } catch (ClientException cExp) {
            session.close();
            throw new ServiceException(HttpStatus.CONFLICT, cExp.getMessage() + bean.toString());
        } catch (Exception exp) {
            session.close();
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, exp.getMessage() + bean.toString());
        }
    }

    /**
     * @param argTopicId
     * @param argUnitId
     */
    public void createRelationBtTopicAndUnitByNodeIds(Long argTopicId, Long argUnitId) {
        String statement = " MATCH (t:Topic), (u:Unit) " +
                " WHERE id(t) = $topicId AND id(u) = $unitId " +
                " CREATE (t)-[r:BELONGS_TO]->(u) " +
                " RETURN t,u ";

        Map<String, Object> params = new HashMap<>();
        params.put("topicId", argTopicId);
        params.put("unitId", argUnitId);

        try (Session session = driver.session()) {
            // Write transactions allow the driver to handle retries and transient errors
            Record record = session.writeTransaction(tx -> {
                Result result = tx.run(statement, params);
                return result.single();
            });
            //            System.out.println(String.format("Created friendship between: %s, %s",
            //                    record.get("t").get("title").asString(),
            //                    record.get("u").get("title").asString()));
            // You should capture any errors along with the query and data for traceability
        } catch (Exception ex) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * @param argSubTopicId
     * @param argParentTopicId
     */
    public void createRelationBtSubtopicAndTopicByNodeIds(Long argSubTopicId, Long argParentTopicId) {
        String statement = " MATCH (st:SubTopic), (pt:Topic) " +
                " WHERE id(st) = $subTopicId AND id(pt) = $parentTopicId " +
                " CREATE (st)-[r:BELONGS_TO]->(pt) " +
                " RETURN st,pt ";

        Map<String, Object> params = new HashMap<>();
        params.put("subTopicId", argSubTopicId);
        params.put("parentTopicId", argParentTopicId);

        try (Session session = driver.session()) {
            // Write transactions allow the driver to handle retries and transient errors
            Record record = session.writeTransaction(tx -> {
                Result result = tx.run(statement, params);
                return result.single();
            });
        } catch (Exception ex) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * @param argBean
     * @return TopicBean
     */
    public TopicBean createTopicWithUnitRelation(TopicBean argBean) {
        TopicBean newBean;
        if (argBean.getUnitId() == null || argBean.getUnitId() == 0) {
            throw new ServiceException(HttpStatus.NOT_ACCEPTABLE, "Unit Id is Required.");
        }

        try {
            newBean = createSimpleTopic(argBean, false);
            createRelationBtTopicAndUnitByNodeIds(newBean.getId(), argBean.getUnitId());
            newBean.setUnitId(argBean.getUnitId());
        } catch (NumberFormatException exp) {
            throw new ServiceException(HttpStatus.NOT_ACCEPTABLE, exp.getMessage());
        }
        return newBean;
    }

    /**
     * @param argBean
     * @return
     */
    public TopicBean createSubTopicWithParentTopicRelation(TopicBean argBean) {
        TopicBean newBean;
        if (argBean.getParentId() == null || argBean.getParentId() == 0) {
            throw new ServiceException(HttpStatus.NOT_ACCEPTABLE, "Parent Topic Id is Required.");
        }
        try {
            newBean = createSimpleTopic(argBean, true);
            createRelationBtSubtopicAndTopicByNodeIds(newBean.getId(), argBean.getParentId());
            newBean.setParentId(argBean.getParentId());
        } catch (NumberFormatException exp) {
            throw new ServiceException(HttpStatus.NOT_ACCEPTABLE, exp.getMessage());
        }
        return newBean;
    }

    /**
     * @param topicId
     * @param oldUnitId
     * @param newUnitId
     */
    public void updateRelationBtTopicAndUnitByNodeIds(Long topicId, Long oldUnitId, Long newUnitId) {
        deleteRelationBtTopicAndUnitByNodeIds(topicId, oldUnitId);
        createRelationBtTopicAndUnitByNodeIds(topicId, newUnitId);
    }

    /**
     * @param subTopicId
     * @param oldParentId
     * @param newParentId
     */
    public void updateRelationBtSubtopicAndParentTopicByNodeIds(Long subTopicId, Long oldParentId, Long newParentId) {
        deleteRelationBtSubtopicAndTopicByNodeIds(subTopicId, oldParentId);
        createRelationBtSubtopicAndTopicByNodeIds(subTopicId, newParentId);
    }

    public void deleteTopicAndRelationByNodeId(Long topicId, boolean subFlag) {
        String nodeType = "";
        if (subFlag) {
            nodeType = "SubTopic";
        } else {
            nodeType = "Topic";
        }
        String statement = "MATCH (t:" + nodeType + ") " +
                "WHERE ID(t)= $topicId " +
                "OPTIONAL MATCH (t)-[r]-() " +
                "DELETE r,t";
        Map<String, Object> params = new HashMap<>();
        params.put("topicId", topicId);
        delete(statement, params);
    }

    public void deleteNodeByTitle(String argTitle) {
        String statement = " MATCH (n {title: $myTitle}) DETACH DELETE n ";
        Map<String, Object> params = new HashMap<>();
        params.put("myTitle", argTitle);
        delete(statement, params);
    }

    public void deleteRelationBtTopicAndUnitByNodeIds(Long topicId, Long unitId) {
        String statement = " MATCH (t:Topic)-[r:BELONGS_TO]->(u:Unit) " +
                " WHERE ID(t) = $topicId AND ID(u)=$unitId " +
                " DELETE r ";
        Map<String, Object> params = new HashMap<>();
        params.put("topicId", topicId);
        params.put("unitId", unitId);
        delete(statement, params);
    }

    public void deleteRelationBtSubtopicAndTopicByNodeIds(Long subTopicId, Long parentTopicId) {
        String statement = " MATCH (st:SubTopic)-[r:BELONGS_TO]->(pt:Topic) " +
                " WHERE ID(st) = $subTopicId AND ID(pt)=$parentTopicId " +
                " DELETE r ";
        Map<String, Object> params = new HashMap<>();
        params.put("subTopicId", subTopicId);
        params.put("parentTopicId", parentTopicId);
        delete(statement, params);
    }

    public List<TopicBean> getAllTopicNodes(boolean subFlag) {
        List<TopicBean> myResult = new ArrayList<>();
        try {
            Session session = driver.session();
            myResult = session.readTransaction(tx -> {
                List<TopicBean> myBeans = new ArrayList<>();
                String nodeType = "";
                if (subFlag) {
                    nodeType = "SubTopic";
                } else {
                    nodeType = "Topic";
                }
                Result result = tx.run("MATCH (t:" + nodeType + ") RETURN t ORDER BY t.id ASC");
                while (result.hasNext()) {
                    org.neo4j.driver.Record record = result.next();
                    Node tmpNode = record.get(0).asNode();
                    // get node label
                    String tmpLabel = nodeService.getLabelOfNode(tmpNode);
                    // get node id
                    long tmpId = tmpNode.id();
                    // get nodeId properties
                    String nodeId = tmpNode.get("nodeId").asString();
                    // get title properties
                    String tmpTitle = tmpNode.get("title").asString();
                    // get description properties
                    String tmpContext = tmpNode.get("description").asString();
                    // create return single bean
                    TopicBean bean = new TopicBean(tmpId,nodeId, tmpTitle, tmpContext);
                    myBeans.add(bean);
                }
                return myBeans;
            });
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        return myResult;
    }

    public TopicBean getSingleTopicById(Long argId) {
        // CQL
        String statement = "MATCH (n:Topic) WHERE id(n) = $topicId RETURN n limit 1";
        Map<String, Object> params = new HashMap<>();
        params.put("topicId", argId);
        return getSingleTopic(statement, params);
    }

    public TopicBean getSingleSubTopicById(Long argId) {
        // CQL
        String statement = "MATCH (n:SubTopic) WHERE id(n) = $subTopicId RETURN n limit 1";
        Map<String, Object> params = new HashMap<>();
        params.put("subTopicId", argId);
        return getSingleSubTopic(statement, params);
    }

    public TopicBean getSingleTopicByTitle(String title) {
        // CQL
        String statement = "MATCH (n:Topic) WHERE n.title= $title RETURN n limit 1";
        Map<String, Object> params = new HashMap<>();
        params.put("title", title);
        return getSingleTopic(statement, params);
    }

    public TopicBean getSingleSubTopicByTitle(String title) {
        // CQL
        String statement = "MATCH (n:SubTopic) WHERE n.title= $title RETURN n limit 1";
        Map<String, Object> params = new HashMap<>();
        params.put("title", title);
        return getSingleSubTopic(statement, params);
    }

    private TopicBean getSingleTopic(String cqlStatement, Map<String, Object> params) {
        TopicBean retValue = new TopicBean();
        Session session = driver.session();
        try {
            Result result = session.run(cqlStatement, params);
            // Each Cypher execution returns a stream of records.
            Node tmpNode = result.single().get("n").asNode();
            if (tmpNode != null) {
                retValue.setId(tmpNode.id());
                retValue.setNodeId(tmpNode.get("nodeId").asString());
                retValue.setTitle(tmpNode.get("title").asString());
                retValue.setDescription(tmpNode.get("description").asString());
            }
        } catch (Exception ex) {
            retValue = null;
        } finally {
            session.close();
        }
        return retValue;
    }

    private TopicBean getSingleSubTopic(String cqlStatement, Map<String, Object> params) {
        TopicBean retValue = new TopicBean();
        Session session = driver.session();
        try {
            Result result = session.run(cqlStatement, params);
            // Each Cypher execution returns a stream of records.
            Node tmpNode = result.single().get("n").asNode();
            if (tmpNode != null) {
                retValue.setId(tmpNode.id());
                retValue.setNodeId(tmpNode.get("nodeId").asString());
                retValue.setTitle(tmpNode.get("title").asString());
                retValue.setDescription(tmpNode.get("description").asString());
            }
        } catch (Exception ex) {
            retValue = null;
        } finally {
            session.close();
        }
        return retValue;
    }

    private Result delete(String statement, Map<String, Object> params) {
        try (Session session = driver.session()) {
            Result myResult = session.writeTransaction(tx -> {
                Result result = tx.run(statement, params);
                return result;
            });
            log.info(myResult.consume().toString());
            return myResult;
        } catch (Exception ex) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    //==== call outer apis fetching data from mysql
    public List<TopicBean> apiGetTopicsByUnitId(Long unitId) {
        String endpoint = appProperties.getOcrApisBaseUrl() + appProperties.getOcrApisGetTopicsByUnitId() + unitId;
        WebClient.Builder webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        WebClient.ResponseSpec responseSpec = webClient.build().get().uri(endpoint).retrieve();

        List<TopicBean> results = new ArrayList<>();
        List<Object> objs = responseSpec.bodyToMono(List.class).block();
        for (Object obj : objs) {
            TopicBean bean = new TopicBean();
            LinkedHashMap<String, Object> myObj = (LinkedHashMap<String, Object>) obj;
            bean.setId(Long.valueOf(myObj.get("id").toString()));
            bean.setTitle(myObj.get("title").toString());
            bean.setDescription(myObj.get("description").toString());
            bean.setUnitId(unitId);
            results.add(bean);
        }

        return results;
    }

    public List<TopicBean> apiGetAllTopics() {
        String endpoint = appProperties.getOcrApisBaseUrl()
                + appProperties.getOcrApisGetTopics();
        WebClient.Builder webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        WebClient.ResponseSpec responseSpec = webClient.build().get().uri(endpoint).retrieve();

        List<TopicBean> results = new ArrayList<>();
        List<Object> objs = responseSpec.bodyToMono(List.class).block();
        for (Object obj : objs) {
            TopicBean bean = new TopicBean();
            LinkedHashMap<String, Object> myObj = (LinkedHashMap<String, Object>) obj;
            bean.setId(Long.valueOf(myObj.get("id").toString()));
            bean.setTitle(myObj.get("title").toString());
            bean.setDescription(myObj.get("description").toString());
            bean.setUnitId(Long.valueOf(myObj.get("unitId").toString()));
            results.add(bean);
        }

        return results;
    }

    public List<TopicBean> apiGetSubTopicsByTopicId(Long topicId) {
        String endpoint = appProperties.getOcrApisBaseUrl() + appProperties.getOcrApisGetSubTopicsByTopicId() + topicId;
        WebClient.Builder webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        WebClient.ResponseSpec responseSpec = webClient.build().get().uri(endpoint).retrieve();

        List<TopicBean> results = new ArrayList<>();
        List<Object> objs = responseSpec.bodyToMono(List.class).block();
        for (Object obj : objs) {
            TopicBean bean = new TopicBean();
            LinkedHashMap<String, Object> myObj = (LinkedHashMap<String, Object>) obj;
            bean.setId(Long.valueOf(myObj.get("id").toString()));
            bean.setTitle(myObj.get("title").toString());
            bean.setDescription(myObj.get("description").toString());
            bean.setParentId(topicId);
            results.add(bean);
        }

        return results;
    }

    //====================================================
    public Result deleteEntitiesBySubTopicId(long argId){
        String statement = "MATCH (n:SubTopic )-[r]->(e:Element) \n" +
                "WHERE id(n) = $topicId \n" +
                "DETACH DELETE r,e \n" +
                "RETURN n ";

        Map<String, Object> params = new HashMap<>();
        params.put("topicId", argId);
        Result result = delete(statement,params);
        return result;
    }

    public List<ElementBean> extractElementsFromProperty(long argId, String argPropertyName){
        Result deleteResult =  deleteEntitiesBySubTopicId(argId);

        // CQL
        String statement = "MATCH (n:SubTopic ) WHERE ID(n) = $topicId \n";
        statement +=
        "CALL apoc.nlp.gcp.entities.stream(n, { \n" +
        "       key: $gcpApiKey, \n" +
        "       nodeProperty: $propertyName \n" +
        "}) \n" +
        "YIELD value \n" +
        "UNWIND value.entities AS entity \n"+
        "MERGE (e:ElementNode { name:entity.name }) \n" +
        "SET e.type = entity.type \n" +
        "MERGE (n)-[:HAS_ELEMENT]->(e) \n" +
        "RETURN e ";

        Map<String, Object> params = new HashMap<>();
        params.put("topicId", argId);
        params.put("gcpApiKey", appProperties.getGcpKey());
        params.put("propertyName", argPropertyName);

        // Open a new session
        List<ElementBean> retList = new ArrayList<>();
        Session session = driver.session();
        try {
            Result result = session.run(statement, params);
            // Each Cypher execution returns a stream of records.
            while (result.hasNext()){
                Node tmpNode = result.next().get("e").asNode();
                if (tmpNode != null) {
                    ElementBean eb = new ElementBean();
                    eb.setId(tmpNode.id());
                    eb.setName(tmpNode.get("name").asString());
                    eb.setType(tmpNode.get("type").asString());
                    retList.add(eb);
                }
            }
        } catch (Exception ex) {
            retList = null;
        } finally {
            session.close();
        }
        return retList;
    }

    /// ====
    public List<ElementBean> reGenerateElementsTopic() {
        List<ElementBean> retValues = new ArrayList<>();
        List<TopicBean> beans = getAllTopicNodes(false);
        for (TopicBean tempBean : beans) {
            List<ElementBean> tempBeans = elementService.extractElementsFromProperty(
                    NodeLabel.TOPIC.getNodeType(),
                    tempBean.getId(),
                    "description");
            if (tempBeans !=null) {
                retValues.addAll(tempBeans);
            }
        }
        return retValues;
    }

    public List<ElementBean> reGenerateElementsSubTopic() {
        List<ElementBean> retValues = new ArrayList<>();
        List<TopicBean> beans = getAllTopicNodes(true);
        for (TopicBean tempBean : beans) {
            List<ElementBean> tempBeans = elementService.extractElementsFromProperty(
                    NodeLabel.SUBTOPIC.getNodeType(),
                    tempBean.getId(),
                    "description");
            if (tempBeans!=null) {
                retValues.addAll(tempBeans);
            }
        }
        return retValues;
    }

}
