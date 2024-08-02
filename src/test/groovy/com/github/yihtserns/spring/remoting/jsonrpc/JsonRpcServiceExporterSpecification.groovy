package com.github.yihtserns.spring.remoting.jsonrpc

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.NullNode
import groovy.transform.ToString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import spock.lang.Shared
import spock.lang.Specification

import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

import static com.fasterxml.jackson.annotation.JsonInclude.Include
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JsonRpcServiceExporterSpecification extends Specification {

    @LocalServerPort
    private int port

    @Autowired
    private TestRestTemplate restTemplate

    @Shared
    private OffsetDateTime dateTime = OffsetDateTime.now(ZoneOffset.UTC)

    def "can call using no params for method with 0-arg"() {
        when:
        String id = UUID.randomUUID()
        def response = callCalc(new Request(
                id: id,
                method: "returnInt",
                params: nullValue))

        then:
        response.id == id
        response.error == null
        response.result == 999

        where:
        nullValue << [
                null,
                NullNode.instance
        ]
    }

    def "should fail when params is sent for method with 0-arg"() {
        when:
        String id = UUID.randomUUID()
        def response = callCalc(new Request(
                id: id,
                method: "returnInt",
                params: invalidParams))

        then:
        response.id == id
        response.result == null
        response.error.code == -32602
        response.error.message == "Invalid params"

        where:
        invalidParams << [
                [3],
                [value: 3]
        ]
    }

    def "can call using params of int array"() {
        when:
        String id = UUID.randomUUID()
        def response = callCalc(new Request(
                id: id,
                method: "subtractArray",
                params: [10, 3]))

        then:
        response.id == id
        response.error == null
        response.result == 10 - 3
    }

    def "should fail when params array size is not the same as the method parameters count"() {
        when:
        String id = UUID.randomUUID()
        def response = callCalc(new Request(id: id, method: "subtractArray", params: params))

        then:
        response.id == id
        response.result == null
        response.error.code == -32602
        response.error.message == "Invalid params"

        where:
        params << [
                [10, 3, 1],
                [10],
                []
        ]
    }

    def "can call using params of object"() {
        when:
        String id = UUID.randomUUID()
        def response = callCalc(new Request(
                id: id,
                method: "subtractObject",
                params: [firstValue: 10, secondValue: 3]))

        then:
        response.id == id
        response.error == null
        response.result == 10 - 3
    }

    def "should fail when object params is sent to method with more than 1 parameter"() {
        when:
        String id = UUID.randomUUID()
        def response = callCalc(new Request(
                id: id,
                method: "subtractObjectTwoParams",
                params: [firstValue: 10, secondValue: 3]))

        then:
        response.id == id
        response.result == null
        response.error.code == -32602
        response.error.message == "Invalid params"
    }

    def "supported array params data types"() {
        when:
        def response = callCalc(new Request(
                id: UUID.randomUUID(),
                method: method,
                params: [paramValue]))

        then:
        response.error == null
        response.result == expectedReturnValue

        where:
        method                              | paramValue                                      | expectedReturnValue
        "returnStringArg"                   | "Expected Param Value"                          | paramValue
        "returnStringArrayArg"              | ["Expected Param Value"]                        | paramValue
        "returnStringListArg"               | ["Expected Param Value"]                        | paramValue
        "returnStringSetArg"                | ["Expected Param Value"]                        | paramValue
        "returnStringCollectionArg"         | ["Expected Param Value"]                        | paramValue

        "returnBooleanArg"                  | true                                            | paramValue
        "returnBooleanWrapperArg"           | true                                            | paramValue
        "returnBooleanArrayArg"             | [true, false, true]                             | paramValue
        "returnBooleanWrapperArrayArg"      | [true, false, true]                             | paramValue
        "returnBooleanListArg"              | [true, false, true]                             | paramValue
        "returnBooleanSetArg"               | [true, false, true]                             | ([true, false] as HashSet).toList()
        "returnBooleanCollectionArg"        | [true, false, true]                             | paramValue

        "returnDoubleArg"                   | 1.234                                           | paramValue
        "returnDoubleWrapperArg"            | 1.234                                           | paramValue
        "returnDoubleArrayArg"              | [1.234]                                         | paramValue
        "returnDoubleWrapperArrayArg"       | [1.234]                                         | paramValue
        "returnDoubleListArg"               | [1.234]                                         | paramValue
        "returnDoubleSetArg"                | [1.234]                                         | paramValue
        "returnDoubleCollectionArg"         | [1.234]                                         | paramValue

        "returnFloatArg"                    | 1.234                                           | paramValue
        "returnFloatWrapperArg"             | 1.234                                           | paramValue
        "returnFloatArrayArg"               | [1.234]                                         | paramValue
        "returnFloatWrapperArrayArg"        | [1.234]                                         | paramValue
        "returnFloatListArg"                | [1.234]                                         | paramValue
        "returnFloatSetArg"                 | [1.234]                                         | paramValue
        "returnFloatCollectionArg"          | [1.234]                                         | paramValue

        "returnBigDecimalArg"               | 1.234                                           | paramValue
        "returnBigDecimalArrayArg"          | [1.234]                                         | paramValue
        "returnBigDecimalListArg"           | [1.234]                                         | paramValue
        "returnBigDecimalSetArg"            | [1.234]                                         | paramValue
        "returnBigDecimalCollectionArg"     | [1.234]                                         | paramValue

        "returnEnumArg"                     | TimeUnit.MINUTES.name()                         | paramValue
        "returnEnumArrayArg"                | [TimeUnit.MINUTES.name()]                       | paramValue
        "returnEnumListArg"                 | [TimeUnit.MINUTES.name()]                       | paramValue
        "returnEnumSetArg"                  | [TimeUnit.MINUTES.name()]                       | paramValue
        "returnEnumCollectionArg"           | [TimeUnit.MINUTES.name()]                       | paramValue

        // Supported by SpringBoot's ObjectMapper
        "returnOffsetDateTimeArg"           | dateTime.format(ISO_DATE_TIME)                  | paramValue
        "returnOffsetDateTimeArrayArg"      | [dateTime.format(ISO_DATE_TIME)]                | paramValue
        "returnOffsetDateTimeListArg"       | [dateTime.format(ISO_DATE_TIME)]                | paramValue
        "returnOffsetDateTimeSetArg"        | [dateTime.format(ISO_DATE_TIME)]                | paramValue
        "returnOffsetDateTimeCollectionArg" | [dateTime.format(ISO_DATE_TIME)]                | paramValue

        // Supported by custom de/serializer
        "returnTupleArg"                    | [1, "2", 3.3, dateTime.format(ISO_DATE_TIME)]   | paramValue
        "returnTupleArrayArg"               | [[1, "2", 3.3, dateTime.format(ISO_DATE_TIME)]] | paramValue
        "returnTupleListArg"                | [[1, "2", 3.3, dateTime.format(ISO_DATE_TIME)]] | paramValue
        "returnTupleSetArg"                 | [[1, "2", 3.3, dateTime.format(ISO_DATE_TIME)]] | paramValue
        "returnTupleCollectionArg"          | [[1, "2", 3.3, dateTime.format(ISO_DATE_TIME)]] | paramValue

        "returnMapArg"                      | ["Key 1": 1, "Key 2": 2]                        | paramValue
        "returnMapArrayArg"                 | [["Key 1": 1, "Key 2": 2], ["Key 3": 3]]        | paramValue
        "returnMapListArg"                  | [["Key 1": 1, "Key 2": 2], ["Key 3": 3]]        | paramValue
        "returnMapSetArg"                   | [["Key 1": 1, "Key 2": 2], ["Key 3": 3]]        | (paramValue as HashSet).toList()
        "returnMapCollectionArg"            | [["Key 1": 1, "Key 2": 2], ["Key 3": 3]]        | paramValue
        "returnValueObjectArg"              | 5                                               | paramValue
    }

    def "supported object params data types, via Jackson JSON"() {
        when:
        def response = callCalc(new Request(
                id: UUID.randomUUID(),
                method: "returnObjectArg",
                params: [(paramName): paramValue]))

        then:
        response.error == null
        response.result == [
                booleanValue: false,
                doubleValue : 0.0,
                floatValue  : 0.0,
                (paramName) : expectedReturnValue
        ]

        where:
        paramName                       | paramValue                                      | expectedReturnValue
        "stringValue"                   | "Expected Param Value"                          | paramValue
        "stringArrayValue"              | ["Expected Param Value"]                        | paramValue
        "stringListValue"               | ["Expected Param Value"]                        | paramValue
        "stringSetValue"                | ["Expected Param Value"]                        | paramValue
        "stringCollectionValue"         | ["Expected Param Value"]                        | paramValue

        "booleanValue"                  | true                                            | paramValue
        "booleanWrapperValue"           | true                                            | paramValue
        "booleanArrayValue"             | [true, false, true]                             | paramValue
        "booleanWrapperArrayValue"      | [true, false, true]                             | paramValue
        "booleanListValue"              | [true, false, true]                             | paramValue
        "booleanSetValue"               | [true, false, true]                             | ([true, false] as HashSet).toList()
        "booleanCollectionValue"        | [true, false, true]                             | paramValue

        "doubleValue"                   | 1.234                                           | paramValue
        "doubleWrapperValue"            | 1.234                                           | paramValue
        "doubleArrayValue"              | [1.234]                                         | paramValue
        "doubleWrapperArrayValue"       | [1.234]                                         | paramValue
        "doubleListValue"               | [1.234]                                         | paramValue
        "doubleSetValue"                | [1.234]                                         | paramValue
        "doubleCollectionValue"         | [1.234]                                         | paramValue

        "floatValue"                    | 1.234                                           | paramValue
        "floatWrapperValue"             | 1.234                                           | paramValue
        "floatArrayValue"               | [1.234]                                         | paramValue
        "floatWrapperArrayValue"        | [1.234]                                         | paramValue
        "floatListValue"                | [1.234]                                         | paramValue
        "floatSetValue"                 | [1.234]                                         | paramValue
        "floatCollectionValue"          | [1.234]                                         | paramValue

        "bigDecimalValue"               | 1.234                                           | paramValue
        "bigDecimalArrayValue"          | [1.234]                                         | paramValue
        "bigDecimalListValue"           | [1.234]                                         | paramValue
        "bigDecimalSetValue"            | [1.234]                                         | paramValue
        "bigDecimalCollectionValue"     | [1.234]                                         | paramValue

        "enumValue"                     | TimeUnit.MINUTES.name()                         | paramValue
        "enumArrayValue"                | [TimeUnit.MINUTES.name()]                       | paramValue
        "enumListValue"                 | [TimeUnit.MINUTES.name()]                       | paramValue
        "enumSetValue"                  | [TimeUnit.MINUTES.name()]                       | paramValue
        "enumCollectionValue"           | [TimeUnit.MINUTES.name()]                       | paramValue

        // Supported by SpringBoot's ObjectMapper
        "offsetDateTimeValue"           | dateTime.format(ISO_DATE_TIME)                  | paramValue
        "offsetDateTimeArrayValue"      | [dateTime.format(ISO_DATE_TIME)]                | paramValue
        "offsetDateTimeListValue"       | [dateTime.format(ISO_DATE_TIME)]                | paramValue
        "offsetDateTimeSetValue"        | [dateTime.format(ISO_DATE_TIME)]                | paramValue
        "offsetDateTimeCollectionValue" | [dateTime.format(ISO_DATE_TIME)]                | paramValue

        // Supported by custom de/serializer
        "tupleValue"                    | [1, "2", 3.3, dateTime.format(ISO_DATE_TIME)]   | paramValue
        "tupleArrayValue"               | [[1, "2", 3.3, dateTime.format(ISO_DATE_TIME)]] | paramValue
        "tupleListValue"                | [[1, "2", 3.3, dateTime.format(ISO_DATE_TIME)]] | paramValue
        "tupleSetValue"                 | [[1, "2", 3.3, dateTime.format(ISO_DATE_TIME)]] | paramValue
        "tupleCollectionValue"          | [[1, "2", 3.3, dateTime.format(ISO_DATE_TIME)]] | paramValue

        "mapValue"                      | ["Key 1": 1, "Key 2": 2]                        | paramValue
        "mapArrayValue"                 | [["Key 1": 1, "Key 2": 2], ["Key 3": 3]]        | paramValue
        "mapListValue"                  | [["Key 1": 1, "Key 2": 2], ["Key 3": 3]]        | paramValue
        "mapSetValue"                   | [["Key 1": 1, "Key 2": 2], ["Key 3": 3]]        | (paramValue as HashSet).toList()
        "mapCollectionValue"            | [["Key 1": 1, "Key 2": 2], ["Key 3": 3]]        | paramValue

        "objectValue"                   | [stringValue: "Expected Param Value"]           | paramValue + [booleanValue: false, doubleValue: 0.0, floatValue: 0.0]

        "value"                         | 5                                               | paramValue
    }

    def "supported empty object params"() {
        when:
        def response = callCalc(new Request(
                id: UUID.randomUUID(),
                method: "returnObjectArg",
                params: [:]))

        then:
        response.error == null
        response.result == [booleanValue: false, doubleValue: 0.0, floatValue: 0.0]
    }

    def "should fail with invalid params error when array params contains incompatible value"() {
        when:
        def response = callCalc(new Request(
                id: UUID.randomUUID(),
                method: method,
                params: [paramValue]))

        then:
        response.result == null
        response.error.code == -32602
        response.error.message == "Invalid params"

        where:
        method                              | paramValue
        "returnDoubleArg"                   | "abc"
        "returnDoubleWrapperArg"            | "abc"
        "returnDoubleArrayArg"              | ["abc"]
        "returnDoubleWrapperArrayArg"       | ["abc"]
        "returnDoubleListArg"               | ["abc"]
        "returnDoubleSetArg"                | ["abc"]
        "returnDoubleCollectionArg"         | ["abc"]

        "returnOffsetDateTimeArg"           | "abc"
        "returnOffsetDateTimeArrayArg"      | ["abc"]
        "returnOffsetDateTimeListArg"       | ["abc"]
        "returnOffsetDateTimeSetArg"        | ["abc"]
        "returnOffsetDateTimeCollectionArg" | ["abc"]
    }

    def "should fail with invalid params error when object params contains incorrect parameter name"() {
        when:
        def response = callCalc(new Request(
                id: UUID.randomUUID(),
                method: "returnObjectArg",
                params: ["nonExistentParam": 1]))

        then:
        response.result == null
        response.error.code == -32602
        response.error.message == "Invalid params"
    }

    def "should fail with internal error when method throws exception"() {
        when:
        def response = callCalc(new Request(
                id: UUID.randomUUID(),
                method: method,
                params: []))

        then:
        response.result == null
        response.error.code == -32603
        response.error.message == "Internal error"

        where:
        method << [
                "throwException",
                "throwError"
        ]
    }

    def "should fail with parse error when the request json is invalid"() {
        when:
        def response = requestCalc(invalidJson)

        then:
        response.body.result == null
        response.body.error.code == -32700
        response.body.error.message == "Parse error"

        where:
        invalidJson << [
                '{"jsonrpc": "2.0", "method": "foobar, "params": "bar", "baz]',
                '{"methodx": "foobar"}',
                '{"key": "value"}',
                '["1", "2"]',
                '[1, 2]',
                '',
                '     ',
                null
        ]
    }

    def "should fail with invalid request when the request is invalid"() {
        when:
        def response = requestCalc(invalidRequest)

        then:
        response.body.result == null
        response.body.error.code == -32600
        response.body.error.message == "Invalid Request"

        where:
        invalidRequest << [
                new Request(id: UUID.randomUUID(), method: "returnStringArg", params: "invalid params data type"),
                new Request(),
                "{}",
        ]
    }

    def "can use custom converter to transform a specific exception into error object"() {
        when:
        def responseWithCustomErrorObject = callCalc(new Request(
                id: UUID.randomUUID(),
                method: "throwCustomApplicationException",
                params: [errorCode]))

        then:
        responseWithCustomErrorObject.result == null
        responseWithCustomErrorObject.error.code == errorCode
        responseWithCustomErrorObject.error.message == "Custom Application Error"

        where:
        // -32000 to -32768 are reserved for pre-defined errors
        errorCode << [
                0,
                999,
                -32000 + 1,
                -32768 - 1
        ]
    }

    def "should return internal error if custom converter produces error object that uses reserved error code"() {
        when:
        def responseWithCustomErrorObject = callCalc(new Request(
                id: UUID.randomUUID(),
                method: "throwCustomApplicationException",
                params: [reservedErrorCode]))

        then:
        responseWithCustomErrorObject.result == null
        responseWithCustomErrorObject.error.code == -32603
        responseWithCustomErrorObject.error.message == "Internal error"

        where:
        reservedErrorCode | description
        -32000            | "Reserved for pre-defined errors (start)"

        -32700            | "Parse error"
        -32600            | "Invalid Request"
        -32601            | "Method not found"
        -32602            | "Invalid params"
        -32603            | "Internal error"

        -32000            | "Implementation-defined server errors (start)"
        -32099            | "Implementation-defined server errors (end)"

        -32768            | "Reserved for pre-defined errors (end)"
    }

    def "should not return anything if request does not have id"() {
        expect:
        with(requestCalc(new Request(method: method, params: params))) {
            statusCode == HttpStatus.NO_CONTENT
            body == null
        }

        where:
        method              | params
        "returnStringArg"   | ["not returned"]
        "returnStringArg"   | []
        "throwException"    | []
        "throwError"        | []
        "nonExistentMethod" | []
    }

    def "should return error when the request is incorrect, even if it does not have an id"() {
        expect:
        with(requestCalc('{"jsonrpc": "2.0", "method": "foobar, "params": "bar", "baz]')) {
            body.id == null
            body.error.code == -32700
            body.error.message == "Parse error"
        }
        with(requestCalc(new Request(method: "returnStringArg", params: "invalid params, neither list nor object"))) {
            body.id == null
            body.error.code == -32600
            body.error.message == "Invalid Request"
        }
    }

    def "does not support overloaded method"() {
        when:
        def exporter = new JsonRpcServiceExporter(
                serviceInterface: OverloadedMethodService,
                service: new OverloadedMethodServiceImpl())
        exporter.afterPropertiesSet()

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "Duplicate method name is not supported: overloaded"
    }

    def "should throw when service is not configured properly"() {
        given:
        def exporter = new JsonRpcServiceExporter()

        when:
        exporter.afterPropertiesSet()

        then:
        def missingServiceEx = thrown(IllegalArgumentException)
        missingServiceEx.message == "Property 'service' is required"

        when:
        exporter.service = new Object()
        exporter.afterPropertiesSet()

        then:
        def missingServiceInterfaceEx = thrown(IllegalArgumentException)
        missingServiceInterfaceEx.message == "Property 'serviceInterface' is required"

        when:
        exporter.serviceInterface = CalcService
        exporter.afterPropertiesSet()

        then:
        def implementationEx = thrown(IllegalArgumentException)
        implementationEx.message == "Service interface [${CalcService.name}]" +
                " needs to be implemented by service [${exporter.@service}]" +
                " of class [${exporter.@service.class.name}]"
    }

    private Map callCalc(Request request) {
        return requestCalc(request).body
    }

    private ResponseEntity<Map> requestCalc(Object request) {
        return restTemplate.exchange(
                "http://localhost:${port}/calc",
                HttpMethod.POST,
                new HttpEntity(request, new HttpHeaders(contentType: MediaType.APPLICATION_JSON)),
                Map)
    }

    @Configuration
    @EnableAutoConfiguration
    static class Application {

        @Bean("/calc")
        JsonRpcServiceExporter calcServiceJsonRpcServiceExporter(ObjectMapper objectMapper) {
            return new JsonRpcServiceExporter(
                    serviceInterface: CalcService,
                    service: calcService(),
                    objectMapper: objectMapper,
                    exceptionHandler: new CustomApplicationExceptionToError())
        }

        @Bean
        CalcService calcService() {
            return new CalcServiceImpl()
        }

        @Bean
        JavatuplesModule javatuplesModule() {
            return new JavatuplesModule()
        }
    }

    @ToString(includePackage = false, includeNames = true)
    static class Request {

        final String jsonrpc = "2.0"
        String id
        String method
        @JsonInclude(Include.NON_NULL)
        Object params
    }
}
