package de.schwarz.ecm.imoprters.apps.reactivexmlparser.service.impl;

import de.schwarz.ecm.imoprters.apps.reactivexmlparser.dto.EDIDocumentSubmitResponseDTO;
import de.schwarz.ecm.imoprters.apps.reactivexmlparser.service.interfaces.ObjectStorageService;
import de.schwarz.ecm.imoprters.apps.reactivexmlparser.dto.EdiDocumentDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ObjectStorageServiceImpl implements ObjectStorageService {
    @Override
    public EDIDocumentSubmitResponseDTO submit(EdiDocumentDTO ediDocumentDTO) {
        return null;
    }

    @Override
    public boolean linkAttachments(List<EDIDocumentSubmitResponseDTO> buffer) {
        return false;
    }
}
