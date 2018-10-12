package com.etriphany.fulltext.domain.util;

import com.etriphany.fulltext.domain.embed.Content;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Content serializer for Jackson API.
 *
 * @author cadu.goncalves
 *
 */
public class ContentSerializer extends JsonSerializer<Content> {

    @Override
    public void serialize(Content content, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField(FieldNames.ID, content.getId());
        jsonGenerator.writeStringField(FieldNames.PATH, content.getPath());
        jsonGenerator.writeStringField(FieldNames.LANGUAGE, content.getLanguage());
        jsonGenerator.writeEndObject();
    }
}
