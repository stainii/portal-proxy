package be.stijnhooft.portal.proxy.security;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class JwtConfig {

    @Value("${security.jwt.header:Authorization}")
    @Getter
    private String header;

    @Value("${security.jwt.prefix:Bearer }")
    @Getter
    private String prefix;

    @Value(value = "${security.jwt.expiration:#{24*60*60}}")
    @Getter
    private int expiration;

    @Value("${security.jwt.secret:JwtSecretKey}")
    @Getter
    private String secret;

}
