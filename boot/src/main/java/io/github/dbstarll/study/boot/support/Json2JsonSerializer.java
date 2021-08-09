package io.github.dbstarll.study.boot.support;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.impl.UnknownSerializer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

@JsonComponent
public class Json2JsonSerializer {
    public static class JsonObjectSerializer extends JsonSerializer<JSONObject> {
        @Override
        public void serialize(JSONObject value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException, JsonProcessingException {
            if (value == null) {
                gen.writeNull();
            } else {
                gen.writeStartObject();
                for (Object key : value.keySet()) {
                    final Object val = value.get(key.toString());
                    if (val != null) {
                        final JsonSerializer<Object> serializer = serializers.findValueSerializer(val.getClass());
                        if (serializer != null && !UnknownSerializer.class.isInstance(serializer)) {
                            gen.writeFieldName(key.toString());
                            serializer.serialize(val, gen, serializers);
                        } else {
                            throw new JsonGenerationException("unknown serializer: " + val.getClass() + " for " + key, gen);
                        }
                    }
                }
                gen.writeEndObject();
            }
        }
    }

    public static class JsonArraySerializer extends JsonSerializer<JSONArray> {
        @Override
        public void serialize(JSONArray value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException, JsonProcessingException {
            if (value == null) {
                gen.writeNull();
            } else {
                gen.writeStartArray();
                for (int i = 0, size = value.length(); i < size; i++) {
                    final Object val = value.get(i);
                    if (val != null) {
                        final JsonSerializer<Object> serializer = serializers.findValueSerializer(val.getClass());
                        if (serializer != null && !UnknownSerializer.class.isInstance(serializer)) {
                            serializer.serialize(val, gen, serializers);
                        } else {
                            throw new JsonGenerationException("unknown serializer: " + val.getClass(), gen);
                        }
                    } else {
                        gen.writeNull();
                    }
                }
                gen.writeEndArray();
            }
        }
    }

    public static class JsonObjectDeserializer extends JsonDeserializer<JSONObject> {
        @Override
        public JSONObject deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            return parseJsonObject(p, ctxt);
        }
    }

    public static class JsonArrayDeserializer extends JsonDeserializer<JSONArray> {
        @Override
        public JSONArray deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            return parseJsonArray(p, ctxt);
        }
    }

    private static JSONObject parseJsonObject(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        if (p.currentToken() == JsonToken.START_OBJECT) {
            final JSONObject json = new JSONObject();

            JsonToken token;
            while ((token = p.nextToken()) != JsonToken.END_OBJECT) {
                if (token == JsonToken.FIELD_NAME) {
                    final String name = p.getCurrentName();
                    token = p.nextToken();

                    switch (token) {
                        case START_OBJECT:
                            json.put(name, parseJsonObject(p, ctxt));
                            break;
                        case START_ARRAY:
                            json.put(name, parseJsonArray(p, ctxt));
                            break;
                        case VALUE_STRING:
                            json.put(name, p.getValueAsString());
                            break;
                        case VALUE_NUMBER_INT:
                            json.put(name, p.getValueAsLong());
                            break;
                        case VALUE_TRUE:
                        case VALUE_FALSE:
                            json.put(name, p.getBooleanValue());
                            break;
                        case VALUE_NUMBER_FLOAT:
                            json.put(name, p.getDoubleValue());
                            break;
                        case VALUE_NULL:
                            break;
                        default:
                            throw new JsonParseException(p, "invalid token: " + token + " for " + name);
                    }
                } else {
                    throw new JsonParseException(p, "not FIELD_NAME");
                }
            }

            return json;
        } else {
            throw new JsonParseException(p, "not START_OBJECT");
        }
    }

    private static JSONArray parseJsonArray(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        if (p.currentToken() == JsonToken.START_ARRAY) {
            final JSONArray json = new JSONArray();

            JsonToken token;
            while ((token = p.nextToken()) != JsonToken.END_ARRAY) {
                switch (token) {
                    case START_OBJECT:
                        json.put(parseJsonObject(p, ctxt));
                        break;
                    case START_ARRAY:
                        json.put(parseJsonArray(p, ctxt));
                        break;
                    case VALUE_STRING:
                        json.put(p.getValueAsString());
                        break;
                    case VALUE_NUMBER_INT:
                        json.put(p.getValueAsLong());
                        break;
                    case VALUE_TRUE:
                    case VALUE_FALSE:
                        json.put(p.getBooleanValue());
                        break;
                    case VALUE_NUMBER_FLOAT:
                        json.put(p.getDoubleValue());
                        break;
                    case VALUE_NULL:
                        break;
                    default:
                        throw new JsonParseException(p, "invalid token: " + token);
                }
            }

            return json;
        } else {
            throw new JsonParseException(p, "not START_ARRAY");
        }
    }
}
