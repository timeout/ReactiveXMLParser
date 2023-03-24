package de.schwarz.ecm.imoprters.apps.reactivexmlparser.configuration;

import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.Queue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.stream.Collectors;

@Slf4j
@Configuration
public class SolaceConfiguration {

    @Bean
    public JCSMPProperties jcsmpProperties(
            @Value("${solace.java.host}") String solaceHost,
            @Value("${solace.java.msgVpn}") String solaceMsgVpn,
            @Value("${solace.java.clientUsername}") String solaceUsername,
            @Value("${solace.java.clientPassword}") String solacePassword
    ) {
        log.info("solaceHost: {}", solaceHost);
        log.info("solaceMsgVpn: {}", solaceMsgVpn);
        log.info("solaceUsername: {}", solaceUsername);
        log.info("solacePassword: {}{}", solacePassword.charAt(0),
                solacePassword.substring(1).chars()
                        .mapToObj(c -> "*")
                        .collect(Collectors.joining())
        );

        JCSMPProperties properties = new JCSMPProperties();
        properties.setProperty(JCSMPProperties.HOST, solaceHost);
        properties.setProperty(JCSMPProperties.VPN_NAME, solaceMsgVpn);
        properties.setProperty(JCSMPProperties.USERNAME, solaceUsername);
        properties.setProperty(JCSMPProperties.PASSWORD, solacePassword);

        return properties;
    }

    @Bean
    public JCSMPSession jcsmpSession(JCSMPProperties jcsmpProperties) throws JCSMPException {
        return JCSMPFactory.onlyInstance().createSession(jcsmpProperties);
    }

}
