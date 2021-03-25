package com.authserver;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthServerApplicationTests {
    @Autowired
    private MockMvc mvc;

    /*
     * test password grant type (implicit flow)
     * the client send password grant_type to authorization server
     * in return of access token from authorization server
     * currently no PKCE support
     */
    @Test
    void generateTokenValidUserAndClientTest() throws Exception {
        mvc.perform(
                post("/oauth/token")
                        .with(httpBasic("client", "secret"))
                        .queryParam("grant_type", "password")
                        .queryParam("username", "john")
                        .queryParam("password", "12345")
                        .queryParam("scope", "read"))
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(status().isOk());
    }

    /*
     * test authorization_code grant type
     * the client send to authorize endpoint with client name
     * in return authorization code from authorization server
     * using the authorization code to exchange for access token
     *
     */
    @Test
    void generateAuthorizationCodeFlowWithValidUserAndClientTest() throws Exception {
        String redirect_uri = "http://localhost:7000/home";
        MvcResult mvcResult = mvc.perform(
                get("/oauth/authorize")
                        .with(httpBasic("john", "12345"))
                        .queryParam("response_type", "code")
                        .queryParam("client_id", "client")
                        .queryParam("scope", "read"))
                .andExpect(status().is3xxRedirection()).andReturn();
        String authCode = Objects.requireNonNull(mvcResult.getResponse().getHeader("Location"))
                .replace(redirect_uri + "?code=", "");
        mvc.perform(
                post("/oauth/token")
                        .with(httpBasic("client", "secret"))
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("code", authCode)
                        .queryParam("scope", "read"))
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(status().isOk());
    }

    /*
     * Test Invalid Client Password
     * Expected Unauthorized Response
     */
    @Test
    void generateAuthorizationOnceWithInValidUser() throws Exception {
        mvc.perform(
                get("/oauth/authorize")
                        .with(httpBasic("john", "123"))
                        .queryParam("response_type", "code")
                        .queryParam("client_id", "client")
                        .queryParam("scope", "read"))
                .andExpect(status().isUnauthorized()).andReturn();
    }

    /*
     * Refresh Token Test
     * Use password grant type to get access token and refresh token
     * Get new access token from the refresh token
     * The access token should be different from original access token
     *
     *
     */
    @Test
    void refreshTokenValidUserAndClientTest() throws Exception {
        MvcResult initialResult =  mvc.perform(
                post("/oauth/token")
                        .with(httpBasic("client", "secret"))
                        .queryParam("grant_type", "password")
                        .queryParam("username", "john")
                        .queryParam("password", "12345")
                        .queryParam("scope", "read"))
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(status().isOk()).andReturn();
        String initialContent = initialResult.getResponse().getContentAsString();
        Map<String, String> initialMap = new ObjectMapper().readValue(initialContent, HashMap.class);
        String refresh_token = initialMap.get("refresh_token");
        mvc.perform(
                post("/oauth/token")
                        .with(httpBasic("client", "secret"))
                        .queryParam("grant_type", "refresh_token")
                        .queryParam("refresh_token", refresh_token))
                   .andExpect(jsonPath("$.access_token").exists())
                   .andExpect(status().isOk()).andReturn();
    }

}