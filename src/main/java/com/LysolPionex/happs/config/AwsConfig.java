package com.LysolPionex.happs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;

@Configuration
public class AwsConfig {

    @Value("${aws.cognito.region}")
    private String region;
    
    @Value("${aws.accessKeyId}")
    private String accessKeyId;
    
    @Value("${aws.secretKey}")
    private String secretKey;
    
    @Bean
    AWSCognitoIdentityProvider cognitoClient() {
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKeyId, secretKey);
        
        return AWSCognitoIdentityProviderClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withRegion(region)
            .build();
    }
}
