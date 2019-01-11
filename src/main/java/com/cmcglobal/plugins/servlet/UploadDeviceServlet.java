package com.cmcglobal.plugins.servlet;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.cmcglobal.plugins.service.UploadFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Scanned
public class UploadDeviceServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(UploadFileServlet.class);

    @Autowired
    UploadFileService uploadFileService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        uploadFileService.handleEventDevice(request, response);
    }

}
