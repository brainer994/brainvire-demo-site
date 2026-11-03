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
        "process.label=Store Approval Decision"
    }
)
public class ApprovalDecisionMetadataProcess implements WorkflowProcess {

    private static final Logger LOG =
            LoggerFactory.getLogger(ApprovalDecisionMetadataProcess.class);

    private static final String DECISION_PROPERTY =
        "workflowApprovalDecision";

    private static final String METADATA_KEY =
        "approvalStatus";

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap arg2) throws WorkflowException {

        String payloadPath =
                String.valueOf(workItem.getWorkflowData().getPayload());

        if (!payloadPath.startsWith("/content/")) {
            throw new WorkflowException(
                    "Expected a page payload under /content: " + payloadPath);
        }

        Session session = workflowSession.adaptTo(Session.class);

        if (session == null) {
            throw new WorkflowException(
                    "Unable to obtain the JCR session.");
        }

        try {
            Node pageNode = session.getNode(payloadPath);

            if (!pageNode.hasNode("jcr:content")) {
                throw new WorkflowException(
                        "Page does not contain jcr:content: " + payloadPath);
            }

            Node contentNode = pageNode.getNode("jcr:content");

            if (!contentNode.hasProperty(DECISION_PROPERTY)) {
                throw new WorkflowException(
                        "Reviewer decision was not provided.");
            }

            String decision = contentNode
                    .getProperty(DECISION_PROPERTY)
                    .getString();

            if (!"APPROVED".equals(decision)
                    && !"REJECTED".equals(decision)) {
                throw new WorkflowException(
                        "Invalid approval decision: " + decision);
            }

            // Persist the decision for subsequent workflow steps.
            MetaDataMap workflowMetadata =
                    workItem.getWorkflowData().getMetaDataMap();

            workflowMetadata.put(METADATA_KEY, decision);

            // Remove the temporary property from the page.
            contentNode.getProperty(DECISION_PROPERTY).remove();
            session.save();

            LOG.info("Approval decision '{}' stored for payload '{}'",
                    decision, payloadPath);

        } catch (RepositoryException e) {
            throw new WorkflowException(
                    "Unable to process the approval decision.", e);
        }
        
    }
    
}
