package de.schwarz.ecm.imoprters.apps.reactivexmlparser.dto;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record AttachmentDTO(
        Map<String, String> metadata,
        Map<String, String> uploadProperties,
        Map<String, String> contentProperties
) implements EdiDocumentDTO {
    @Override
    public Map<String, String> properties() {
        return Stream.concat(metadata.entrySet().stream(),
                        uploadProperties.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }
}
