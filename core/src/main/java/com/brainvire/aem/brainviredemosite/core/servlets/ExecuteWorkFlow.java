package com.brainvire.aem.brainviredemosite.core.servlets;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.model.WorkflowModel;

import javax.servlet.Servlet;
import org.apache.sling.api.resource.ResourceResolver;

@Component (service=Servlet.class)
@SlingServletPaths (
    value ={"/bin/executeWorkflow","/bv-version/executeworkflow"}
)
public class ExecuteWorkFlow extends SlingSafeMethodsServlet {
    
    private static final Logger LOG = LoggerFactory.getLogger(ExecuteWorkFlow.class); 

    @Override 
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response) throws IOException {

        String status = "Workflow Executing";

        final ResourceResolver resourceResolver = request.getResourceResolver();

        String payload = request.getRequestParameter("page").toString();

        try{
            LOG.info("\n INSIDE EXECUTE WORKFLOW SERVLET......");
            if(StringUtils.isNotBlank(payload)){

                WorkflowSession workflowSession = resourceResolver.adaptTo(WorkflowSession.class);

                WorkflowModel workflowModel = workflowSession.getModel("/var/workflow/models/brainvire-page-version");

                WorkflowData workflowData = workflowSession.newWorkflowData("JCR_PATH", payload);

                status = workflowSession.startWorkflow(workflowModel, workflowData).getState();
            
            }

        }catch(Exception e){
            LOG.info("\n ERROR IN WORKFLOW", e.getMessage());
        }

        response.setContentType("application/json");
        response.getWriter().write(status);
    }

}
