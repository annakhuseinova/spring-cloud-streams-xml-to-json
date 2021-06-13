package com.annakhuseinova.springcloudstreamsxmltojson.model;

import lombok.Data;

@Data
public class OrderEnvelop {

    private String xmlOrderKey;
    private String xmlOrderValue;
    private String orderTag;
    private Order validOrder;
}
