package io.gainable.reactivexmlparser.service.interfaces;

import io.gainable.reactivexmlparser.dto.EdiDocumentDTO;
import reactor.core.publisher.Flux;

public interface EdiIntXMLParsingService {
    Flux<EdiDocumentDTO> parseEdiIntXMLAsString(String filePath);
}
