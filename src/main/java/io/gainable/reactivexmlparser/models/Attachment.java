package io.gainable.reactivexmlparser.models;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final record Attachment(
        Map<String, String> metadata,
        Map<String, String> uploadProperties,
        ByteDocument byteDocument
) implements EdiDocument {
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
