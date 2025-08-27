package com.LysolPionex.happs.service;

import java.util.Map;

import javax.naming.AuthenticationException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException;

@Service
public class AuthService {

    private final CognitoIdentityProviderClient cognitoClient;
    
    @Value("${aws.cognito.clientId}")
    private String clientId;
    
    public AuthService(CognitoIdentityProviderClient cognitoClient) {
        this.cognitoClient = cognitoClient;
    }
    
    public String authenticate(String identifier, String password) throws AuthenticationException {

        try {
            InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                .clientId(clientId)
                .authParameters(Map.of(
                    "USERNAME", identifier,
                    "PASSWORD", password))
                .build();
            
            InitiateAuthResponse response = cognitoClient.initiateAuth(authRequest);
            return response.authenticationResult().accessToken();
            
            
        } catch (NotAuthorizedException | UserNotFoundException e) {
            throw new AuthenticationException("Invalid email/username or password");
        } catch (Exception e) {
            throw new RuntimeException("Authentication failed", e);
        }        
    }
}
