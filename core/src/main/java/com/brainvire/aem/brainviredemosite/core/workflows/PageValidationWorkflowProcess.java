package com.brainvire.aem.brainviredemosite.core.workflows;


import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;

@Component (
    service = WorkflowProcess.class,
    property = {
        "process.label= Page Validation Process"
    }

)
public class PageValidationWorkflowProcess implements WorkflowProcess {

    private static final Logger log = LoggerFactory.getLogger(PageValidationWorkflowProcess.class);

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {

        if(workItem == null || workItem.getWorkflowData() == null) {
            throw new WorkflowException("WorkFlow data is unavailable.");
        }

        Object payLoad = workItem.getWorkflowData().getPayload();

        if (payLoad == null || payLoad.toString().isEmpty()) {
            throw new WorkflowException("PayLoad is unavailable.");
        }

        String pagePath = payLoad.toString();

        if(!pagePath.startsWith("/content/")) {
            throw new WorkflowException("Expected an AEM Page under /content: " + pagePath);
        }
        
        Session session = workflowSession.adaptTo(Session.class);

        if(session == null) {
            throw new WorkflowException("Unable to adapt Workflow Session to JCR Session.");
        }

        try{

            if(!session.nodeExists(pagePath)){
                throw new WorkflowException("Page does not exist at path: " + pagePath);
            }

            Node pageNode = session.getNode(pagePath);

            if (!pageNode.hasNode("jcr:content")) {
                throw new WorkflowException(
                        "Page does not contain jcr:content: " + pagePath);
            }
            
            Node contentNode = pageNode.getNode("jcr:content");

            if (!contentNode.hasProperty("jcr:title")
                    || isBlank(contentNode.getProperty("jcr:title").getString())) {

                throw new WorkflowException(
                        "Page title is mandatory: " + pagePath);
            }

            String title = contentNode.getProperty("jcr:title").getString();

            log.info(
                    "Page validation successful. Path={}, Title={}",
                    pagePath,
                    title);

        } catch (RepositoryException e) {
            log.error(
                    "Repository error while validating page: {}",
                    pagePath,
                    e);

            throw new WorkflowException(
                    "Unable to validate page: " + pagePath, e);
        }
        
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
    
}
