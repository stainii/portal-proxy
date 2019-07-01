package be.stijnhooft.portal.proxy.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class SecurityFilterConfiguration {

    private final JwtConfig jwtConfig;

    public SecurityFilterConfiguration(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .cors().and()
            .csrf().disable()
            .logout().disable()
            .httpBasic().disable()
            .formLogin().disable()

            // make it possible for the front-end to access the Authorization header
            .addFilterAt(exposeAuthorizationHeader(), SecurityWebFiltersOrder.LAST)

            // authenticate with the jwt token with every request (when provided)
            .addFilterAt(jwtTokenAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION)

            .authorizeExchange()
            // allow all who are accessing the auth service, front-end and public APIs
            .pathMatchers("/auth-service/**").permitAll()
            .pathMatchers("/front-end/**").permitAll()
            .pathMatchers("/notifications/api/notification/*/action/url/").permitAll()
            .pathMatchers(HttpMethod.OPTIONS).permitAll()

            // Any other request must be authenticated
            .anyExchange().authenticated()
            .and()

            // handle an unauthorized attempt
            .exceptionHandling().authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))

            .and()
            .build();
    }


    private WebFilter exposeAuthorizationHeader() {
        return (exchange, chain) -> {
            exchange.getResponse().getHeaders().add("Access-Control-Expose-Headers", "Authorization");
            return unauthenticated(exchange, chain);
        };
    }


    private WebFilter jwtTokenAuthenticationFilter() {
        return (exchange, chain) -> {
            // 1. get the authentication header. Tokens are supposed to be passed in the authentication header
            List<String> authorizationHeaders = exchange.getRequest().getHeaders().get(jwtConfig.getHeader());
            String authorizationHeader = CollectionUtils.isEmpty(authorizationHeaders) ? null : authorizationHeaders.get(0);

            // No authorization header provided? Proceed without adding a logged in user on the context
            // Spring security will return a 401 later on, when trying to access a secured endpoint without authentication.
            // If the endpoint is not secured, Spring security will let it through.
            if(authorizationHeader == null) {
                return unauthenticated(exchange, chain);
            }

            // 2. validate the header and check the prefix
            if (!authorizationHeader.startsWith(jwtConfig.getPrefix())) {
                throw new IllegalArgumentException("JWT token's header is not valid");
            }

            // 3. Get the token
            String token = authorizationHeader.replace(jwtConfig.getPrefix(), "");

            try {	// exceptions might be thrown in creating the claims if for example the token is expired

                // 4. Validate the token
                Claims claims = Jwts.parser()
                    .setSigningKey(jwtConfig.getSecret().getBytes())
                    .parseClaimsJws(token)
                    .getBody();

                String username = claims.getSubject();
                if(username != null) {
                    @SuppressWarnings("unchecked")
                    List<String> authorities = (List<String>) claims.get("authorities");

                    // 5. Create auth object
                    // UsernamePasswordAuthenticationToken: A built-in object, used by spring to represent the current authenticated / being authenticated user.
                    // It needs a list of authorities, which has type of GrantedAuthority interface, where SimpleGrantedAuthority is an implementation of that interface
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        username, null, authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));

                    // 6. Authenticate the user
                    // Now, user is authenticated
                    return authenticated(exchange, chain, auth);
                } else {
                    log.debug("JWT token's username is not valid");
                    return unauthenticated(exchange, chain);
                }

            } catch (Exception e) {
                log.error("Error while validating JWT token: ", e);
                return unauthenticated(exchange, chain);
            }
        };
    }

    private Mono<Void> authenticated(ServerWebExchange exchange, WebFilterChain chain, UsernamePasswordAuthenticationToken auth) {
        return chain.filter(exchange).subscriberContext(ReactiveSecurityContextHolder.withAuthentication(auth));
    }

    private Mono<Void> unauthenticated(ServerWebExchange exchange, WebFilterChain chain) {
        ReactiveSecurityContextHolder.clearContext();
        return chain.filter(exchange);
    }


}
