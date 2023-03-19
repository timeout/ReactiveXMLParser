package io.gainable.reactivexmlparser.service.impl;

import io.gainable.reactivexmlparser.configuration.TranslationProperties;
import io.gainable.reactivexmlparser.dto.AttachmentDTO;
import io.gainable.reactivexmlparser.dto.EdiDocumentDTO;
import io.gainable.reactivexmlparser.dto.UploadDocumentDTO;
import io.gainable.reactivexmlparser.service.interfaces.EdiIntXMLParsingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

@Slf4j
@Service
public class EdiIntXMLParsingServiceImpl implements EdiIntXMLParsingService {

    private TranslationProperties translationProperties;

    public EdiIntXMLParsingServiceImpl(TranslationProperties translationProperties) {
        this.translationProperties = translationProperties;
    }

    public Flux<EdiDocumentDTO> parseEdiIntXMLAsString(String filePath) {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        Supplier<InputStream> inputStreamSupplier = () -> {
            try {
                return new FileInputStream(filePath);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Unable to open file input stream", e);
            }
        };

        return Flux.using(
                () -> {
                    try {
                        InputStream inputStream = inputStreamSupplier.get();
                        XMLStreamReader xmlStreamReader = inputFactory.createXMLStreamReader(inputStream);
                        return new EdiDocumentIterator(xmlStreamReader, inputStream, translationProperties);
                    } catch (XMLStreamException e) {
                        throw new RuntimeException("Unable to create XMLStreamReader", e);
                    }
                },
                iterator -> Flux.generate(sink -> {
                    if (iterator.hasNext()) {
                        sink.next(iterator.next());
                    } else {
                        sink.complete();
                    }
                }),
                EdiDocumentIterator::close
        );
    }

    private static class EdiDocumentIterator implements Iterator<EdiDocumentDTO>, AutoCloseable {
        private final ParsingContext context;
        private final InputStream inputStream;

        private final TranslationProperties translationProperties;

        public EdiDocumentIterator(
                XMLStreamReader xmlStreamReader,
                InputStream inputStream,
                TranslationProperties translationProperties
        ) {
            this.context = new ParsingContext(xmlStreamReader);
            this.inputStream = inputStream;
            this.translationProperties = translationProperties;
        }

        private boolean hasNextDocument() {
            if (context.ediDocumentDTO != null) {
                return true;
            }

            try {
                while (context.xmlStreamReader.hasNext()) {
                    processEvent(context, translationProperties);

                    if (context.ediDocumentDTO != null) {
                        return true;
                    }
                }
            } catch (XMLStreamException e) {
                throw new RuntimeException(e);
            }

            return false;
        }

        private void processEvent(ParsingContext context, TranslationProperties translationProperties) throws XMLStreamException {
            int eventType = context.xmlStreamReader.next();
            EventProcessor eventProcessor = new EventProcessor(context, translationProperties);

            switch (eventType) {
                case XMLStreamReader.START_ELEMENT:
                    eventProcessor.handleStartElement();
                    break;
                case XMLStreamReader.CHARACTERS:
                    eventProcessor.handleCharacters();
                    break;
                case XMLStreamReader.END_ELEMENT:
                    eventProcessor.handleEndElement();
                    break;
            }
        }

        @Override
        public boolean hasNext() {
            return hasNextDocument();
        }

        @Override
        public EdiDocumentDTO next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            EdiDocumentDTO result = context.ediDocumentDTO;
            context.ediDocumentDTO = null;
            return result;
        }

        @Override
        public void close() {
            closeXmlStreamReader(context.xmlStreamReader);
            closeInputStream(inputStream);
        }

        private void closeXmlStreamReader(XMLStreamReader xmlStreamReader) {
            if (xmlStreamReader != null) {
                try {
                    xmlStreamReader.close();
                } catch (XMLStreamException e) {
                    // Log the exception or handle it according to your application's requirements
                }
            }
        }

