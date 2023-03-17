/**
 * Project: SpringBootNeo4j
 * Date: 22/11/2022
 * Author: Jason
 */
package com.zkz.microservice.neo4j.service;

import com.zkz.microservice.neo4j.configuration.NodeLabel;
import com.zkz.microservice.neo4j.dto.ResourceBean;
import com.zkz.microservice.neo4j.exception.ServiceException;
import com.zkz.microservice.neo4j.repository.DriverSessionHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Record;
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.ClientException;
import org.neo4j.driver.types.Node;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class ResourceService {
    private final Driver driver;
    private final DriverSessionHelper sessionHelper;
    private final NodeService nodeService;

    /**
     * @param bean
     * @return
     */
    public ResourceBean createSimpleResource(ResourceBean bean) {
        String statement = " CREATE (n:Resource { " +
                "title: $title, " +
                "resourceType: $resourceType, " +
                "description: $description, " +
                "sourceURI: $sourceURI " +
                "}) RETURN n";
        // Open a new session
        Session session = driver.session();
        try {
            // execute write transaction
            var tmpNode = session.writeTransaction(tx -> {
                var res = tx.run(statement,
                        Values.parameters("title", bean.getTitle(),
                                "resourceType", bean.getResourceType(),
                                "description", bean.getDescription(),
                                "sourceURI", bean.getSourceURI())
                );
                return res.single().get("n").asNode();
            });

            // return new bean for this method
            session.close();
            return new ResourceBean(
                    tmpNode.id(),
                    tmpNode.get("title").asString(),
                    tmpNode.get("resourceType").asString(),
                    tmpNode.get("description").asString(),
                    tmpNode.get("sourceURI").asString()
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
     * @return
     */
    public ResourceBean updateSimpleResource(ResourceBean bean) {
        String statement = " MATCH (n:Resource) " +
                " WHERE ID(n) = $nId " +
                " SET n.title = $title, n.resourceType = $resourceType, n.description = $description, n.sourceURI = $sourceURI" +
                " RETURN n ";
        // Open a new session
        Session session = driver.session();
        try {
            // execute update transaction
            var tmpNode = session.writeTransaction(tx -> {
                var res = tx.run(statement,
                        Values.parameters("nId", bean.getId(),
                                "title", bean.getTitle(),
                                "resourceType", bean.getResourceType(),
                                "description", bean.getDescription(),
                                "sourceURI", bean.getSourceURI())
                );
                return res.single().get("n").asNode();
            });
            // return new UnitBean for this method
            session.close();
            return new ResourceBean(
                    tmpNode.id(),
                    tmpNode.get("title").asString(),
                    tmpNode.get("resourceType").asString(),
                    tmpNode.get("description").asString(),
                    tmpNode.get("sourceURI").asString()
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
     * @param argResourceId
     * @param argAnchorId
     * @param nodeType
     */
    public void createRelationBtResourceAndOtherNodeByIds(Long argResourceId, Long argAnchorId, NodeLabel nodeType) {
        String statement = String.format(" MATCH (n1:Resource), (n2:%s) " +
                " WHERE ID(n1) = $firstId AND ID(n2) = $secondId " +
                " CREATE (n1)-[r:APPLIED_TO]->(n2) " +
                " RETURN n1,n2 ", nodeType.getNodeType());

        Map<String, Object> params = new HashMap<>();
        params.put("firstId", argResourceId);
        params.put("secondId", argAnchorId);

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

    public void createResourceWithRelation(ResourceBean resourceBean, Long argAnchorNodeId, NodeLabel argAnchorNodeType) {
        ResourceBean rBean = createSimpleResource(resourceBean);
        if (rBean == null) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "Resource cannot be created.");
        }

        Object obj = sessionHelper.getNodeById(argAnchorNodeType, argAnchorNodeId);
        if (obj == null) {
            deleteResourceAndRelationByNodeId(rBean.getId());
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR,
                    String.format("Resource cannot mounted at node with id[%s].", argAnchorNodeId));
        }
        createRelationBtResourceAndOtherNodeByIds(rBean.getId(), argAnchorNodeId, argAnchorNodeType);

    }

    /**
     * @param argResourceId
     * @param argAnchorId
     * @param argAnchorNodeType
     */
    public void deleteRelationBtResourceAndOtherNodeByIds(Long argResourceId, Long argAnchorId, NodeLabel argAnchorNodeType) {
        String statement = String.format(" MATCH (n1:Resource)-[r:APPLIED_TO]->(n2:%s) " +
                " WHERE ID(n1) = $firstId AND ID(n2)=$secondId " +
                " DELETE r ", argAnchorNodeType.getNodeType());
        Map<String, Object> params = new HashMap<>();
        params.put("firstId", argResourceId);
        params.put("secondId", argAnchorId);
        sessionHelper.delete(statement, params);
    }

    /**
     * @param argResourceId
     * @param argOldAnchorId
     * @param argNewAnchorId
     * @param nodeType
     */
    public void updateRelationByNodeIds(Long argResourceId, Long argOldAnchorId, Long argNewAnchorId, NodeLabel nodeType) {
        deleteRelationBtResourceAndOtherNodeByIds(argResourceId, argOldAnchorId, nodeType);
        createRelationBtResourceAndOtherNodeByIds(argResourceId, argNewAnchorId, nodeType);
    }

    /**
     * @param argNodeId
     */
    public void deleteResourceAndRelationByNodeId(Long argNodeId) {
        String statement = "MATCH (n:Resource)  WHERE ID(n)= $myId OPTIONAL MATCH (n)-[r]-() DELETE r,n";
        Map<String, Object> params = new HashMap<>();
        params.put("myId", argNodeId);
        sessionHelper.delete(statement, params);
    }

    public void deleteResourceAndRelationByNodeTitle(String argNodeTitle) {
        String statement = "MATCH (n:Resource)  WHERE n.title = $myTitle OPTIONAL MATCH (n)-[r]-() DELETE r,n";
        Map<String, Object> params = new HashMap<>();
        params.put("myTitle", argNodeTitle);
        sessionHelper.delete(statement, params);
    }

    /**
     * @return
     */
    public List<ResourceBean> getAll() {
        List<ResourceBean> myResult = new ArrayList<>();
        try {
            Session session = driver.session();
            myResult = session.readTransaction(tx -> {
                List<ResourceBean> myBeans = new ArrayList<>();
                Result result = tx.run("MATCH (n:Resource) RETURN n ORDER BY n.id ASC");
                while (result.hasNext()) {
                    org.neo4j.driver.Record record = result.next();
                    Node tmpNode = record.get(0).asNode();
                    // get node label
                    String tmpLabel = nodeService.getLabelOfNode(tmpNode);
                    // get node id
                    long tmpId = tmpNode.id();
                    // get title properties
                    String tmpTitle = tmpNode.get("title").asString();
                    // get resourceType properties
                    String tmpResourceType = tmpNode.get("resourceType").asString();
                    // get description properties
                    String tmpContext = tmpNode.get("description").asString();
                    // get source URI properties
                    String tmpSourceURI = tmpNode.get("sourceURI").asString();
                    // create return single bean
                    ResourceBean bean = new ResourceBean(tmpId, tmpTitle, tmpResourceType, tmpContext, tmpSourceURI);
                    myBeans.add(bean);
                }
                return myBeans;
            });
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        return myResult;
    }

    /**
     * @param argId
     * @return
     */
    public ResourceBean getResourceById(Long argId) {
        String statement = String.format(" MATCH (n:Resource) WHERE ID(n) = %d  RETURN n", argId);
        return getResourceByStatement(statement);
    }

    /**
     * @param argTitle
     * @return
     */
    public ResourceBean getResourceByTitle(String argTitle) {
        String statement = String.format(" MATCH (n:Resource) WHERE n.title = %s  RETURN n", argTitle);
        // String statement = String.format(" MATCH (n:%s {title:%s}) RETURN n;", nodeType.getNodeType(), argTitle);
        return getResourceByStatement(statement);
    }

    /**
     * @param argStatement
     * @return
     */
    private ResourceBean getResourceByStatement(String argStatement) {
        ResourceBean myResult = null;
        try {
            Session session = driver.session();
            myResult = session.readTransaction(tx -> {
                ResourceBean myBean = null;
                Result result = tx.run(argStatement);
                while (result.hasNext()) {
                    org.neo4j.driver.Record record = result.next();
                    Node tmpNode = record.get(0).asNode();
                    // get node id
                    long tmpId = tmpNode.id();
                    // get title properties
                    String tmpTitle = tmpNode.get("title").asString();
                    // get node label
                    String tmpLabel = nodeService.getLabelOfNode(tmpNode);
                    // get description properties
                    String tmpContext = tmpNode.get("description").asString();
                    // get source URI properties
                    String tmpSourceURI = tmpNode.get("sourceURI").asString();
                    // create return single bean
                    myBean = new ResourceBean(tmpId, tmpTitle, tmpLabel, tmpContext, tmpSourceURI);
                }
                return myBean;
            });
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        return myResult;
    }
}
