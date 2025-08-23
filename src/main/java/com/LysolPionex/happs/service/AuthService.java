package com.LysolPionex.happs.service;

import javax.naming.AuthenticationException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.amazonaws.services.cognitoidp.model.InitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.InitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.NotAuthorizedException;
import com.amazonaws.services.cognitoidp.model.UserNotFoundException;

@Service
public class AuthService {

    private final AWSCognitoIdentityProvider cognitoClient;
    
    @Value("${aws.cognito.clientId}")
    private String clientId;
    
    public AuthService(AWSCognitoIdentityProvider cognitoClient) {
        this.cognitoClient = cognitoClient;
    }
    
    public String authenticate(String identifier, String password) throws AuthenticationException {

        try {
            InitiateAuthRequest authRequest = new InitiateAuthRequest()
                .withAuthFlow(AuthFlowType.USER_PASSWORD_AUTH)
                .withClientId(clientId)
                .addAuthParametersEntry("USERNAME", identifier)
                .addAuthParametersEntry("PASSWORD", password);
            
            InitiateAuthResult result = cognitoClient.initiateAuth(authRequest);
            return result.getAuthenticationResult().getAccessToken();
            
            
        } catch (NotAuthorizedException | UserNotFoundException e) {
            throw new AuthenticationException("Invalid email/username or password");
        } catch (Exception e) {
            throw new RuntimeException("Authentication failed", e);
        }        
    }
}
