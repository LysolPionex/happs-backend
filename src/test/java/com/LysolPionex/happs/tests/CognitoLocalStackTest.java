package com.LysolPionex.happs.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.LysolPionex.happs.service.CognitoUserService;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateUserPoolRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateUserPoolResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameExistsException;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CognitoLocalStackTest {

    @MockitoBean
    private CognitoIdentityProviderClient cognitoClient;
    
    @Autowired
    private CognitoUserService cognitoUserService;
 
    private static String userPoolId = "us-east-2_testPoolId";
    private Set<String> userDB;
    
    @BeforeEach
    void setUp(){
        userDB = new HashSet<>();       //we'll use this set to mimic users being stored in a db
        
        //setup the responses in mockito for creating userpool (always succeeds)
        CreateUserPoolResponse createUserPoolResponse = CreateUserPoolResponse.builder()
            .userPool(UserPoolType.builder().id(userPoolId).build())
            .build();
        
        when(cognitoClient.createUserPool(any(CreateUserPoolRequest.class)))
        .thenReturn(createUserPoolResponse);
        
        //setup the responses in mockito for creating users
        when(cognitoClient.adminCreateUser(any(AdminCreateUserRequest.class)))
        .thenAnswer(invocation -> {
            AdminCreateUserRequest request = invocation.getArgument(0);
            String username = request.username();
            if(userDB.contains(username)) {
                throw UsernameExistsException.builder().message("User already exists!").build();
            }
            
            userDB.add(username);
            
            return AdminCreateUserResponse.builder()
                .user(UserType.builder().username(username).build())
                .build();
        });

        //setup the responses in mockito for getting users
        when(cognitoClient.adminGetUser(any(AdminGetUserRequest.class)))
        .thenAnswer(invocation -> {
            AdminGetUserRequest request = invocation.getArgument(0);
            String username = request.username();
            if(!userDB.contains(username)) {
                throw UserNotFoundException.builder().message("User not found!").build();
            }
            
            return AdminGetUserResponse.builder()
                .username(username).build();
        });
        
        //setup the responses in mockito for deleting users
        //it's flipped around from the others using a doAnswer, because the return is a null
        doAnswer(invocation -> {
            AdminDeleteUserRequest request = invocation.getArgument(0);
            String username = request.username();
            if (!userDB.remove(username)) {
                throw UserNotFoundException.builder().message("User not found").build();
            }
            return null;
        }).when(cognitoClient).adminDeleteUser(any(AdminDeleteUserRequest.class));
    }
        
    @Test
    void testAddAndDeleteUser() {

        String testUsername = "testuser_" + System.currentTimeMillis();
        String testEmail = testUsername + "@example.com";

        /*****************
         * add user tests
         *****************/
        String createdUsername = cognitoUserService.addUser(testUsername, testEmail, userPoolId);
        assertEquals(testUsername, createdUsername);
        
        // Verify existence
        String gottenUsername = cognitoUserService.getUser(testUsername, userPoolId);
        assertEquals(testUsername, gottenUsername);

        //add duplicate user
        assertThrows(UsernameExistsException.class, () -> cognitoUserService.addUser(testUsername, testEmail, userPoolId));
        
        
        /********************
         * delete user tests
         ********************/
        cognitoUserService.deleteUser(testUsername, userPoolId);
        
        // Verify deletion
        assertThrows(UserNotFoundException.class, () -> cognitoUserService.getUser(testUsername, userPoolId));
        
        //delete same user again
        assertThrows(UserNotFoundException.class, () -> cognitoUserService.deleteUser(testUsername, userPoolId));
        
        //delete non-existent user
        assertThrows(UserNotFoundException.class, () -> cognitoUserService.deleteUser("userNotExist", userPoolId));
    }
}
