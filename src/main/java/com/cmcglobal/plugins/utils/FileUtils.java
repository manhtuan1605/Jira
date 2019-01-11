package com.cmcglobal.plugins.utils;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import static com.cmcglobal.plugins.utils.Constants.TIMESTAMP_MILISECOND_FORMAT;

/**
 * @author nttha1
 */

public class FileUtils {

    /**
     * @param file
     * @return
     */
    public String getFileNameWithoutExtension(final File file) {
        String fileNameWithoutExtension = "";

        try {
            if (file != null && file.exists()) {
                final String name = file.getName();
                fileNameWithoutExtension = name.substring(0, name.lastIndexOf('.'));
            }
        } catch (final Exception e) {
            Logger.getLogger(e.getMessage());
            fileNameWithoutExtension = "";
        }

        return fileNameWithoutExtension;

    }

    /**
     * @param file
     * @return fileRenameWith extension
     */
    public static String getFileNameCurrent(final File file, final String prefix) {
        String fileName = "";
        try {
            if (file != null && file.exists()) {
                final String name = file.getName();
                fileName = FileUtils.getFileNameCurrent(name, prefix);
            }
        } catch (final Exception e) {
            Logger.getLogger(e.getMessage());
            fileName = "";
        }
        return fileName;

    }

    /**
     * @param fileName
     * @return fileRenameWith extension
     */
    public static String getFileNameCurrent(final String fileName, final String prefix) {
        final DateFormat dateFormat = new SimpleDateFormat(TIMESTAMP_MILISECOND_FORMAT);
        return FilenameUtils.getBaseName(fileName) + prefix + dateFormat.format(new Date()) + "." +
               FilenameUtils.getExtension(fileName);
    }

    /**
     * create check exist directory
     *
     * @param fileName
     */
    public static void createFoderSystem(final String fileName, final String projectKey, final String type) {
        final File file = new File(fileName);
        if (!file.exists()) {
            file.mkdir();
        }
        final File directorProject = new File(fileName + File.separator + projectKey);
        // check child folder
        if (!directorProject.exists()) {
            directorProject.mkdir();
        }
        final File directorProject2 = new File(fileName + File.separator + projectKey + File.separator + type);
        // check child folder
        if (!directorProject2.exists()) {
            directorProject2.mkdir();
        }

    }

    public static String setPathFile(final String fileName, final String projectKey, final String type, final String fi) {
        final String path = fileName + File.separator + projectKey + File.separator + type;
        FileUtils.createFoderSystem(fileName, projectKey, type);
        return path + File.separator + fi;
    }

    /**
     * @param fileUploadPath
     * @param fileName
     * @return
     * @throws IOException
     */
    public static Workbook getWorkbookByExtension(final String fileUploadPath, final String fileName)
            throws IOException, InvalidFormatException {
        try (FileInputStream fileInputStream = new FileInputStream(fileUploadPath)) {
            if (FilenameUtils.isExtension(fileName, "xls")) {
                //excel 2003 to early
                return new HSSFWorkbook(fileInputStream);
            } else if (FilenameUtils.isExtension(fileName, "xlsm")) {
                return new XSSFWorkbook(OPCPackage.open(fileInputStream));
            } else {
                return new XSSFWorkbook(fileInputStream);
            }
        }
    }

    public static Workbook getWorkbookByExtension(final FileItem fileItem, final String fileName)
            throws IOException, InvalidFormatException {
        if (FilenameUtils.isExtension(fileName, "xls")) {
            //excel 2003 to early
            return new HSSFWorkbook(fileItem.getInputStream());
        } else if (FilenameUtils.isExtension(fileName, "xlsm")) {
            return new XSSFWorkbook(OPCPackage.open(fileItem.getInputStream()));
        } else {
            return new XSSFWorkbook(fileItem.getInputStream());
        }
    }

    public static String stripExtension(final String str) {
        if (str == null) {
            return null;
        }
        final int pos = str.lastIndexOf(Constants.PREFIX_DOT);
        if (pos == -1) {
            return str;
        }

        return str.substring(0, pos);
    }

    public static void removeRow(final Sheet sheet, final int headerIndex) {
        for (int i = headerIndex; i <= sheet.getLastRowNum(); i++) {
            if (sheet.getRow(i) != null) {
                sheet.removeRow(sheet.getRow(i));
            }
        }
    }

}
