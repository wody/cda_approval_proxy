package com.sixsentix.cda.ApprovalProxy.controller;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@RestController
public class ApprovalProxyController {

    Logger logger = LoggerFactory.getLogger(ApprovalProxyController.class);

    @GetMapping("/approve")
    public ResponseEntity<String> proxy(@RequestParam String host, @RequestParam String token, @RequestParam String approver, String comment) {
        HttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(host + "/api/approval_requests");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("approvalToken", token));
        params.add(new BasicNameValuePair("approver", approver));
        if (comment != null) params.add(new BasicNameValuePair("comment", comment));

        logger.info("URI: " + post.getURI());
        logger.info("Params: " + params);

        ResponseEntity<String> responseEntity = null;
        try {
            post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            HttpResponse response = client.execute(post);

            int statusCode = response.getStatusLine().getStatusCode();
            logger.info("Request returned with code: " + statusCode);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream content = entity.getContent();
                byte[] bytes = StreamUtils.copyToByteArray(content);
                logger.info("Approval return: " + new String(bytes));

                responseEntity = new ResponseEntity<>(new String(bytes), HttpStatus.valueOf(statusCode));
            }
        } catch (IOException e) {
            logger.error("Unexpected error during POST call\n" + e.getMessage());
        }

        return responseEntity;
    }
}
