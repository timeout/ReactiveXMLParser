package io.gainable.reactivexmlparser.models;

import java.util.Arrays;
import java.util.Objects;

public record ByteDocument(String contentName, String mimeType, byte[] content) {
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ByteDocument other)) return false;
        return Objects.equals(contentName, other.contentName) &&
                Objects.equals(mimeType, other.mimeType) &&
                Arrays.equals(content, other.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contentName, mimeType, Arrays.hashCode(content));
    }

    @Override
    public String toString() {
        return "ByteDocument{" +
                "contentName='" + contentName + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", content=" + Arrays.toString(content) +
                '}';
    }
}
