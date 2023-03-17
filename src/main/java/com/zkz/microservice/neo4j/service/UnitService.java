/**
 * Project: SpringBootNeo4j
 * Date: 1/11/2022
 * Author: Jason
 */
package com.zkz.microservice.neo4j.service;

import com.zkz.microservice.neo4j.configuration.AppProperties;
import com.zkz.microservice.neo4j.configuration.NodeLabel;
import com.zkz.microservice.neo4j.dto.ElementBean;
import com.zkz.microservice.neo4j.dto.UnitBean;
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
public class UnitService {
    private final AppProperties appProperties;
    private final Driver driver;
    private final ElementService elementService;

    private static Object convert(Value value) {
        switch (value.type().name()) {
            case "PATH":
                return value.asList(UnitService::convert);
            case "NODE":
            case "RELATIONSHIP":
                return value.asMap();
        }
        return value.asObject();
    }

    public UnitBean getSingleUnitByTitle(String title) {
        // CQL
        String statement = "MATCH (n:Unit) WHERE n.title= $title RETURN n limit 1";
        Map<String, Object> params = new HashMap<>();
        params.put("title", title);
        return getSingleUnit(statement, params);
    }

    public UnitBean getSingleUnitByNodeId(Long nodeId) {
        // CQL
        String statement = "MATCH (n:Unit) WHERE id(n) = $nodeId RETURN n";
        Map<String, Object> params = new HashMap<>();
        params.put("nodeId", nodeId);
        return getSingleUnit(statement, params);
    }

    public List<UnitBean> getAllUnitNodes() {
        List<UnitBean> myResult = new ArrayList<>();
        try {
            Session session = driver.session();
            myResult = session.readTransaction(tx -> {
                List<UnitBean> unitBeans = new ArrayList<>();
                Result result = tx.run("MATCH (u:Unit) RETURN u ORDER BY u.id ASC");
                while (result.hasNext()) {
                    org.neo4j.driver.Record record = result.next();
                    Node tmpNode = record.get(0).asNode();
                    // get node label
                    // String tmpLabel = nodeService.getLabelOfNode(tmpNode);
                    // get node id
                    long tmpId = tmpNode.id();
                    // get title properties
                    String tmpTitle = tmpNode.get("title").asString();
                    // get context properties
                    String tmpContext = tmpNode.get("context").asString();
                    // create return single bean
                    UnitBean bean = new UnitBean(tmpId, tmpTitle, tmpContext);
                    unitBeans.add(bean);
                }
                return unitBeans;
            });
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        return myResult;
    }

    public UnitBean createSimpleUnit(UnitBean bean) {
        // CQL
        String statement = " CREATE (u:Unit { " +
                "title: $title, " +
                "context: $context " +
                "}) RETURN u";
        // Open a new session
        Session session = driver.session();
        try {
            // execute write transaction
            var tmpNode = session.writeTransaction(tx -> {
                var res = tx.run(statement,
                        Values.parameters("title", bean.getTitle(), "context", bean.getContext())
                );
                return res.single().get("u").asNode();
            });

            // return new UnitBean for this method
            session.close();
            return new UnitBean(
                    tmpNode.id(),
                    tmpNode.get("title").asString(),
                    tmpNode.get("context").asString()
            );
        } catch (ClientException cExp) {
            session.close();
            throw new ServiceException(HttpStatus.CONFLICT, cExp.getMessage());
        } catch (Exception exp) {
            session.close();
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, exp.getMessage());
        }
    }

