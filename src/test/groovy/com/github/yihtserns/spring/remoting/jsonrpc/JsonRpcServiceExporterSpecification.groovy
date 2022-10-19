package com.github.yihtserns.spring.remoting.jsonrpc


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
import spock.lang.Specification

import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JsonRpcServiceExporterSpecification extends Specification {

    @LocalServerPort
    private int port

    @Autowired
    private TestRestTemplate restTemplate

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
        method                              | paramValue                                   | expectedReturnValue
        "returnStringArg"                   | "Expected Param Value"                       | paramValue
        "returnStringArrayArg"              | ["Expected Param Value"]                     | paramValue
        "returnStringListArg"               | ["Expected Param Value"]                     | paramValue
        "returnStringSetArg"                | ["Expected Param Value"]                     | paramValue
        "returnStringCollectionArg"         | ["Expected Param Value"]                     | paramValue

        "returnBooleanArg"                  | true                                         | paramValue
        "returnBooleanWrapperArg"           | true                                         | paramValue
        "returnBooleanArrayArg"             | [true, false, true]                          | paramValue
        "returnBooleanWrapperArrayArg"      | [true, false, true]                          | paramValue
        "returnBooleanListArg"              | [true, false, true]                          | paramValue
        "returnBooleanSetArg"               | [true, false, true]                          | [true, false]
        "returnBooleanCollectionArg"        | [true, false, true]                          | paramValue

        "returnDoubleArg"                   | 1.234                                        | paramValue
        "returnDoubleWrapperArg"            | 1.234                                        | paramValue
        "returnDoubleArrayArg"              | [1.234]                                      | paramValue
        "returnDoubleWrapperArrayArg"       | [1.234]                                      | paramValue
        "returnDoubleListArg"               | [1.234]                                      | paramValue
        "returnDoubleSetArg"                | [1.234]                                      | paramValue
        "returnDoubleCollectionArg"         | [1.234]                                      | paramValue

        // String-to-X auto-conversion
        "returnDoubleArg"                   | "1.234"                                      | 1.234
        "returnDoubleWrapperArg"            | "1.234"                                      | 1.234
        "returnDoubleArrayArg"              | ["1.234"]                                    | [1.234]
        "returnDoubleWrapperArrayArg"       | ["1.234"]                                    | [1.234]
        "returnDoubleListArg"               | ["1.234"]                                    | [1.234]
        "returnDoubleSetArg"                | ["1.234"]                                    | [1.234]
        "returnDoubleCollectionArg"         | ["1.234"]                                    | [1.234]

        "returnFloatArg"                    | 1.234                                        | paramValue
        "returnFloatWrapperArg"             | 1.234                                        | paramValue
        "returnFloatArrayArg"               | [1.234]                                      | paramValue
        "returnFloatWrapperArrayArg"        | [1.234]                                      | paramValue
        "returnFloatListArg"                | [1.234]                                      | paramValue
        "returnFloatSetArg"                 | [1.234]                                      | paramValue
        "returnFloatCollectionArg"          | [1.234]                                      | paramValue

        "returnBigDecimalArg"               | 1.234                                        | paramValue
        "returnBigDecimalArrayArg"          | [1.234]                                      | paramValue
        "returnBigDecimalListArg"           | [1.234]                                      | paramValue
        "returnBigDecimalSetArg"            | [1.234]                                      | paramValue
        "returnBigDecimalCollectionArg"     | [1.234]                                      | paramValue

        "returnEnumArg"                     | TimeUnit.MINUTES.name()                      | paramValue
        "returnEnumArrayArg"                | [TimeUnit.MINUTES.name()]                    | paramValue
        "returnEnumListArg"                 | [TimeUnit.MINUTES.name()]                    | paramValue
        "returnEnumSetArg"                  | [TimeUnit.MINUTES.name()]                    | paramValue
        "returnEnumCollectionArg"           | [TimeUnit.MINUTES.name()]                    | paramValue

        "returnOffsetDateTimeArg"           | OffsetDateTime.now().format(ISO_DATE_TIME)   | paramValue
        "returnOffsetDateTimeArrayArg"      | [OffsetDateTime.now().format(ISO_DATE_TIME)] | paramValue
        "returnOffsetDateTimeListArg"       | [OffsetDateTime.now().format(ISO_DATE_TIME)] | paramValue
        "returnOffsetDateTimeSetArg"        | [OffsetDateTime.now().format(ISO_DATE_TIME)] | paramValue
        "returnOffsetDateTimeCollectionArg" | [OffsetDateTime.now().format(ISO_DATE_TIME)] | paramValue

        "returnMapArg"                      | ["Key 1": 1, "Key 2": 2]                     | paramValue
        "returnMapArrayArg"                 | [["Key 1": 1, "Key 2": 2], ["Key 3": 3]]     | paramValue
        "returnMapListArg"                  | [["Key 1": 1, "Key 2": 2], ["Key 3": 3]]     | paramValue
        "returnMapSetArg"                   | [["Key 1": 1, "Key 2": 2], ["Key 3": 3]]     | paramValue
        "returnMapCollectionArg"            | [["Key 1": 1, "Key 2": 2], ["Key 3": 3]]     | paramValue
    }

    def "supported object params data types"() {
        when:
        def response = callCalc(new Request(
                id: UUID.randomUUID(),
                method: "returnObjectArg",
                params: [(paramName): paramValue]))

        then:
        response.error == null
        response.result[paramName] == expectedReturnValue

        where:
        paramName                       | paramValue                                   | expectedReturnValue
        "stringValue"                   | "Expected Param Value"                       | paramValue
        "stringArrayValue"              | ["Expected Param Value"]                     | paramValue
        "stringListValue"               | ["Expected Param Value"]                     | paramValue
        "stringSetValue"                | ["Expected Param Value"]                     | paramValue
        "stringCollectionValue"         | ["Expected Param Value"]                     | paramValue

        "booleanValue"                  | true                                         | paramValue
        "booleanWrapperValue"           | true                                         | paramValue
        "booleanArrayValue"             | [true, false, true]                          | paramValue
        "booleanWrapperArrayValue"      | [true, false, true]                          | paramValue
        "booleanListValue"              | [true, false, true]                          | paramValue
        "booleanSetValue"               | [true, false, true]                          | [true, false]
        "booleanCollectionValue"        | [true, false, true]                          | paramValue

        "doubleValue"                   | 1.234                                        | paramValue
        "doubleWrapperValue"            | 1.234                                        | paramValue
        "doubleArrayValue"              | [1.234]                                      | paramValue
        "doubleWrapperArrayValue"       | [1.234]                                      | paramValue
        "doubleListValue"               | [1.234]                                      | paramValue
        "doubleSetValue"                | [1.234]                                      | paramValue
        "doubleCollectionValue"         | [1.234]                                      | paramValue

        "floatValue"                    | 1.234                                        | paramValue
        "floatWrapperValue"             | 1.234                                        | paramValue
        "floatArrayValue"               | [1.234]                                      | paramValue
        "floatWrapperArrayValue"        | [1.234]                                      | paramValue
        "floatListValue"                | [1.234]                                      | paramValue
        "floatSetValue"                 | [1.234]                                      | paramValue
        "floatCollectionValue"          | [1.234]                                      | paramValue

        "bigDecimalValue"               | 1.234                                        | paramValue
        "bigDecimalArrayValue"          | [1.234]                                      | paramValue
        "bigDecimalListValue"           | [1.234]                                      | paramValue
        "bigDecimalSetValue"            | [1.234]                                      | paramValue
        "bigDecimalCollectionValue"     | [1.234]                                      | paramValue

        "enumValue"                     | TimeUnit.MINUTES.name()                      | paramValue
        "enumArrayValue"                | [TimeUnit.MINUTES.name()]                    | paramValue
        "enumListValue"                 | [TimeUnit.MINUTES.name()]                    | paramValue
        "enumSetValue"                  | [TimeUnit.MINUTES.name()]                    | paramValue
        "enumCollectionValue"           | [TimeUnit.MINUTES.name()]                    | paramValue

        "offsetDateTimeValue"           | OffsetDateTime.now().format(ISO_DATE_TIME)   | paramValue
        "offsetDateTimeArrayValue"      | [OffsetDateTime.now().format(ISO_DATE_TIME)] | paramValue
        "offsetDateTimeListValue"       | [OffsetDateTime.now().format(ISO_DATE_TIME)] | paramValue
        "offsetDateTimeSetValue"        | [OffsetDateTime.now().format(ISO_DATE_TIME)] | paramValue
        "offsetDateTimeCollectionValue" | [OffsetDateTime.now().format(ISO_DATE_TIME)] | paramValue

        "mapValue"                      | ["Key 1": 1, "Key 2": 2]                     | paramValue
        "mapArrayValue"                 | [["Key 1": 1, "Key 2": 2], ["Key 3": 3]]     | paramValue
        "mapListValue"                  | [["Key 1": 1, "Key 2": 2], ["Key 3": 3]]     | paramValue
        "mapSetValue"                   | [["Key 1": 1, "Key 2": 2], ["Key 3": 3]]     | paramValue
        "mapCollectionValue"            | [["Key 1": 1, "Key 2": 2], ["Key 3": 3]]     | paramValue
    }

    def "supported empty object params"() {
        when:
        def response = callCalc(new Request(
                id: UUID.randomUUID(),
                method: "returnObjectArg",
                params: [:]))

        then:
        response.error == null
        response.result == new DataTypeObject().properties.tap { it.remove("class") }
    }

    def "unsupported object params data types"() {
        when:
        def response = callCalc(new Request(
                id: UUID.randomUUID(),
                method: "returnObjectArg",
                params: [(paramName): paramValue]))

        then:
        response.result == null
        response.error.code == -32602
        response.error.message == "Invalid params"

        where:
        paramName                 | paramValue
        "objectValue"             | [stringValue: "Expected Nested Param Value"]
        "objectValue.stringValue" | "Expected Nested Param Value"
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

        "returnOffsetDateTimeArg"           | "2020"
        "returnOffsetDateTimeArrayArg"      | ["2020"]
        "returnOffsetDateTimeListArg"       | ["2020"]
        "returnOffsetDateTimeSetArg"        | ["2020"]
        "returnOffsetDateTimeCollectionArg" | ["2020"]
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
        def response = requestCalc('{"jsonrpc": "2.0", "method": "foobar, "params": "bar", "baz]')

        then:
        response.body.result == null
        response.body.error.code == -32700
        response.body.error.message == "Parse error"
    }

    def "should fail with invalid request when params data type is incorrect"() {
        when:
        def response = callCalc(new Request(
                id: UUID.randomUUID(),
                method: "returnStringArg",
                params: "neither list nor object"))

        then:
        response.result == null
        response.error.code == -32600
        response.error.message == "Invalid Request"
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

    def "can use anonymous class converter to transform a specific exception into error object"() {
        when:
        def responseWithCustomErrorObject = callCalc(new Request(
                id: UUID.randomUUID(),
                method: "throwHandledIllegalArgumentException",
                params: []))

        then:
        responseWithCustomErrorObject.result == null
        responseWithCustomErrorObject.error.code == -1
        responseWithCustomErrorObject.error.message == "Handled"
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
        return restTemplate.postForObject(
                "http://localhost:${port}/calc",
                new HttpEntity(request, new HttpHeaders(contentType: MediaType.APPLICATION_JSON)),
                Map)
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
        JsonRpcServiceExporter calcServiceJsonRpcServiceExporter(Collection<ThrowableConverter> throwableConverters) {
            def exporter = new JsonRpcServiceExporter(
                    serviceInterface: CalcService,
                    service: calcService())

            throwableConverters.each { exporter.addThrowableConverter(it) }

            return exporter
        }

        @Bean
        CalcService calcService() {
            return new CalcServiceImpl()
        }

        @Bean
        CustomApplicationExceptionConverter customApplicationExceptionConverter() {
            return new CustomApplicationExceptionConverter()
        }

        @Bean
        ThrowableConverter<IllegalArgumentException> illegalArgumentExceptionConverter() {
            return new ThrowableConverter<IllegalArgumentException>() {
                @Override
                JsonRpcResponse.Error convert(IllegalArgumentException ex) {
                    return new JsonRpcResponse.Error(-1, ex.message)
                }
            }
        }
    }

    interface CalcService {

        int subtractArray(int firstValue, int secondValue)

        int subtractObject(SubtractObject bean)

        String returnStringArg(String value)

        String[] returnStringArrayArg(String[] value)

        List<String> returnStringListArg(List<String> value)

        Set<String> returnStringSetArg(Set<String> value)

        Collection<String> returnStringCollectionArg(Collection<String> value)

        boolean returnBooleanArg(boolean value)

        Boolean returnBooleanWrapperArg(Boolean value)

        boolean[] returnBooleanArrayArg(boolean[] value)

        Boolean[] returnBooleanWrapperArrayArg(Boolean[] value)

        List<Boolean> returnBooleanListArg(List<Boolean> value)

        Set<Boolean> returnBooleanSetArg(Set<Boolean> value)

        Collection<Boolean> returnBooleanCollectionArg(Collection<Boolean> value)

        double returnDoubleArg(double value)

        Double returnDoubleWrapperArg(Double value)

        double[] returnDoubleArrayArg(double[] value)

        Double[] returnDoubleWrapperArrayArg(Double[] value)

        List<Double> returnDoubleListArg(List<Double> value)

        Set<Double> returnDoubleSetArg(Set<Double> value)

        Collection<Double> returnDoubleCollectionArg(Collection<Double> value)

        float returnFloatArg(float value)

        Float returnFloatWrapperArg(Float value)

        float[] returnFloatArrayArg(float[] value)

        Float[] returnFloatWrapperArrayArg(Float[] value)

        List<Float> returnFloatListArg(List<Float> value)

        Set<Float> returnFloatSetArg(Set<Float> value)

        Collection<Float> returnFloatCollectionArg(Collection<Float> value)

        BigDecimal returnBigDecimalArg(BigDecimal value)

        BigDecimal[] returnBigDecimalArrayArg(BigDecimal[] value)

        List<BigDecimal> returnBigDecimalListArg(List<BigDecimal> value)

        Set<BigDecimal> returnBigDecimalSetArg(Set<BigDecimal> value)

        Collection<BigDecimal> returnBigDecimalCollectionArg(Collection<BigDecimal> value)

        TimeUnit returnEnumArg(TimeUnit value)

        TimeUnit[] returnEnumArrayArg(TimeUnit[] value)

        List<TimeUnit> returnEnumListArg(List<TimeUnit> value)

        Set<TimeUnit> returnEnumSetArg(Set<TimeUnit> value)

        Collection<TimeUnit> returnEnumCollectionArg(Collection<TimeUnit> value)

        OffsetDateTime returnOffsetDateTimeArg(OffsetDateTime value)

        OffsetDateTime[] returnOffsetDateTimeArrayArg(OffsetDateTime[] value)

        List<OffsetDateTime> returnOffsetDateTimeListArg(List<OffsetDateTime> value)

        Set<OffsetDateTime> returnOffsetDateTimeSetArg(Set<OffsetDateTime> value)

        Collection<OffsetDateTime> returnOffsetDateTimeCollectionArg(Collection<OffsetDateTime> value)

        Map<String, Integer> returnMapArg(Map<String, Integer> value)

        Map<String, Integer>[] returnMapArrayArg(Map<String, Integer>[] value)

        List<Map<String, Integer>> returnMapListArg(List<Map<String, Integer>> value)

        Set<Map<String, Integer>> returnMapSetArg(Set<Map<String, Integer>> value)

        Collection<Map<String, Integer>> returnMapCollectionArg(Collection<Map<String, Integer>> value)

        DataTypeObject returnObjectArg(DataTypeObject value)

        void throwException()

        void throwError()

        void throwCustomApplicationException(int errorCode) throws CustomApplicationException

        void throwHandledIllegalArgumentException()
    }

    static class CalcServiceImpl implements CalcService {

        @Override
        int subtractArray(int firstValue, int secondValue) {
            return firstValue - secondValue;
        }

        @Override
        int subtractObject(SubtractObject bean) {
            return bean.firstValue - bean.secondValue
        }

        @Override
        String returnStringArg(String value) {
            return value
        }

        @Override
        String[] returnStringArrayArg(String[] value) {
            return value
        }

        @Override
        List<String> returnStringListArg(List<String> value) {
            value.each { assert it instanceof String }
            return value
        }

        @Override
        Set<String> returnStringSetArg(Set<String> value) {
            value.each { assert it instanceof String }
            return value
        }

        @Override
        Collection<String> returnStringCollectionArg(Collection<String> value) {
            value.each { assert it instanceof String }
            return value
        }

        @Override
        boolean returnBooleanArg(boolean value) {
            return value
        }

        @Override
        Boolean returnBooleanWrapperArg(Boolean value) {
            return value
        }

        @Override
        boolean[] returnBooleanArrayArg(boolean[] value) {
            return value
        }

        @Override
        Boolean[] returnBooleanWrapperArrayArg(Boolean[] value) {
            value.each { assert it instanceof Boolean }
            return value
        }

        @Override
        List<Boolean> returnBooleanListArg(List<Boolean> value) {
            value.each { assert it instanceof Boolean }
            return value
        }

        @Override
        Set<Boolean> returnBooleanSetArg(Set<Boolean> value) {
            value.each { assert it instanceof Boolean }
            return value
        }

        @Override
        Collection<Boolean> returnBooleanCollectionArg(Collection<Boolean> value) {
            value.each { assert it instanceof Boolean }
            return value
        }

        @Override
        double returnDoubleArg(double value) {
            return value
        }

        @Override
        Double returnDoubleWrapperArg(Double value) {
            return value
        }

        @Override
        double[] returnDoubleArrayArg(double[] value) {
            return value
        }

        @Override
        Double[] returnDoubleWrapperArrayArg(Double[] value) {
            return value
        }

        @Override
        List<Double> returnDoubleListArg(List<Double> value) {
            value.each { assert it instanceof Double }
            return value
        }

        @Override
        Set<Double> returnDoubleSetArg(Set<Double> value) {
            value.each { assert it instanceof Double }
            return value
        }

        @Override
        Collection<Double> returnDoubleCollectionArg(Collection<Double> value) {
            value.each { assert it instanceof Double }
            return value
        }

        @Override
        float returnFloatArg(float value) {
            return value
        }

        @Override
        Float returnFloatWrapperArg(Float value) {
            return value
        }

        @Override
        float[] returnFloatArrayArg(float[] value) {
            return value
        }

        @Override
        Float[] returnFloatWrapperArrayArg(Float[] value) {
            return value
        }

        @Override
        List<Float> returnFloatListArg(List<Float> value) {
            value.each { assert it instanceof Float }
            return value
        }

        @Override
        Set<Float> returnFloatSetArg(Set<Float> value) {
            value.each { assert it instanceof Float }
            return value
        }

        @Override
        Collection<Float> returnFloatCollectionArg(Collection<Float> value) {
            value.each { assert it instanceof Float }
            return value
        }

        @Override
        BigDecimal returnBigDecimalArg(BigDecimal value) {
            return value
        }

        @Override
        BigDecimal[] returnBigDecimalArrayArg(BigDecimal[] value) {
            return value
        }

        @Override
        List<BigDecimal> returnBigDecimalListArg(List<BigDecimal> value) {
            value.each { assert it instanceof BigDecimal }
            return value
        }

        @Override
        Set<BigDecimal> returnBigDecimalSetArg(Set<BigDecimal> value) {
            value.each { assert it instanceof BigDecimal }
            return value
        }

        @Override
        Collection<BigDecimal> returnBigDecimalCollectionArg(Collection<BigDecimal> value) {
            value.each { assert it instanceof BigDecimal }
            return value
        }

        @Override
        TimeUnit returnEnumArg(TimeUnit value) {
            return value
        }

        @Override
        TimeUnit[] returnEnumArrayArg(TimeUnit[] value) {
            return value
        }

        @Override
        List<TimeUnit> returnEnumListArg(List<TimeUnit> value) {
            value.each { assert it instanceof TimeUnit }
            return value
        }

        @Override
        Set<TimeUnit> returnEnumSetArg(Set<TimeUnit> value) {
            value.each { assert it instanceof TimeUnit }
            return value
        }

        @Override
        Collection<TimeUnit> returnEnumCollectionArg(Collection<TimeUnit> value) {
            value.each { assert it instanceof TimeUnit }
            return value
        }

        @Override
        OffsetDateTime returnOffsetDateTimeArg(OffsetDateTime value) {
            return value
        }

        @Override
        OffsetDateTime[] returnOffsetDateTimeArrayArg(OffsetDateTime[] value) {
            return value
        }

        @Override
        List<OffsetDateTime> returnOffsetDateTimeListArg(List<OffsetDateTime> value) {
            value.each { assert it instanceof OffsetDateTime }
            return value
        }

        @Override
        Set<OffsetDateTime> returnOffsetDateTimeSetArg(Set<OffsetDateTime> value) {
            value.each { assert it instanceof OffsetDateTime }
            return value
        }

        @Override
        Collection<OffsetDateTime> returnOffsetDateTimeCollectionArg(Collection<OffsetDateTime> value) {
            value.each { assert it instanceof OffsetDateTime }
            return value
        }

        @Override
        Map<String, Integer> returnMapArg(Map<String, Integer> value) {
            value.each { k, v -> assert k instanceof String && v instanceof Integer }
            return value
        }

        @Override
        Map<String, Integer>[] returnMapArrayArg(Map<String, Integer>[] value) {
            value*.each { k, v -> assert k instanceof String && v instanceof Integer }
            return value
        }

        @Override
        List<Map<String, Integer>> returnMapListArg(List<Map<String, Integer>> value) {
            value*.each { k, v -> assert k instanceof String && v instanceof Integer }
            return value
        }

        @Override
        Set<Map<String, Integer>> returnMapSetArg(Set<Map<String, Integer>> value) {
            value*.each { k, v -> assert k instanceof String && v instanceof Integer }
            return value
        }

        @Override
        Collection<Map<String, Integer>> returnMapCollectionArg(Collection<Map<String, Integer>> value) {
            value*.each { k, v -> assert k instanceof String && v instanceof Integer }
            return value
        }

        @Override
        DataTypeObject returnObjectArg(DataTypeObject value) {
            value.stringListValue.each { assert it instanceof String }
            value.stringSetValue.each { assert it instanceof String }
            value.stringCollectionValue.each { assert it instanceof String }

            value.doubleListValue.each { assert it instanceof Double }
            value.doubleSetValue.each { assert it instanceof Double }
            value.doubleCollectionValue.each { assert it instanceof Double }

            value.floatListValue.each { assert it instanceof Float }
            value.floatSetValue.each { assert it instanceof Float }
            value.floatCollectionValue.each { assert it instanceof Float }

            value.bigDecimalListValue.each { assert it instanceof BigDecimal }
            value.bigDecimalSetValue.each { assert it instanceof BigDecimal }
            value.bigDecimalCollectionValue.each { assert it instanceof BigDecimal }

            value.enumListValue.each { assert it instanceof TimeUnit }
            value.enumSetValue.each { assert it instanceof TimeUnit }
            value.enumCollectionValue.each { assert it instanceof TimeUnit }

            value.mapValue.each { k, v -> assert k instanceof String && v instanceof Integer }
            value.mapArrayValue*.each { k, v -> assert k instanceof String && v instanceof Integer }
            value.mapListValue*.each { k, v -> assert k instanceof String && v instanceof Integer }
            value.mapSetValue*.each { k, v -> assert k instanceof String && v instanceof Integer }
            value.mapCollectionValue*.each { k, v -> assert k instanceof String && v instanceof Integer }

            return value
        }

        @Override
        void throwException() {
            throw new RuntimeException("Simulated Exception!")
        }

        @Override
        void throwError() {
            throw new Error("Simulated Error!")
        }

        @Override
        void throwCustomApplicationException(int errorCode) throws CustomApplicationException {
            throw new CustomApplicationException(errorCode)
        }

        @Override
        void throwHandledIllegalArgumentException() {
            throw new IllegalArgumentException("Handled")
        }
    }

    static class SubtractObject {

        int firstValue
        int secondValue
    }

    static class DataTypeObject {

        String stringValue
        String[] stringArrayValue
        List<String> stringListValue
        List<String> stringSetValue
        Collection<String> stringCollectionValue

        boolean booleanValue
        Boolean booleanWrapperValue
        boolean[] booleanArrayValue
        Boolean[] booleanWrapperArrayValue
        List<Boolean> booleanListValue
        Set<Boolean> booleanSetValue
        Collection<Boolean> booleanCollectionValue

        double doubleValue
        Double doubleWrapperValue
        double[] doubleArrayValue
        Double[] doubleWrapperArrayValue
        List<Double> doubleListValue
        Set<Double> doubleSetValue
        Collection<Double> doubleCollectionValue

        float floatValue
        Float floatWrapperValue
        float[] floatArrayValue
        Float[] floatWrapperArrayValue
        List<Float> floatListValue
        Set<Float> floatSetValue
        Collection<Float> floatCollectionValue

        BigDecimal bigDecimalValue
        BigDecimal[] bigDecimalArrayValue
        List<BigDecimal> bigDecimalListValue
        Set<BigDecimal> bigDecimalSetValue
        Collection<BigDecimal> bigDecimalCollectionValue

        TimeUnit enumValue
        TimeUnit[] enumArrayValue
        List<TimeUnit> enumListValue
        Set<TimeUnit> enumSetValue
        Collection<TimeUnit> enumCollectionValue

        OffsetDateTime offsetDateTimeValue
        OffsetDateTime[] offsetDateTimeArrayValue
        List<OffsetDateTime> offsetDateTimeListValue
        Set<OffsetDateTime> offsetDateTimeSetValue
        Collection<OffsetDateTime> offsetDateTimeCollectionValue

        Map<String, Integer> mapValue
        Map<String, Integer>[] mapArrayValue
        List<Map<String, Integer>> mapListValue
        Set<Map<String, Integer>> mapSetValue
        Collection<Map<String, Integer>> mapCollectionValue

        DataTypeObject objectValue
    }

    interface OverloadedMethodService {

        int overloaded(String value)

        int overloaded(int value)
    }

    class OverloadedMethodServiceImpl implements OverloadedMethodService {

        @Override
        int overloaded(String value) {
            return 0
        }

        @Override
        int overloaded(int value) {
            return 0
        }
    }

    static class CustomApplicationException extends Exception {

        int errorCode

        CustomApplicationException(int errorCode) {
            this.errorCode = errorCode
        }
    }

    static class CustomApplicationExceptionConverter implements ThrowableConverter<CustomApplicationException> {

        @Override
        JsonRpcResponse.Error convert(CustomApplicationException ex) {
            return new JsonRpcResponse.Error(ex.errorCode, "Custom Application Error")
        }
    }

    static class Request {

        final String jsonrpc = "2.0"
        String id
        String method
        Object params
    }
}
