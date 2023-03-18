package io.gainable.reactivexmlparser.models;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record UploadDocument(Map<String, String> metadata,
                             Map<String, String> uploadProperties) implements EdiDocument {
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
