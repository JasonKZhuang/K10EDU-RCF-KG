/**
 * Project: EducationMicroservice
 * Date: 9/3/2023
 * Author: Jason
 */
package com.zkz.microservice.neo4j.service;

import com.zkz.microservice.neo4j.configuration.AppProperties;
import com.zkz.microservice.neo4j.dto.DictionaryBean;
import com.zkz.microservice.neo4j.dto.ElementBean;
import com.zkz.microservice.neo4j.exception.ServiceException;
import com.zkz.microservice.neo4j.repository.ElementRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class ElementService {
    private final AppProperties appProperties;
    private final Driver driver;
    private final ElementRepository elementRepository;

    public void deleteElementsById(Long id){
        elementRepository.deleteById(id);
    }

    public Result deleteElementsByParentId(String argParentLabel, long argId) {
        String statement = "MATCH (n:" + argParentLabel + ")-[r]->(e:Element) \n" +
                "WHERE id(n) = $paramId \n" +
                "DETACH DELETE r,e \n" +
                "RETURN n ";

        Map<String, Object> params = new HashMap<>();
        params.put("paramId", argId);

        try (Session session = driver.session()) {
            Result myResult = session.writeTransaction(tx -> {
                Result result = tx.run(statement, params);
                return result;
            });
        } catch (Exception ex) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
        return null;
    }

    public List<ElementBean> extractElementsFromProperty(String argLabelName,
                                                         long argId,
                                                         String argPropertyName) {
        deleteElementsByParentId(argLabelName, argId);

        // CQL
        String statement = "MATCH (n:" + argLabelName + ") WHERE ID(n) = $paramId \n";
        statement +=
                "CALL apoc.nlp.gcp.entities.stream(n, { \n" +
                        "       key: $gcpApiKey, \n" +
                        "       nodeProperty: $propertyName \n" +
                        "}) \n" +
                        "YIELD value \n" +
                        "UNWIND value.entities AS entity \n" +
                        "MERGE (e:Element { nodeId: apoc.create.uuid(), name:entity.name }) \n" +
                        "SET e.type = entity.type \n" +
                        "MERGE (n)-[:HAS_ELEMENT]->(e) \n" +
                        "RETURN e ";

        Map<String, Object> params = new HashMap<>();
        params.put("paramId", argId);
        params.put("gcpApiKey", appProperties.getGcpKey());
        params.put("propertyName", argPropertyName);

        // Open a new session
        List<ElementBean> retList = new ArrayList<>();
        Session session = driver.session();
        try {
            Result result = session.run(statement, params);
            // Each Cypher execution returns a stream of records.
            while (result.hasNext()) {
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
            ex.printStackTrace();
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        } finally {
            session.close();
        }
        return retList;
    }

    public List<ElementBean> getAllElementNodes() {
        List<ElementBean> myResult = new ArrayList<>();
        try {
            Session session = driver.session();
            myResult = session.readTransaction(tx -> {
                List<ElementBean> myBeans = new ArrayList<>();
                String nodeType = "Element";
                Result result = tx.run("MATCH (t:" + nodeType + ") RETURN t ORDER BY t.id ASC");
                while (result.hasNext()) {
                    org.neo4j.driver.Record record = result.next();
                    Node tmpNode = record.get(0).asNode();
                    // get node id
                    long tmpId = tmpNode.id();
                    // get nodeId properties
                    String nodeId = tmpNode.get("nodeId").asString();
                    // get title properties
                    String tmpName = tmpNode.get("name").asString();
                    // get description properties
                    String tmpType = tmpNode.get("type").asString();
                    // create return single bean
                    ElementBean bean = new ElementBean(tmpId, nodeId, tmpName, tmpType);
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

    public int filterByDictionary() {
        int removedNumbers = 0;
        List<ElementBean> elementBeans = getAllElementNodes();
        for (ElementBean eb : elementBeans) {
            String nodeId = eb.getNodeId();
            DictionaryBean db = apiGetDictionaryByWord(eb.getName(), "chemistry");
            if (db == null) {
                String statement = "MATCH (n)-[r]->(e:Element) WHERE e.nodeId = '"+nodeId+"' DELETE r,e;" ;
                System.out.println(statement);
                Map<String, Object> params = new HashMap<>();
                params.put("paramId", nodeId);
                delete(statement,params);
//                elementRepository.deleteById(eb.getId());
                removedNumbers++;
            }
        }
        return removedNumbers;
    }

    private void delete(String statement, Map<String, Object> params) {
        try (Session session = driver.session()) {
            Result myResult = session.writeTransaction(tx -> {
                Result result = tx.run(statement, params);
                session.close();
                return result;
            });
        } catch (Exception ex) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    public DictionaryBean apiGetDictionaryByWord(String argWord, String argSubject) {
        String endpoint = appProperties.getOcrApisBaseUrl() + appProperties.getOcrApisGetDictionaryByWord() + "/" + argSubject + "/" + argWord;
        WebClient.Builder webClient = WebClient.builder().defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE).defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        WebClient.ResponseSpec responseSpec = webClient.build().get().uri(endpoint).retrieve();

        try {
            DictionaryBean bean = responseSpec.bodyToMono(DictionaryBean.class).block();
            if (bean == null) {
                return null;
            }
            return bean;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new DictionaryBean();
    }

}
