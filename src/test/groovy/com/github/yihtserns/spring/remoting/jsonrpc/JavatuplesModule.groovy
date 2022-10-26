package com.github.yihtserns.spring.remoting.jsonrpc

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.javatuples.Quartet

class JavatuplesModule extends SimpleModule {

    JavatuplesModule() {
        addDeserializer(Quartet, new QuartetDeserializer())
        addSerializer(Quartet, new QuartetSerializer())
    }

    private static class QuartetDeserializer extends StdDeserializer<Quartet> implements ContextualDeserializer {

        private JavaType value1Type
        private JavaType value2Type
        private JavaType value3Type
        private JavaType value4Type

        protected QuartetDeserializer() {
            this(null, null, null, null)
        }

        QuartetDeserializer(JavaType value1Type, JavaType value2Type, JavaType value3Type, JavaType value4Type) {
            super(Quartet)
            this.value1Type = value1Type
            this.value2Type = value2Type
            this.value3Type = value3Type
            this.value4Type = value4Type
        }

        @Override
        Quartet deserialize(JsonParser parser, DeserializationContext context) {
            if (!parser.isExpectedStartArrayToken()) {
                return (Quartet) context.handleUnexpectedToken(Quartet, parser)
            }

            ArrayNode arrayNode = parser.readValueAsTree()
            if (arrayNode.size() != 4) {
                throw new InvalidFormatException(
                        parser,
                        "Expected a JSON array with 4 entries, but was: ${arrayNode}",
                        arrayNode,
                        Quartet)
            }

            return Quartet.with(
                    context.readTreeAsValue(arrayNode[0], value1Type),
                    context.readTreeAsValue(arrayNode[1], value2Type),
                    context.readTreeAsValue(arrayNode[2], value3Type),
                    context.readTreeAsValue(arrayNode[3], value4Type))
        }

        @Override
        JsonDeserializer<?> createContextual(DeserializationContext context, BeanProperty property) {
            def types = context.getContextualType().getBindings().getTypeParameters()

            return new QuartetDeserializer(
                    types[0],
                    types[1],
                    types[2],
                    types[3])
        }
    }

    private static class QuartetSerializer extends StdSerializer<Quartet> {

        protected QuartetSerializer() {
            super(Quartet)
        }

        @Override
        void serialize(Quartet quartet, JsonGenerator generator, SerializerProvider provider) {
            def list = [
                    quartet.value0,
                    quartet.value1,
                    quartet.value2,
                    quartet.value3
            ]
            def listSerializer = provider.findTypedValueSerializer(List, false, null)

            listSerializer.serialize(list, generator, provider)
        }
    }
}
