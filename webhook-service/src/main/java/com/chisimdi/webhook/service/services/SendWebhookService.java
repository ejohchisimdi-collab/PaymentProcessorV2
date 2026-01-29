package com.chisimdi.webhook.service.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
public class SendWebhookService {

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .build();

    byte[] serialize(Object obj) throws Exception {
        return MAPPER.writeValueAsBytes(obj);
    }

    String sign(String base64Secret, byte[] payload) throws Exception {
        byte[] secret = Base64.getDecoder().decode(base64Secret);

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secret, "HmacSHA256");
        mac.init(keySpec);

        byte[] rawHmac = mac.doFinal(payload);
        return Base64.getEncoder().encodeToString(rawHmac);
    }

    public void sendWebhooks(String merchantUrl,String hmacSecret,Object object)throws Exception{
        byte[] payload = serialize(object);
        long timestamp = Instant.now().getEpochSecond();


        byte[] signedPayload = (
                timestamp + "." + new String(payload, StandardCharsets.UTF_8)
        ).getBytes(StandardCharsets.UTF_8);

        String signature = sign(hmacSecret, signedPayload);
        RestClient restClient = RestClient.builder()
                .baseUrl(merchantUrl)
                .build();

        restClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Signature", signature)
                .header("X-Timestamp", String.valueOf(timestamp))
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }

}
