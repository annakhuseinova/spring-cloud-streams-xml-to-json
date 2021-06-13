package com.annakhuseinova.springcloudstreamsxmltojson.services;

import com.annakhuseinova.springcloudstreamsxmltojson.model.Order;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

@Service
public class OrderListenerService {

    @Value("${application.configs.error.topic.name}")
    private String ERROR_TOPIC;

    @StreamListener("xml-input-channel")
    @SendTo({"india-orders-channel", "abroad-orders-channel"})
    public KStream<String, Order>[] process(KStream<String, String> input){
        
    }
}
