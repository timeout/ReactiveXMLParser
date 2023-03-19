package io.gainable.reactivexmlparser.service.impl;

import io.gainable.reactivexmlparser.dto.EDIDocumentSubmitResponseDTO;
import io.gainable.reactivexmlparser.dto.EdiDocumentDTO;
import io.gainable.reactivexmlparser.service.interfaces.ObjectStorageService;
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
