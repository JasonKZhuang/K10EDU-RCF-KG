/**
 * Project: SpringBootNeo4j
 * Date: 23/10/2022
 * Author: Jason
 */
package com.zkz.microservice.neo4j.service;

import com.zkz.microservice.neo4j.configuration.AppProperties;
import com.zkz.microservice.neo4j.dto.ChapterBean;
import com.zkz.microservice.neo4j.dto.UnitBean;
import com.zkz.microservice.neo4j.entity.education.ChapterNode;
import com.zkz.microservice.neo4j.exception.ServiceException;
import com.zkz.microservice.neo4j.repository.ChapterRepository;
import com.zkz.microservice.neo4j.repository.OutcomeRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.neo4j.driver.exceptions.ClientException;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class ChapterService {
    private final AppProperties appProperties;
    private final ChapterRepository chapterRepository;
    private final OutcomeRepository outcomeRepository;
    private final Driver driver;
    private final NodeService nodeService;

    public ChapterNode createChapter(ChapterBean bean) {
        ChapterNode entity = new ChapterNode();
        try {
            BeanUtils.copyProperties(bean, entity);
            entity.setId(null);
            entity.setNodeId(UUID.randomUUID().toString());
            entity = chapterRepository.save(entity);
        } catch (Exception exp) {
            throw new RuntimeException(exp.getMessage());
        }
        return entity;
    }

    public ChapterBean createSimpleChapter(ChapterBean bean) {
        // CQL
        String statement = "MATCH  (s:Subject) \n" +
        "WHERE s.title = 'Science' AND s.yearLevel = 'Year 9' \n" +
        "CREATE (s)-[r1:HAS_CHAPTER]->(c:Chapter { \n" +
                    "nodeId: apoc.create.uuid(), \n" +
                    "title: $title, \n" +
                    "sequence: $sequence, \n" +
                    "focusArea: $focusArea \n" +
        "}), (c)-[r2:IS_CHAPTER_OF]->(s) \n" +
        "RETURN c" ;

        // Open a new session
        Session session = driver.session();
        try {
            // execute write transaction
            var tmpNode = session.writeTransaction(tx -> {
                var res = tx.run(statement,
                        Values.parameters(
                                "title", bean.getTitle(),
                                "sequence", bean.getSequence(),
                                "focusArea", bean.getFocusArea()
                        )
                );
                return res.single().get("c").asNode();
            });

            // return new UnitBean for this method
            session.close();
            return new ChapterBean(
                    tmpNode.id(),
                    tmpNode.get("nodeId").asString(),
                    tmpNode.get("title").asString(),
                    tmpNode.get("sequence").asString(),
                    tmpNode.get("focusArea").asString()
            );
        } catch (ClientException cExp) {
            throw new ServiceException(HttpStatus.CONFLICT, cExp.getMessage());
        } catch (Exception exp) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, exp.getMessage());
        }finally {
            session.close();
        }
    }

    public ChapterBean updateSimpleChapter(ChapterBean bean) {
        // CQL
        String statement =  " MATCH ( c:Chapter ) " +
                " WHERE id(c) = $chapterId " +
                " SET c.title = $title, c.sequence = $sequence, c.focusArea = $focusArea " +
                " RETURN c ";
        // Open a new session
        Session session = driver.session();
        try {
            // execute update transaction
            var tmpNode = session.writeTransaction(tx -> {
                var res = tx.run(statement,
                        Values.parameters("chapterId",bean.getId(),
                                "title", bean.getTitle(),
                                "sequence", bean.getSequence(),
                                "focusArea", bean.getFocusArea())
                );
                return res.single().get("c").asNode();
            });
            // return new UnitBean for this method
            session.close();
            return new ChapterBean(
                    tmpNode.id(),
                    tmpNode.get("nodeId").asString(),
                    tmpNode.get("title").asString(),
                    tmpNode.get("sequence").asString(),
                    tmpNode.get("focusArea").asString()
            );
        } catch (ClientException cExp) {
            session.close();
            throw new ServiceException(HttpStatus.CONFLICT, cExp.getMessage());
        } catch (Exception exp) {
            session.close();
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, exp.getMessage());
        }
    }

    public String deleteChapterById(String argId) {
        try {
            Long pkId = Long.parseLong(argId);
            chapterRepository.deleteById(pkId);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
        return "ok";
    }

    public String deleteChapterByTitle(String title) {
        try {
            chapterRepository.deleteByTitle(title);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
        return "ok";
    }

    public ChapterBean getChapterById(String id) {
        ChapterBean retBean = new ChapterBean();
        try{
            long pkId = Long.parseLong(id);
            Optional<ChapterNode> entity = chapterRepository.findById(pkId);
            if (entity.isPresent()) {
                BeanUtils.copyProperties(entity.get(), retBean);
            }
        }catch (Exception exp){
            throw new RuntimeException("id must be a digital number.");
        }
        return retBean;
    }

    public List<ChapterBean> getChapterByTitle(String title) {
        List<ChapterBean> retBeans = new ArrayList<>();
        List<ChapterNode> lst = chapterRepository.findByTitle(title);
        for (ChapterNode e : lst) {
            ChapterBean bean = new ChapterBean();
            BeanUtils.copyProperties(e, bean);
            retBeans.add(bean);
        }
        return retBeans;
    }

    public List<ChapterBean> getAllChapters() {
        List<ChapterNode> entities = chapterRepository.findAll();
        List<ChapterBean> beans = new ArrayList<>();
        for (ChapterNode e : entities) {
            ChapterBean bean = new ChapterBean();
            bean.setId(e.getId());
            bean.setNodeId(e.getNodeId());
            bean.setTitle(e.getTitle());
            bean.setFocusArea(e.getFocusArea());
            beans.add(bean);
        }
        return beans;
    }

    public List<ChapterBean> apiGetAllChapters() {
        String endpoint = appProperties.getOcrApisBaseUrl() + appProperties.getOcrApisGetChapters();
        WebClient.Builder webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        WebClient.ResponseSpec responseSpec = webClient.build().get().uri(endpoint).retrieve();

        List<ChapterBean> results = new ArrayList<>();
        List<Object> objs = responseSpec.bodyToMono(List.class).block();
        for (Object obj : objs) {
            ChapterBean chapterBean = new ChapterBean();
            LinkedHashMap<String, Object> myObj = (LinkedHashMap<String, Object>) obj;
            chapterBean.setId(Long.valueOf(myObj.get("id").toString()));
            chapterBean.setTitle(myObj.get("title").toString());
            chapterBean.setFocusArea(myObj.get("focusArea").toString());
            results.add(chapterBean);
        }

        return results;
    }
}
