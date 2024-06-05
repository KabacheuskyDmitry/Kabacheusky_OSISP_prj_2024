package com.example.protocol;
import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class Client {

    public static void main(String[] args) throws Exception {
        // Загрузка хранилища ключей
        KeyStore ks = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream("/home/dimon/kursovaiya/client.keystore.jks")) {
            ks.load(fis, "secret".toCharArray());
        }

        // Инициализация KeyManagerFactory
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, "secret".toCharArray());

        // Создание SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }}, null);

        // Настройка HttpsURLConnection для использования SSLContext
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

        URL url = new URL("https://localhost:8443/");
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true); // Необходимо для отправки тела запроса, если оно есть
        connection.setRequestProperty("Host", "localhost"); // Установка заголовка Host
        connection.connect(); // Вызываем connect() перед чтением ответа

        connection.setSSLSocketFactory(sslContext.getSocketFactory());
        connection.setHostnameVerifier((hostname, session) -> true);

        try {
            // Попытка установить соединение
            connection.connect();
            System.out.println("Certification has been successful");
        } catch (SSLException e) {
            // Обработка исключения, связанного с ошибкой SSL/TLS
            System.err.println("Ошибка SSL/TLS: " + e.getMessage());
            return;
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine())!= null) {
            content.append(inputLine);
        }
        in.close();

        System.out.println(content.toString());
    }
}

