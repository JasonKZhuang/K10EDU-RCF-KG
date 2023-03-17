/**
 * Project: SpringBootNeo4j
 * Date: 25/10/2022
 * Author: Jason
 */
package com.zkz.microservice.neo4j.service;

import com.zkz.microservice.neo4j.configuration.AppProperties;
import com.zkz.microservice.neo4j.configuration.NodeLabel;
import com.zkz.microservice.neo4j.dto.ElementBean;
import com.zkz.microservice.neo4j.dto.OutcomeBean;
import com.zkz.microservice.neo4j.entity.education.ChapterNode;
import com.zkz.microservice.neo4j.entity.education.OutcomeNode;
import com.zkz.microservice.neo4j.exception.ServiceException;
import com.zkz.microservice.neo4j.repository.ChapterRepository;
import com.zkz.microservice.neo4j.repository.OutcomeRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.neo4j.driver.exceptions.ClientException;
import org.neo4j.driver.types.Node;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class OutcomeService {
    private final AppProperties appProperties;
    private final ChapterRepository chapterRepository;
    private final OutcomeRepository outcomeRepository;
    private final Driver driver;
//    private final NodeService nodeService;
    private final ElementService elementService;

    public OutcomeNode createOutcome(OutcomeBean bean) {
        OutcomeNode entity = new OutcomeNode();
        BeanUtils.copyProperties(bean, entity);
        entity.setId(null);
        return outcomeRepository.save(entity);
    }

    public OutcomeBean createSimpleOutcome(OutcomeBean bean) {
        // CQL
        String statement = " CREATE (n:Outcome { " +
                "nodeId: apoc.create.uuid()," +
                "title: $title, " +
                "level: $level, " +
                "description: $description " +
                "}) RETURN n";
        // Open a new session
        Session session = driver.session();
        try {
            // execute write transaction
            var tmpNode = session.writeTransaction(tx -> {
                var res = tx.run(statement,
                        Values.parameters(
                                "title", bean.getTitle(),
                                "level", bean.getLevel(),
                                "description", bean.getDescription()
                        )
                );
                return res.single().get("n").asNode();
            });
            session.close();

            return new OutcomeBean(
                    tmpNode.id(),
                    tmpNode.get("nodeId").asString(),
                    tmpNode.get("title").asString(),
                    tmpNode.get("level").asString(),
                    tmpNode.get("description").asString()
            );
        } catch (ClientException cExp) {
            session.close();
            throw new ServiceException(HttpStatus.CONFLICT, bean.toString() + cExp.getMessage());
        } catch (Exception exp) {
            session.close();
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, bean.toString() + exp.getMessage());
        }
    }

    public OutcomeNode createOutcome(String chapterTitle, OutcomeBean bean) {
        List<ChapterNode> cEntities = chapterRepository.findByTitle(chapterTitle);
        if (cEntities == null || cEntities.size() == 0) {
            throw ServiceException.notFound("Chapter cannot be found.");
        }
        OutcomeNode oEntity = new OutcomeNode();
        oEntity.setNodeId(UUID.randomUUID().toString());
        oEntity.setTitle(bean.getTitle());
        oEntity.setDescription(bean.getDescription());
        oEntity.setLevel(bean.getLevel());
        oEntity.setChapter(cEntities.get(0));
        try {
            oEntity = outcomeRepository.save(oEntity);
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        return oEntity;
    }

    public OutcomeNode createOutcome(long chapterId, OutcomeBean bean) {
        Optional<ChapterNode> optionalChapter = chapterRepository.findById(chapterId);
        if (optionalChapter.isPresent() == false) {
            throw ServiceException.notFound("Chapter cannot be found.");
        }
        ChapterNode cEntity = optionalChapter.get();
        OutcomeNode oEntity = new OutcomeNode();
        oEntity.setNodeId(UUID.randomUUID().toString());
        oEntity.setTitle(bean.getTitle());
        oEntity.setDescription(bean.getDescription());
        oEntity.setLevel(bean.getLevel());
        oEntity.setChapter(cEntity);
        try {
            oEntity = outcomeRepository.save(oEntity);
        } catch (Exception exp) {
            throw ServiceException.notFound("Outcome cannot be saved.");
        }
        return oEntity;
    }

    public OutcomeBean updateSimpleOutcome(OutcomeBean bean) {
        if (bean.getId() == null){
            return null;
        }
        // CQL
        String statement = " MATCH ( n:Outcome ) " +
                " WHERE id(n) = $nodeId " +
                " SET " +
                " n.title = $title, " +
                " n.level = $level, " +
                " n.description = $description " +
                " RETURN n ";
        // Open a new session
        Session session = driver.session();
        try {
            // execute update transaction
            var tmpNode = session.writeTransaction(tx -> {
                var res = tx.run(statement,
                        Values.parameters("nodeId", bean.getId(),
                                "title", bean.getTitle(),
                                "level", bean.getLevel(),
                                "description", bean.getDescription()
                        )
                );
                if (res == null) return null;
                return res.single().get("n").asNode();
            });
            // return new UnitBean for this method
            session.close();
            return new OutcomeBean(
                    tmpNode.id(),
                    tmpNode.get("nodeId").asString(),
                    tmpNode.get("title").asString(),
                    tmpNode.get("level").asString(),
                    tmpNode.get("description").asString()
            );
        } catch (ClientException cExp) {
            session.close();
            throw new ServiceException(HttpStatus.CONFLICT, bean.toString() + cExp.getMessage());
        } catch (Exception exp) {
            session.close();
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, bean.toString() + exp.getMessage());
        }
    }

    public String deleteOutcomeByTitle(String title) {
        try {
            outcomeRepository.deleteByTitle(title);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
        return "ok";
    }

    public String deleteOutcomeById(String argId) {
        try {
            long id = Long.parseLong(argId);
            outcomeRepository.deleteById(id);
        } catch (Exception ex) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
        return "ok";
    }

    public void deleteOutcomesAndRelationsById(Long outcomeId) {
        String statement = "MATCH (n:Outcome) " +
                "WHERE ID(n)= $outcomeId " +
                "OPTIONAL MATCH (n)-[r]-() " +
                "DELETE r,n";
        Map<String, Object> params = new HashMap<>();
        params.put("outcomeId", outcomeId);
        delete(statement, params);
    }

    public void deleteOutcomesAndRelationsByChapterId(Long chapterId) {
        List<OutcomeBean> outcomeBeans = getOutcomesByChapterId(chapterId);
        if (outcomeBeans != null && outcomeBeans.size() > 0) {
            for (OutcomeBean ob : outcomeBeans) {
                deleteOutcomesAndRelationsById(ob.getId());
            }
        }
    }

    public OutcomeBean getOutcomeById(String argId) {
        OutcomeBean retValue = null;
        try {
            long id = Long.parseLong(argId);
            Optional<OutcomeNode> oEntity = outcomeRepository.findById(id);
            if (oEntity.isPresent()) {
                BeanUtils.copyProperties(oEntity, retValue);
            }
        } catch (Exception ex) {
            throw new ServiceException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
        return retValue;
    }

    public OutcomeBean getOutcomesByTitle(String argTitle) {
        OutcomeBean retValues = null;
        try {
            List<OutcomeNode> entities = outcomeRepository.findByTitle(argTitle);
            if (entities != null && entities.size() > 0) {
                retValues = new OutcomeBean();
                retValues.setId(entities.get(0).getId());
                retValues.setTitle(entities.get(0).getTitle());
                retValues.setLevel(entities.get(0).getLevel());
                retValues.setDescription(entities.get(0).getDescription());
            }
        } catch (Exception ex) {
            throw new ServiceException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
        return retValues;
    }

    public List<OutcomeBean> getOutcomesByChapterId(Long chapterId) {

        // CQL
        String statement = " MATCH (c:Chapter) - [r] -> (o:Outcome) " +
                " WHERE ID(c)= $chapterId " +
                " RETURN o ";
        // Open a new session
        Session session = driver.session();
        try {
            List<OutcomeBean> result = session.run(statement, Values.parameters("chapterId", chapterId))
                    .stream()
                    .map(r -> {
                        Node tmpNode = r.get("o").asNode();
                        return new OutcomeBean(
                                tmpNode.id(),
                                tmpNode.get("nodeId").asString(),
                                tmpNode.get("title").asString(),
                                tmpNode.get("level").asString(),
                                tmpNode.get("description").asString()
                        );
                    }).collect(Collectors.toList());
            return result;
        } catch (ClientException cExp) {
            session.close();
            throw new ServiceException(HttpStatus.CONFLICT, cExp.getMessage());
        } catch (Exception exp) {
            session.close();
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, exp.getMessage());
        }
    }

    public List<OutcomeBean> getAllOutcomeNodes() {
        // CQL
        String statement = " MATCH (o:Outcome) RETURN o " ;

        // Open a new session
        Session session = driver.session();
        try {
            List<OutcomeBean> result = session.run(statement)
                    .stream()
                    .map(r -> {
                        Node tmpNode = r.get("o").asNode();
                        return new OutcomeBean(
                                tmpNode.id(),
                                tmpNode.get("nodeId").asString(),
                                tmpNode.get("title").asString(),
                                tmpNode.get("level").asString(),
                                tmpNode.get("description").asString()
                        );
                    }).collect(Collectors.toList());
            return result;
        } catch (ClientException cExp) {
            session.close();
            throw new ServiceException(HttpStatus.CONFLICT, cExp.getMessage());
        } catch (Exception exp) {
            session.close();
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, exp.getMessage());
        }
    }

    private void delete(String statement, Map<String, Object> params) {
        try (Session session = driver.session()) {
            Result myResult = session.writeTransaction(tx -> {
                Result result = tx.run(statement, params);
                return result;
            });
            //            log.info(myResult.consume().toString());
        } catch (Exception ex) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    public List<ElementBean> reGenerateElements() {
        List<ElementBean> retValues = new ArrayList<>();
        List<OutcomeBean> outcomeBeans = getAllOutcomeNodes();
        for (OutcomeBean ob : outcomeBeans) {
            List<ElementBean> tempBeans = elementService.extractElementsFromProperty(
                    NodeLabel.OUTCOME.getNodeType(),
                    ob.getId(),
                    "description");
            if (tempBeans!=null) {
                retValues.addAll(tempBeans);
            }
        }
        return retValues;
    }
    // ====
    public List<OutcomeBean> apiGetOutcomesByChapterId(Long chapterId) {
        String endpoint = appProperties.getOcrApisBaseUrl() + appProperties.getOcrApisGetOutcomesByChapterId() + chapterId;
        return apiGetOutcomesByChapter(endpoint);
    }

    public List<OutcomeBean> apiGetOutcomesByChapterTitle(String chapterTitle) {
        String endpoint = appProperties.getOcrApisBaseUrl() + appProperties.getOcrApisGetOutcomesByChapterTitle() + chapterTitle;
        return apiGetOutcomesByChapter(endpoint);
    }

    private List<OutcomeBean> apiGetOutcomesByChapter(String endpoint) {
        WebClient.Builder webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        WebClient.ResponseSpec responseSpec = webClient.build().get().uri(endpoint).retrieve();
        List<Object> objs = responseSpec.bodyToMono(List.class).block();
        List<OutcomeBean> retList = new ArrayList<>();
        for (Object obj : objs) {
            OutcomeBean bean = new OutcomeBean();
            LinkedHashMap<String, Object> myObj = (LinkedHashMap<String, Object>) obj;
            bean.setId(Long.valueOf(myObj.get("id").toString()));
            bean.setTitle(myObj.get("title").toString());
            bean.setLevel(myObj.get("level").toString());
            bean.setDescription(myObj.get("description").toString());
            retList.add(bean);
        }
        return retList;
    }

}