    public UnitBean updateSimpleUnit(UnitBean bean) {
        // CQL
        String statement = " MATCH ( u:Unit ) " +
                " WHERE id(u) = $unitId " +
                " SET u.title = $title, u.context = $context " +
                " RETURN u ";
        // Open a new session
        Session session = driver.session();
        try {
            // execute update transaction
            var tmpNode = session.writeTransaction(tx -> {
                var res = tx.run(statement,
                        Values.parameters("unitId", bean.getId(),
                                "title", bean.getTitle(),
                                "context", bean.getContext())
                );
                return res.single().get("u").asNode();
            });
            // return new UnitBean for this method
            session.close();
            return new UnitBean(
                    tmpNode.id(),
                    tmpNode.get("title").asString(),
                    tmpNode.get("context").asString()
            );
        } catch (ClientException cExp) {
            session.close();
            throw new ServiceException(HttpStatus.CONFLICT, cExp.getMessage());
        } catch (Exception exp) {
            session.close();
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, exp.getMessage());
        }
    }

    public UnitBean createUnitWithChapter(UnitBean argBean) {
        UnitBean newUnitBean;
        if (argBean.getChapterId() == null || argBean.getChapterId() == 0) {
            throw new ServiceException(HttpStatus.NOT_ACCEPTABLE, "Chapter Id is Required.");
        }

        try {
            newUnitBean = createSimpleUnit(argBean);
            createRelationshipByNodeId(newUnitBean.getId(), argBean.getChapterId());
            newUnitBean.setChapterId(argBean.getChapterId());
        } catch (NumberFormatException exp) {
            throw new ServiceException(HttpStatus.NOT_ACCEPTABLE, exp.getMessage());
        }
        return newUnitBean;
    }

    public void createRelationshipByNodeId(Long unitId, Long chapterId) {
        String statement = " MATCH (u:Unit), (c:Chapter) " +
                " WHERE id(u) = $unitId AND id(c) = $chapterId " +
                " CREATE (u)-[r:BELONGS_TO]->(c) " +
                " RETURN u,c ";

        Map<String, Object> params = new HashMap<>();
        params.put("unitId", unitId);
        params.put("chapterId", chapterId);

        try (Session session = driver.session()) {
            // Write transactions allow the driver to handle retries and transient errors
            Record record = session.writeTransaction(tx -> {
                Result result = tx.run(statement, params);
                return result.single();
            });
            System.out.println(String.format("Created friendship between: %s, %s",
                    record.get("c").get("title").asString(),
                    record.get("u").get("title").asString()));
            // You should capture any errors along with the query and data for traceability
        } catch (Exception ex) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    public void updateRelationByNodeId(Long unitId, Long oldChapterId, Long newChapterId) {
        deleteRelationshipByNodeId(unitId, oldChapterId);
        createRelationshipByNodeId(unitId, newChapterId);
    }

    public void deleteUnitWithRelationById(Long unitId) {
        String statement = "MATCH (u:Unit) " +
                "WHERE ID(u)= $unitId " +
                "OPTIONAL MATCH (u)-[r]-() " +
                "DELETE r,u";
        Map<String, Object> params = new HashMap<>();
        params.put("unitId", unitId);
        delete(statement, params);
    }

    public void deleteUnitWithRelationByTitle(String unitTitle) {
        String statement = " MATCH (n {title: $unitTitle}) DETACH DELETE n ";
        Map<String, Object> params = new HashMap<>();
        params.put("unitTitle", unitTitle);
        delete(statement, params);
    }

    public void deleteRelationshipByNodeId(Long unitId, Long chapterId) {
        String statement = " MATCH (u:Unit)-[r:BELONGS_TO]->(c:Chapter) " +
                " WHERE ID(u) = $unitId AND ID(c)=$chapterId " +
                " DELETE r ";
        Map<String, Object> params = new HashMap<>();
        params.put("unitId", unitId);
        params.put("chapterId", chapterId);
        delete(statement, params);
    }

    private List<Map<String, Object>> query(String query, Map<String, Object> params) {
        try (Session session = driver.session()) {
            return session.readTransaction(
                    tx -> tx.run(query, params).list(r -> r.asMap(UnitService::convert))
            );
        }
    }

    private void delete(String statement, Map<String, Object> params) {
        try (Session session = driver.session()) {
            Result myResult = session.writeTransaction(tx -> {
                Result result = tx.run(statement, params);
                session.close();
                return result;
            });
            // log.info(myResult.consume().toString());
        } catch (Exception ex) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    // A Managed transaction is a quick and easy way to wrap a Cypher Query.
    // The `session.run` method will run the specified Query.
    // This simpler method does not use any automatic retry mechanism.
    // "MATCH (a:Person) WHERE a.name STARTS WITH $x RETURN a.name AS name"
    private UnitBean getSingleUnit(String cqlStatement, Map<String, Object> params) {
        UnitBean retValue = new UnitBean();
        Session session = driver.session();
        try {
            Result result = session.run(cqlStatement, params);
            // Each Cypher execution returns a stream of records.
            Node tmpNode = result.single().get("n").asNode();
            if (tmpNode != null) {
                retValue.setId(tmpNode.id());
                retValue.setNodeId(tmpNode.get("nodeId").asString());
                retValue.setTitle(tmpNode.get("title").asString());
                retValue.setContext(tmpNode.get("context").asString());
            }
        } catch (Exception ex) {
            retValue = null;
        } finally {
            session.close();
        }
        return retValue;
    }

    /// ====
    public List<ElementBean> reGenerateElements() {
        List<ElementBean> retValues = new ArrayList<>();
        List<UnitBean> beans = getAllUnitNodes();
        for (UnitBean tempBean : beans) {
            List<ElementBean> tempBeans = elementService.extractElementsFromProperty(
                    NodeLabel.UNIT.getNodeType(),
                    tempBean.getId(),
                    "context");
            if (tempBeans !=null) {
                retValues.addAll(tempBeans);
            }
        }
        return retValues;
    }

    /// ===

    public List<UnitBean> apiGetAllUnits() {
        String endpoint = appProperties.getOcrApisBaseUrl()
                        + appProperties.getOcrApisGetUnits();
        return apiGetUnitsByEndpoint(endpoint);
    }

    public List<UnitBean> apiGetUnitsByChapterId(Long chapterId) {
        String endpoint = appProperties.getOcrApisBaseUrl()
                + appProperties.getOcrApisGetUnitsByChapterId()
                + chapterId;
        return apiGetUnitsByEndpoint(endpoint);
    }

    private List<UnitBean> apiGetUnitsByEndpoint(String endpoint) {
        WebClient.Builder webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        WebClient.ResponseSpec responseSpec = webClient.build().get().uri(endpoint).retrieve();

        List<UnitBean> results = new ArrayList<>();
        List<Object> objs = responseSpec.bodyToMono(List.class).block();
        for (Object obj : objs) {
            UnitBean bean = new UnitBean();
            LinkedHashMap<String, Object> myObj = (LinkedHashMap<String, Object>) obj;
            bean.setId(Long.valueOf(myObj.get("id").toString()));
            bean.setTitle(myObj.get("title").toString());
            bean.setContext(myObj.get("context").toString());
            bean.setChapterId(Long.valueOf(myObj.get("chapterId").toString()));
            results.add(bean);
        }

        return results;
    }
}
