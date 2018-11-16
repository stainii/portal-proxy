package be.stijnhooft.portal.proxy.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;

/**
 * This configuration class loosens the Http Firewall class.
 * This is necessary to support Angular's router notation, which can look like this:
 *  http://localhost:4200/housagotchi/(recurring-tasks//menuBar:recurring-tasks)
 *                                    -               --       -               -
 *
 * Underlined characters are blocked by the default StrictHttpFirewall.
 */
@Configuration
public class LoosenHttpFirewall {

    @Bean
    public HttpFirewall looseHttpFirewall() {
        return new DefaultHttpFirewall(); // instead of StrictHttpFirewall
    }

}
