package com.irdeto.multiscreen;

import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class ServiceCall extends AbstractHandler {

    static final Logger logger = Logger.getLogger(ServiceCall.class);

    @Override
    public void handle( String target,
                        Request baseRequest,
                        HttpServletRequest request,
                        HttpServletResponse response ) throws IOException,
            ServletException
    {
        // Declare response encoding and types
        response.setContentType("text/html; charset=utf-8");

        // Declare response status code
        response.setStatus(HttpServletResponse.SC_OK);

        // Write back response
        response.getWriter().println(getServiceCall(request));

        // Inform jetty that this request has now been handled
        baseRequest.setHandled(true);
    }

    public String getServiceCall(HttpServletRequest request)
    {
        String serviceUrl = request.getParameter("serviceUrl");

        if (serviceUrl == null || serviceUrl == "") {
            return "Missing parameter 'serviceUrl'";
        }

        logger.debug("Using DNS Name Resolution for Service Call.. " + serviceUrl);
        String response = "";
        try {
            if (!serviceUrl.startsWith("http://")) {
                serviceUrl = "http://" + serviceUrl;
            }
            URL url = new URL(serviceUrl);
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            String strTemp = "";
            while (null != (strTemp = br.readLine())) {
                response += strTemp + "\n";
            }
        } catch (Exception ex) {
            logger.error("There was an error with requesting URL " + serviceUrl, ex);
            response = ex.getMessage();
        }

        return response;
    }
}
