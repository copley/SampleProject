package com.irdeto.multiscreen;

import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.IOException;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class HelloWorld extends AbstractHandler {

    static final Logger logger = Logger.getLogger(HelloWorld.class);

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
        response.getWriter().println(getHelloWorld());

        // Inform jetty that this request has now been handled
        baseRequest.setHandled(true);
    }

    public String getHelloWorld()
    {
        String consulServices = "Hello World!";
        logger.debug("Custom DEBUG Message: Called getHelloWorld()");
        return consulServices;
    }
}