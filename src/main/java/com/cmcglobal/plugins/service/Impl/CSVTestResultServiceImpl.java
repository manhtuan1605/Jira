package com.cmcglobal.plugins.service.Impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.cmcglobal.plugins.entity.CSVTestResult;
import com.cmcglobal.plugins.service.CSVTestResultService;
import net.java.ao.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;

@Scanned
@Named
public class CSVTestResultServiceImpl implements CSVTestResultService {

    @ComponentImport
    private final        ActiveObjects ao;
    private static final Logger        log = LoggerFactory.getLogger(CSVTestResultServiceImpl.class);

    @Inject
    public CSVTestResultServiceImpl(final ActiveObjects ao) {
        this.ao = ao;
    }

    @Override
    public CSVTestResult createOrUpdate(final String fileName) {
        final CSVTestResult[] csvTestResults = ao.find(CSVTestResult.class, Query.select().where("UPLOAD_FILE_NAME = ? ", fileName));
        final CSVTestResult csvTestResult;
        if (csvTestResults.length > 0) {
            csvTestResult = csvTestResults[0];
            csvTestResult.setUpdateDate(new Date());
        } else {
            csvTestResult = ao.create(CSVTestResult.class);
            csvTestResult.setCreateDate(new Date());
            csvTestResult.setUploadFileName(fileName);
        }
        csvTestResult.save();
        return csvTestResult;
    }
}
