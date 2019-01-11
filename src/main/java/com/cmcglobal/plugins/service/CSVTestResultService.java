package com.cmcglobal.plugins.service;

import com.atlassian.activeobjects.tx.Transactional;
import com.cmcglobal.plugins.entity.CSVTestResult;

@Transactional
public interface CSVTestResultService {
    CSVTestResult createOrUpdate(final String fileName);
}
