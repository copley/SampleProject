package com.irdeto.multiscreen;

import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.IOException;
import java.util.Random;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class Health extends AbstractHandler {

    static final Logger logger = Logger.getLogger(Health.class);
    static final Random random = new Random();

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
        String fail = request.getParameter("fail");

        if (fail != null && fail == "true" || random.nextInt(3) == 0) {
            logger.debug("Force Failing Health Check based on input/random case..");
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
        }

        // Write back response
        response.getWriter().println(getHealth());

        // Inform jetty that this request has now been handled
        baseRequest.setHandled(true);
    }

    public String getHealth()
    {
        logger.debug("Health check call");

        return "{"
            + "\"HealthCheck\": {"
            + "\"weight\": {"
            +        "\"Health\": 1,"
            +        "\"ConsulServices\": 2,"
            +        "\"HelloWorld\": 1,"
            +        "\"ServiceCall\": 4"
            +    "},"
            +    "\"limit\": 10,"
            +    "\"active\": {"
            +        "\"Health\": 2,"
            +        "\"ConsulServices\": 0,"
            +        "\"HelloWorld\": 1,"
            +        "\"ServiceCall\": 1"
            +    "}"
            +   "}"
            + "}";
    }
}
