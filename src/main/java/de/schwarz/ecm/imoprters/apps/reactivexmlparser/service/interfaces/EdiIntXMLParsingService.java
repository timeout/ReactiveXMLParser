package de.schwarz.ecm.imoprters.apps.reactivexmlparser.service.interfaces;

import de.schwarz.ecm.imoprters.apps.reactivexmlparser.dto.EdiDocumentDTO;
import reactor.core.publisher.Flux;

public interface EdiIntXMLParsingService {
    Flux<EdiDocumentDTO> parseEdiIntXMLAsString(String filePath);
}
