package com.example.fileserver;


import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Created by guanxinquan on 16/2/25.
 */
public class FastdfsFileService {

    TrackerServer trackerServer ;

    private static final String urlPrefix = "http://192.168.99.100:8080/";

    public FastdfsFileService() throws IOException, MyException {
        String config = this.getClass().getResource("/fdfs_client.conf").getFile();
        ClientGlobal.init(config);
        TrackerClient client = new TrackerClient();
        trackerServer = client.getConnection();
    }

    public String saveFile(String fileName,String tag,InputStream inputStream) throws IOException, MyException {

        ByteArrayOutputStream outputStream = translateToByteArray(inputStream);
        String ext = getExtName(fileName);

        if(!StringUtils.isEmpty(tag)){//添加水印
            ByteArrayInputStream bi = new ByteArrayInputStream(outputStream.toByteArray());
            outputStream = pressText(bi,tag,ext);
        }

        StorageClient1 client1 = new StorageClient1(trackerServer,null);
        NameValuePair[] metaList = new NameValuePair[3];
        metaList[0] = new NameValuePair("filename",fileName);
        metaList[1] = new NameValuePair("fileExtName",ext);
        metaList[2] = new NameValuePair("fileLength",String.valueOf(outputStream.size()));
        String path = client1.upload_file1("image",outputStream.toByteArray(),ext,metaList);
        return urlPrefix+path;
    }

    private String getExtName(String fileName){
        String[] splits = fileName.split("\\.");
        if(splits.length > 1) {
            return splits[splits.length - 1];
        }else{
            return "";
        }
    }

    private ByteArrayOutputStream translateToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(inputStream,out);
        return out;
    }

    private ByteArrayOutputStream pressText(InputStream inputStream,String content,String suffix) throws IOException {
        Integer w = 100;
        Integer h = 100;
        Integer x = 20;
        Integer y = 20;
        Image src = ImageIO.read(inputStream);
        int  wideth = src.getWidth( null );
        int  height = src.getHeight( null );
        BufferedImage image = new  BufferedImage(wideth, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics g = image.createGraphics();
        g.drawImage(src, 0 ,  0 , wideth, height,  null );
        g.setColor(Color.RED);
        g.setFont(new  Font("宋体", Font.BOLD, 50));
        g.drawString(content, w/2 + x , w/2 + y);
        g.dispose();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image,suffix,out);
        return out;
    }

}
