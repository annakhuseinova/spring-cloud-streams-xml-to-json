package com.annakhuseinova.springcloudstreamsxmltojson.bindings;

import com.annakhuseinova.springcloudstreamsxmltojson.model.Order;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.cloud.stream.annotation.Input;

public interface OrderListenerBinding {

    @Input("xml-input-channel")
    KStream<String, String> xmlInputStream();

    @Input("india-orders-channel")
    KStream<String, Order> indiaOutputChannel();

    @Input("abroad-orders-channel")
    KStream<String, Order> abroadOutputStream();
}
