## Authorization Server

Implement using OAuth2 Library of Spring Cloud version Hoxton.SR10

Listen on port 7000

# Private Key

keytool command

# Public Key

override configure(AuthorizationServerSecurityConfigurer security) method so that 

the resource server could access public key via this endpoint:

/outh/token_key

