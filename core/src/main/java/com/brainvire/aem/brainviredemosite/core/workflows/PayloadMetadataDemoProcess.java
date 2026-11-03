package com.brainvire.aem.brainviredemosite.core.workflows;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;

@Component (
    service = WorkflowProcess.class,
    property = {
        "process.label=Payload Metadata Demo Process"
    }
)
public class PayloadMetadataDemoProcess implements WorkflowProcess {

    private static final Logger LOG =
            LoggerFactory.getLogger(PayloadMetadataDemoProcess.class);

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {
        
        WorkflowData workflowData = workItem.getWorkflowData();

        String payloadType = workflowData.getPayloadType();

        String payloadPath = workflowData.getPayload().toString();

        LOG.info("Payload type: {}", payloadType);
        LOG.info("Original payload: {}", payloadPath);

        String pagePath = payloadPath;

        if ("JCR_PATH".equals(payloadType)
                && pagePath.endsWith("/jcr:content")) {
            pagePath = pagePath.substring(
                    0, pagePath.length() - "/jcr:content".length());
        }

        LOG.info("Candidate page path: {}", pagePath);

        MetaDataMap metadata = workflowData.getMetaDataMap();

        metadata.put("lesson.source", "page-modification-launcher");
        metadata.put("lesson.originalPayload", payloadPath);
        metadata.put("lesson.candidatePagePath", pagePath);

        workflowSession.updateWorkflowData(
        workItem.getWorkflow(),
        workflowData);

        LOG.info("Workflow metadata updated for payload: {}", payloadPath);
    }
    
}
