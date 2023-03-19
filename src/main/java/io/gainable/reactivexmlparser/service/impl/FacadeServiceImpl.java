package io.gainable.reactivexmlparser.service.impl;

import io.gainable.reactivexmlparser.dto.UploadDocumentSubmitReponseDTO;
import io.gainable.reactivexmlparser.service.interfaces.EdiIntXMLParsingService;
import io.gainable.reactivexmlparser.service.interfaces.FacadeService;
import io.gainable.reactivexmlparser.service.interfaces.ObjectStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FacadeServiceImpl implements FacadeService {

    private final EdiIntXMLParsingService ediIntXMLParsingService;
    private final ObjectStorageService objectStorageService;

    public FacadeServiceImpl(
            EdiIntXMLParsingService ediIntXMLParsingService,
            ObjectStorageService objectStorageService
    ) {
        this.ediIntXMLParsingService = ediIntXMLParsingService;
        this.objectStorageService = objectStorageService;
    }

    @Override
    public void processAndSubmitEDIArchiveMessage(String filePath) {
        ediIntXMLParsingService.parseEdiIntXMLAsString(filePath)
                .map(objectStorageService::submit)
                .bufferUntil(UploadDocumentSubmitReponseDTO.class::isInstance)
                .filter(buffer -> !buffer.isEmpty())
                .map(objectStorageService::linkAttachments)
                .reduce(true, (acc, result) -> acc && result)
                .subscribe(success -> {
                    if (success) {
                        // delete xml
                        log.info("{} deleted", filePath);
                    } else {
                        // move xml to error directory / queue
                        log.warn("{} could not be archived.", filePath);
                    }
                });
    }
}
