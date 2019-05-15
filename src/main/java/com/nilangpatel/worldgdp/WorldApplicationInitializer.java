package com.nilangpatel.worldgdp;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

// used to register Spring's DispatcherServlet instance and uses the other @Configuration classes to configure
// the DispatcherServlet
public class WorldApplicationInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    
    // point to the configuration classes that need to load into the servlet context
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return null;
    }

    // point to the configuration classes that need to load into the servlet context
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[] {AppConfiguration.class};
    }

    // provide the servlet mapping for DispatcherServlet
    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }
}