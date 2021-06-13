package com.annakhuseinova.springcloudstreamsxmltojson.services;

import com.annakhuseinova.springcloudstreamsxmltojson.bindings.OrderListenerBinding;
import com.annakhuseinova.springcloudstreamsxmltojson.model.Order;
import com.annakhuseinova.springcloudstreamsxmltojson.model.OrderEnvelop;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;

import static com.annakhuseinova.springcloudstreamsxmltojson.config.AppSerdes.OrderEnvelop;
import static com.annakhuseinova.springcloudstreamsxmltojson.config.AppSerdes.String;
import static com.annakhuseinova.springcloudstreamsxmltojson.constants.AppConstants.ADDRESS_ERROR;
import static com.annakhuseinova.springcloudstreamsxmltojson.constants.AppConstants.VALID_ORDER;

@Service
@Slf4j
@EnableBinding(OrderListenerBinding.class)
public class OrderListenerService {

    @Value("${application.configs.error.topic.name}")
    private String ERROR_TOPIC;

    @StreamListener("xml-input-channel")
    @SendTo({"india-orders-channel", "abroad-orders-channel"})
    public KStream<String, Order>[] process(KStream<String, String> input){
        input.foreach((key, value)-> String.format("Received XML Order Key: %s, Value: %s", key, value));
        KStream<String, OrderEnvelop> orderEnvelopKStream = input.map((key, value)-> {
            OrderEnvelop orderEnvelop = new OrderEnvelop();
            orderEnvelop.setXmlOrderKey(key);
            orderEnvelop.setXmlOrderValue(value);
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(Order.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                orderEnvelop.setValidOrder((Order) unmarshaller.unmarshal(new StringReader(value)));
                if (orderEnvelop.getValidOrder().getShipTo().getCity().isEmpty()){
                    log.error("Missing destination city");
                    orderEnvelop.setOrderTag(ADDRESS_ERROR);
                }
            } catch (JAXBException e){
                log.error("Failed to unmarshal the incoming XML");
            }
            return KeyValue.pair(orderEnvelop.getOrderTag(), orderEnvelop);
        });

        orderEnvelopKStream.filter((key,value)-> !key.equalsIgnoreCase(VALID_ORDER))
                .to(ERROR_TOPIC, Produced.with(String(), OrderEnvelop()));

        KStream<String, Order> validOrders = orderEnvelopKStream.filter((key, value)-> key.equalsIgnoreCase(VALID_ORDER))
                .map((key, value)-> KeyValue.pair(value.getValidOrder().getOrderId(), value.getValidOrder()));
        Predicate<String, Order> isIndiaOrder = (k, v)-> v.getShipTo().getCountry().equalsIgnoreCase("india");
        Predicate<String, Order> isAbroadOrder = (k, v)-> !v.getShipTo().getCountry().equalsIgnoreCase("india");
        return validOrders.branch(isIndiaOrder, isAbroadOrder);
    }
}
