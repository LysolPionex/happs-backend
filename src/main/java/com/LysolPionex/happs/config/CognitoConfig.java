package com.LysolPionex.happs.config;

import java.net.URI;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClientBuilder;

@Configuration
public class CognitoConfig {
    @Value("${aws.cognito.region:us-east-2}")
    private String region;
    
    @Value("${aws.cognito.endpoint:#{null}}")
    private String endpoint;
    
    @Value("${aws.cognito.accessKey:#{null}}")
    private String accessKey;
    
    @Value("${aws.cognito.secretKey:#{null}}")
    private String secretKey;
    
    @Bean
    public CognitoIdentityProviderClient cognitoClient() {
        CognitoIdentityProviderClientBuilder builder = CognitoIdentityProviderClient.builder()
            .region(Region.of(region));
        
        Optional.ofNullable(endpoint).ifPresent(ep -> builder.endpointOverride(URI.create(ep)));
        
        if(accessKey == null || secretKey == null) {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }
        else {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
            ));
        }
        
        return builder.build();
    }
}
