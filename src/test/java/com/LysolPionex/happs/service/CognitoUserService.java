package com.LysolPionex.happs.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;

@Service
public class CognitoUserService {
    @Autowired
    private final CognitoIdentityProviderClient cognitoClient;
    private final String defaultUserPoolId;
    
    public CognitoUserService(CognitoIdentityProviderClient cognitoClient,
                              @Value("${aws.cognito.userPoolId:#{null}}") String defaultUserPoolId) {
        this.cognitoClient = cognitoClient;
        this.defaultUserPoolId = defaultUserPoolId;
    }

    //currently return username for chaining requests.  Might want to make it a Result<UserDto> at some point? 
    public String addUser(String username, String email, String userPoolId) {
        String poolId = userPoolId != null ? userPoolId : defaultUserPoolId;
        
        AdminCreateUserRequest request = AdminCreateUserRequest.builder()
            .userPoolId(poolId)
            .username(username)
            .userAttributes(
                AttributeType.builder().name("email").value(email).build(),
                AttributeType.builder().name("email_verified").value("true").build()
                )
            .messageAction("SUPPRESS")          //messages sent to the user (e.g. "thanks for joining...")
            .build();
        
        AdminCreateUserResponse response = cognitoClient.adminCreateUser(request);
        
        return response.user().username();    
    }
    
    public String getUser(String username, String userPoolId) {
        
        AdminGetUserRequest getRequest = AdminGetUserRequest.builder()
            .userPoolId(userPoolId)
            .username(username)
            .build();
        AdminGetUserResponse response = cognitoClient.adminGetUser(getRequest);
    
        return response.username();
    }
    
    public void deleteUser(String username, String userPoolId) {
        String poolId = userPoolId != null ? userPoolId : defaultUserPoolId;
        
        AdminDeleteUserRequest request = AdminDeleteUserRequest.builder()
            .userPoolId(poolId)
            .username(username)
            .build();
        
        cognitoClient.adminDeleteUser(request);
    }
}