        private void closeInputStream(InputStream inputStream) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // Log the exception or handle it according to your application's requirements
                }
            }
        }
    }

    private static class EventProcessor {
        private final ParsingContext context;
        private final Map<String, String> metadataTranslations;
        private final Map<String, String> documentTranslations;
        private final Map<String, String> fieldTranslations;
        private final Map<String, String> attachmentTranslations;

        public EventProcessor(
                ParsingContext context,
                TranslationProperties translationProperties
        ) {
            this.context = context;
            this.metadataTranslations = translationProperties.getMetadata();
            this.documentTranslations = translationProperties.getDocument();
            this.fieldTranslations = translationProperties.getField();
            this.attachmentTranslations = translationProperties.getAttachment();
        }

        private void handleStartElement() {
            context.currentElement = context.xmlStreamReader.getLocalName();

            switch (context.currentElement) {
                case "metaData" -> context.metadata = new HashMap<>();
                case "DocumentUploadSummary" -> context.uploadProperties = new HashMap<>();
                case "Field" -> context.fieldName = context.fieldValue = "";
                case "Attachment" -> context.contentProperties = new HashMap<>();
            }
        }


        private void handleCharacters() {
            if (context.metadata != null && !context.xmlStreamReader.isWhiteSpace()) {
                String text = context.xmlStreamReader.getText().trim();

                if (metadataTranslations.containsKey(context.currentElement)) {
                    context.metadata.put(
                            metadataTranslations.get(context.currentElement),
                            text
                    );
                }
            }

            if (context.uploadProperties != null && !context.xmlStreamReader.isWhiteSpace()) {
                String text = context.xmlStreamReader.getText().trim();

                if (documentTranslations.containsKey(context.currentElement)) {
                    context.uploadProperties.put(
                            documentTranslations.get(context.currentElement),
                            text
                    );
                }
            }

            if (context.uploadProperties != null && context.fieldName != null && !context.xmlStreamReader.isWhiteSpace()) {
                String text = context.xmlStreamReader.getText().trim();

                switch (context.currentElement) {
                    case "Name" -> context.fieldName = text;
                    case "Value" -> context.fieldValue = text;
                }
            }

            if (context.uploadProperties != null && context.contentProperties != null && !context.xmlStreamReader.isWhiteSpace()) {
                String text = context.xmlStreamReader.getText().trim();

                if ("ByteContent".equals(context.currentElement) && (context.contentProperties.containsKey(attachmentTranslations.get("ByteContent")))) {
                    text = context.contentProperties.get(attachmentTranslations.get("ByteContent")) + text;
                }

                if (attachmentTranslations.containsKey(context.currentElement)) {
                    context.contentProperties.put(
                            attachmentTranslations.get(context.currentElement),
                            text
                    );
                }
            }
        }

        private void handleEndElement() {
            context.currentElement = context.xmlStreamReader.getLocalName();

            if ("EDIArchiveMessage".equals(context.currentElement)) {
                context.metadata = null;
            }

            if ("Attachment".equals(context.currentElement) && context.contentProperties != null && context.uploadProperties != null) {
                final var withoutWhitespace =
                        context.contentProperties.get("byteContent").replaceAll("\\s", "");
                context.contentProperties.put("byteContent", withoutWhitespace);
                context.ediDocumentDTO = new AttachmentDTO(
                        context.metadata,
                        context.uploadProperties,
                        context.contentProperties
                );
                context.contentProperties = null;
            }

            if ("Field".equals(context.currentElement) && context.fieldValue != null && context.fieldName != null && context.uploadProperties != null) {
                context.uploadProperties.put(
                        fieldTranslations.get(context.fieldName),
                        context.fieldValue
                );
                context.fieldName = null;
                context.fieldValue = null;
            }

            if ("DocumentUploadSummary".equals(context.currentElement) && context.metadata != null && context.uploadProperties != null) {
                context.ediDocumentDTO = new UploadDocumentDTO(context.metadata, context.uploadProperties);
                context.uploadProperties = null;
            }
        }
    }

    private static class ParsingContext {
        XMLStreamReader xmlStreamReader;
        Map<String, String> metadata;
        Map<String, String> uploadProperties;
        Map<String, String> contentProperties;
        String fieldName;
        String fieldValue;
        String currentElement;
        EdiDocumentDTO ediDocumentDTO;

        public ParsingContext(XMLStreamReader xmlStreamReader) {
            this.xmlStreamReader = xmlStreamReader;
        }
    }

}
