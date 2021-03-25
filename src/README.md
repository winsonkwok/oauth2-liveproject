## Authorization Server

OAuth2 Library of Spring Cloud version Hoxton.SR10

Listen on port 7000

### Private Key

keytool command

### Public Key

override configure(AuthorizationServerSecurityConfigurer security) method so that 

the resource server could access public key via this endpoint:

/outh/token_key

### Refresh Token Endpoint test

based on following two discussions in stackoverflow:
https://stackoverflow.com/questions/58931365/refresh-token-grant-type-error-userdetailsservice-is-required-but-i-dont-want
and
https://stackoverflow.com/questions/34716636/no-authenticationprovider-found-on-refresh-token-spring-oauth2-java-config

DefaultTokenSerivce and createPreAuth methods added into AuthServerConfig class

Without DefaultTokenService method, the refresh token endpoint will unable to refresh access token with following Error:
Unable to find default UserDetail Service

Without createPreAuth method, the refresh token endpoint will unable to refresh access token with following Error:
No AuthenticationProvider found for org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
