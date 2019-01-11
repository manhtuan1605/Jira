package com.cmcglobal.plugins.service;

import com.atlassian.activeobjects.tx.Transactional;
import com.cmcglobal.plugins.dto.ResultMessage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Transactional
public interface DeviceService {

    ResultMessage imports(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
}
