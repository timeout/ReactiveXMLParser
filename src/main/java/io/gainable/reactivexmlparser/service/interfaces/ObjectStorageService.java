package io.gainable.reactivexmlparser.service.interfaces;

import io.gainable.reactivexmlparser.dto.EDIDocumentSubmitResponseDTO;
import io.gainable.reactivexmlparser.dto.EdiDocumentDTO;

import java.util.List;

public interface ObjectStorageService {
    EDIDocumentSubmitResponseDTO submit(EdiDocumentDTO ediDocumentDTO);

    boolean linkAttachments(List<EDIDocumentSubmitResponseDTO> buffer);
}
