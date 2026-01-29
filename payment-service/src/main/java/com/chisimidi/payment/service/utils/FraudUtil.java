package com.chisimidi.payment.service.utils;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class FraudUtil {
    int fraudScore;
    List<String>warnings=new ArrayList<>();

}
