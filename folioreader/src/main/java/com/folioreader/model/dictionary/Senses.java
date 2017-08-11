package com.folioreader.model.dictionary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author gautam chibde on 4/7/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Senses {
    @JsonProperty
    @JsonDeserialize(using = DefinitionDeserializer.class)
    private String[] definition;
    @JsonProperty
    private List<Example> examples;

    @Override
    public String toString() {
        return "Senses{" +
                "definition=" + Arrays.toString(definition) +
                ", examples=" + examples +
                '}';
    }

    public String[] getDefinition() {
        return definition;
    }

    public void setDefinition(String[] definition) {
        this.definition = definition;
    }

    public List<Example> getExamples() {
        return examples;
    }

    public void setExamples(List<Example> examples) {
        this.examples = examples;
    }

    public static class DefinitionDeserializer extends StdDeserializer<String[]> {

        public DefinitionDeserializer() {
            super(String[].class);
        }

        protected DefinitionDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public String[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            JsonNode node = p.getCodec().readTree(p);
            List<String> strings = new ArrayList<>();
            ObjectCodec oc = p.getCodec();
            if (node.isArray()) {
                for (JsonNode n : node) {
                    strings.add(oc.treeToValue(n, String.class));
                }
            } else {
                strings.add(oc.treeToValue(node, String.class));
            }
            return strings.toArray(new String[0]);
        }
    }
}
