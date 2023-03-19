package io.gainable.reactivexmlparser.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "translation")
public class TranslationProperties {

    private Map<String, String> attachment;
    private Map<String, String> field;
    private Map<String, String> document;
    private Map<String, String> metadata;

    // Getters and setters

    // Attachment
    public Map<String, String> getAttachment() {
        return attachment;
    }

    public void setAttachment(Map<String, String> attachment) {
        this.attachment = attachment;
    }

    // Field
    public Map<String, String> getField() {
        return field;
    }

    public void setField(Map<String, String> field) {
        this.field = field;
    }

    // Document
    public Map<String, String> getDocument() {
        return document;
    }

    public void setDocument(Map<String, String> document) {
        this.document = document;
    }

    // Metadata
    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}

