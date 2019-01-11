package com.cmcglobal.plugins.service;

import com.atlassian.activeobjects.tx.Transactional;
import com.cmcglobal.plugins.dto.ResultMessage;
import com.cmcglobal.plugins.dto.UploadFileDTO;
import com.cmcglobal.plugins.entity.UploadFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Transactional
public interface UploadFileService {

    ResultMessage handleEvent(HttpServletRequest request, HttpServletResponse response) throws IOException;

    void handleEventDevice(HttpServletRequest request, HttpServletResponse response) throws IOException;

    UploadFile create(final UploadFileDTO uploadFileDTO);

    void updateOrdelete(final long projectId, final String fileName, final String message);

    List<UploadFileDTO> findAllByProjectId(final long projectId, final String importType);

    UploadFile findByProjectIdAndFileName(final long projectId, final String fileName);

    UploadFile findByProjectIdAndFileNameStatus(final long projectId, final String fileName, final String status);

    List<UploadFile> findAllBigFile(final String status);

    void updateStatusFile(Long projectId, String fileUploadName, String status, String message);

}
