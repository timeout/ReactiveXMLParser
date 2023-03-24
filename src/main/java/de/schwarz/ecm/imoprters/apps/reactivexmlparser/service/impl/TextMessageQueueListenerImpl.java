package de.schwarz.ecm.imoprters.apps.reactivexmlparser.service.impl;

import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.ConsumerFlowProperties;
import com.solacesystems.jcsmp.FlowReceiver;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.XMLMessageListener;
import de.schwarz.ecm.imoprters.apps.reactivexmlparser.service.interfaces.TextMessageQueueListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import javax.annotation.PreDestroy;

@Slf4j
@Service
public class TextMessageQueueListenerImpl implements TextMessageQueueListener {
    private final JCSMPSession jcsmpSession;

    public TextMessageQueueListenerImpl(JCSMPSession jcsmpSession) {
        this.jcsmpSession = jcsmpSession;
    }

    @Override
    public Flux<TextMessage> listen(String queueName) {
        Sinks.Many<TextMessage> sink = Sinks.many().multicast().onBackpressureBuffer();

        ConsumerFlowProperties flowProperties = new ConsumerFlowProperties();
        flowProperties.setEndpoint(JCSMPFactory.onlyInstance().createQueue(queueName));
        flowProperties.setAckMode(JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT);

        try {
            FlowReceiver flowReceiver = jcsmpSession.createFlow(
                    new TextMessageXMLMessageListener(sink),
                    flowProperties
            );

            flowReceiver.start();

            return Flux.from(sink.asFlux()).doOnCancel(() -> {
                    flowReceiver.stop();
                    flowReceiver.close();
            });

        } catch (JCSMPException e) {
            throw new RuntimeException("Error creating Solace queue listener", e);
        }
    }

    @PreDestroy
    public void cleanUp() {
        jcsmpSession.closeSession();
    }

    private record TextMessageXMLMessageListener(
            Sinks.Many<TextMessage> sink) implements XMLMessageListener {

        @Override
        public void onReceive(BytesXMLMessage bytesXMLMessage) {
            if (bytesXMLMessage instanceof TextMessage textMessage) {
                sink.tryEmitNext(textMessage);
            }

            bytesXMLMessage.ackMessage();
        }

        @Override
        public void onException(JCSMPException e) {
            sink.tryEmitError(e);
        }
    }
}
