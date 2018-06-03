package com.example.demo.utils;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by song on 2018/3/9.
 */
public class ESutil {

   private static TransportClient client=null;

//    static {
//        try {
//            InetSocketTransportAddress node = new InetSocketTransportAddress(
//                    InetAddress.getByName("192.168.142.134"),
//                    9300
//            );
//            Settings settings= Settings.builder()
//                    .build();
//
//            TransportClient client = new PreBuiltTransportClient(settings);//bug
//            client.addTransportAddress(node);//bug
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


    public static TransportClient client()  {
        if(client==null){
            InetSocketTransportAddress node = null;
            try {
                node = new InetSocketTransportAddress(
                        InetAddress.getByName("192.168.142.134"),
                        9300
                );
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            Settings settings= Settings.builder()
                    .build();

             client = new PreBuiltTransportClient(settings);
            client.addTransportAddress(node);
        }
        return client;
    }


}
