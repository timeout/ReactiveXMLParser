package de.schwarz.ecm.imoprters.apps.reactivexmlparser.service.interfaces;

import com.solacesystems.jcsmp.TextMessage;
import reactor.core.publisher.Flux;

public interface TextMessageQueueListener {
    Flux<TextMessage> listen(String queueName);
}
