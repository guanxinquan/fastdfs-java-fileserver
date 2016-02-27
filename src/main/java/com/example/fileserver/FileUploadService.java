package com.example.fileserver;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by guanxinquan on 16/2/25.
 */
public class FileUploadService extends HttpServlet{

    private FastdfsFileService fileService;

    private static final Integer MAX_MEMORY_SIZE = 1024 * 10;//10M

    private static final Integer MAX_SIZE = 1024 * 20;//20M


    private  ServletFileUpload upload;

    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);

    private static final DateFormat format = new SimpleDateFormat("yy年MM月dd日 HH:mm:ss");



    @Override
    public void init() throws ServletException {
        super.init();
        try {
            fileService = new FastdfsFileService();
            DiskFileItemFactory factory = new DiskFileItemFactory();

            factory.setSizeThreshold(MAX_MEMORY_SIZE);
            factory.setRepository((File) getServletContext().getAttribute("javax.servlet.context.tempdir"));
            upload = new ServletFileUpload(factory);
            upload.setSizeMax(MAX_SIZE);
        } catch (Exception e) {
            logger.error("start file service error",e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //super.doPost(req, resp);
        String withTag = req.getParameter("withtag");
        String tags = null;
        String url = null;
        if(!StringUtils.isEmpty(withTag)){
            tags = format.format(new Date());
        }
        try {
            if (ServletFileUpload.isMultipartContent(req)) {//客户端用multipart的形式上传文件
                List<FileItem> items = upload.parseRequest(req);
                Iterator<FileItem> iter = items.iterator();
                while (iter.hasNext()) {
                    FileItem item = iter.next();
                    if (item.isFormField()) {
                        //忽略form field
                    } else {
                        String fileName = item.getName();
                        InputStream inputStream = item.getInputStream();
                        url = fileService.saveFile(fileName, tags, inputStream);
                    }
                }
            } else {//客户端直接用binary的形式上传文件
                InputStream inputStream = req.getInputStream();
                String fileName = req.getParameter("filename");
                url = fileService.saveFile(fileName, tags, inputStream);
            }
            resp.getOutputStream().write(url.getBytes());
            resp.getOutputStream().close();
        }catch (Exception e){
            logger.error("file upload error",e);
        }
    }
}
