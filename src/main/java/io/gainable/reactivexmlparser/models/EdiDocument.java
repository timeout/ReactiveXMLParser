package io.gainable.reactivexmlparser.models;

import java.util.Map;

public sealed interface EdiDocument permits UploadDocument, Attachment {
    Map<String, String> properties();
}

