package com.bialystok.events;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.ws.Endpoint;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;

public class Server {

    public static void main(String[] args) {
        try {
            // 1. Setup HTTPS Server
            HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress("localhost", 8443), 0);
            
            // 2. Load Keystore (for SSL/TLS - Additional point requirement)
            SSLContext sslContext = SSLContext.getInstance("TLS");
            char[] password = "password".toCharArray();
            KeyStore ks = KeyStore.getInstance("JKS");
            
            try (FileInputStream fis = new FileInputStream("keystore.jks")) {
                ks.load(fis, password);
            } catch (Exception e) {
                System.err.println("Failed to load keystore.jks. Please run generate_keystore.bat first.");
                System.err.println("Fallback to HTTP (not recommended).");
                Endpoint.publish("http://localhost:8080/ws/events", new BialystokEventServiceImpl());
                System.out.println("Service started at http://localhost:8080/ws/events?wsdl");
                return;
            }

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, password);
            
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);

            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext));

            // 3. Create context and publish endpoint
            HttpContext httpContext = httpsServer.createContext("/ws/events");
            httpsServer.start();

            Endpoint endpoint = Endpoint.create(new BialystokEventServiceImpl());
            endpoint.publish(httpContext);

            System.out.println("HTTPS Web Service is running!");
            System.out.println("WSDL available at: https://localhost:8443/ws/events?wsdl");
            System.out.println("Press Ctrl+C to stop...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
