package de.schwarz.ecm.imoprters.apps.reactivexmlparser.service.interfaces;

import de.schwarz.ecm.imoprters.apps.reactivexmlparser.dto.EDIDocumentSubmitResponseDTO;
import de.schwarz.ecm.imoprters.apps.reactivexmlparser.dto.EdiDocumentDTO;

import java.util.List;

public interface ObjectStorageService {
    EDIDocumentSubmitResponseDTO submit(EdiDocumentDTO ediDocumentDTO);

    boolean linkAttachments(List<EDIDocumentSubmitResponseDTO> buffer);
}
