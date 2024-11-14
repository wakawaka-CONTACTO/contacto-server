package org.kiru.user.user.dto.request;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PurposeTypeDeserializer extends JsonDeserializer<List<Integer>> {
    @Override
    public List<Integer> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        List<Integer> purposes = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode element : node) {
                purposes.add(element.asInt());
            }
        }
        return purposes;
    }
}