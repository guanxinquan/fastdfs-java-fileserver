package com.example.fileserver;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by guanxinquan on 16/2/25.
 */
public class ClientConnectionPool {
    private static CloseableHttpClient httpClient = null;

    private static final Logger logger = LoggerFactory.getLogger(ClientConnectionPool.class);

    @PostConstruct
    private void init(){
        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.create();
        ConnectionSocketFactory plainSF = new PlainConnectionSocketFactory();
        registryBuilder.register("http",plainSF);

        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            TrustStrategy anyTrustStrategy = new TrustStrategy() {//默认所有证书都可信
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            };
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(trustStore, anyTrustStrategy).build();
            LayeredConnectionSocketFactory sslSF = new SSLConnectionSocketFactory(sslContext);
            registryBuilder.register("https", sslSF);
        }catch (Exception e){
            logger.error("https create error",e);
        }

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registryBuilder.build());
        cm.setMaxTotal(10);
        cm.setDefaultMaxPerRoute(10);
        HttpHost localhost = new HttpHost("localhost",80);
        cm.setMaxPerRoute(new HttpRoute(localhost),10);
        httpClient = HttpClients.custom().setConnectionManager(cm).build();

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 执行http请求
     * @param request
     * @return
     * @throws IOException
     */
    public String execute(final HttpUriRequest request) throws IOException {
        if(httpClient == null){
            init();
        }
        CloseableHttpResponse response = null;
        String responseStr = null;
        try {
            response = httpClient.execute(request);
            responseStr = EntityUtils.toString(response.getEntity(), Charset.forName("utf-8"));
        }finally {
            if(response != null)
                response.close();
        }

        return responseStr;
    }

    @PreDestroy
    private void shutDown(){
        if(httpClient != null){
            try {
                httpClient.close();
            } catch (IOException e) {

            }
        }
    }

}
