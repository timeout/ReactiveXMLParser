package de.schwarz.ecm.imoprters.apps.reactivexmlparser.dto;

import java.util.Map;

public sealed interface EdiDocumentDTO permits UploadDocumentDTO, AttachmentDTO {
    Map<String, String> properties();
}

