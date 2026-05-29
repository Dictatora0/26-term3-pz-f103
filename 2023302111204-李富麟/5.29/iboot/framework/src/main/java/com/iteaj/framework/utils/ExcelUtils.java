package com.iteaj.framework.utils;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import com.iteaj.framework.exception.ServiceException;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;

public final class ExcelUtils {

    public static void exportForXSSF(Collection<?> records, Class<?> entityClass, OutputStream outputStream) throws IOException {
        final ExportParams exportParams = new ExportParams();
        final Workbook sheets = ExcelExportUtil.exportExcel(exportParams, entityClass, records);
        sheets.write(outputStream); outputStream.flush(); outputStream.flush();
    }

    public static void exportForHSSF(Collection<?> records, Class<?> entityClass, OutputStream outputStream) throws IOException {
        final ExportParams exportParams = new ExportParams();
        final Workbook sheets = ExcelExportUtil.exportExcel(exportParams, entityClass, records);
        sheets.write(outputStream); outputStream.flush(); outputStream.flush();
    }

    public static void exportExcel(Collection<?> records, Class<?> entityClass, ExportParams params, OutputStream outputStream) throws IOException {
        Workbook sheets = ExcelExportUtil.exportExcel(params, entityClass, records);
        sheets.write(outputStream);
        outputStream.flush();
        outputStream.close();
    }

    public static void exportExcel(Collection<?> records, List<ExcelExportEntity> entityList, ExportParams params, OutputStream outputStream) throws IOException {
        Workbook sheets = ExcelExportUtil.exportExcel(params, entityList, records);
        sheets.write(outputStream);
        outputStream.flush();
        outputStream.close();
    }

    public static void exportExcel(Collection<?> records, Class<?> entityClass, ExportParams params, String fileName, HttpServletResponse response) throws IOException {
        ServletOutputStream outputStream = response.getOutputStream();
        response.setCharacterEncoding("UTF-8");
        response.setHeader("content-Type", "application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8") + ".xls");
        Workbook sheets = ExcelExportUtil.exportExcel(params, entityClass, records);
        sheets.write(outputStream);
        outputStream.flush();
        outputStream.close();
    }

    public static void exportExcel(Collection<?> records, List<ExcelExportEntity> entityList, ExportParams params, String fileName, HttpServletResponse response) throws IOException {
        ServletOutputStream outputStream = response.getOutputStream();
        response.setCharacterEncoding("UTF-8");
        response.setHeader("content-Type", "application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8") + ".xls");

        Workbook sheets = ExcelExportUtil.exportExcel(params, entityList, records);
        sheets.write(outputStream);
        outputStream.flush();
        outputStream.close();
    }

    public static <T> List<T> importExcel(MultipartFile file, Class<T> clazz, ImportParams importParams) throws ServiceException {
        try {
            return ExcelImportUtil.importExcel(file.getInputStream(), clazz, importParams);
        } catch (Exception e) {
            throw new ServiceException("读取文件失败");
        }
    }
}
