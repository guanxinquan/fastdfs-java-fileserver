package com.example.fileserver;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

/**
 * Created by guanxinquan on 16/2/25.
 */
public class FileServerTest {

    public static void main(String[] args) throws IOException, URISyntaxException {

        URI uri = new URIBuilder("http://localhost:12306/upload")
                .addParameter("withtag","true")
                .addParameter("filename","test.png")
                .build();

        HttpPost post = new HttpPost(uri);
        FileInputStream file = new FileInputStream("/Users/HaiZhi/Desktop/test.png");
        InputStreamEntity inputStreamEntity = new InputStreamEntity(file,ContentType.APPLICATION_OCTET_STREAM);
        post.setEntity(inputStreamEntity);

        ClientConnectionPool clientConnectionPool = new ClientConnectionPool();
        String result = clientConnectionPool.execute(post);

        System.out.println(result);


    }


}
