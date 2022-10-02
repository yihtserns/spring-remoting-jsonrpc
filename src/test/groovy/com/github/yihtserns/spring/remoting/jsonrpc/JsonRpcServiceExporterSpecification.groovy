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
import org.springframework.http.MediaType
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.TimeUnit

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Unroll
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
        response.result == paramValue

        where:
        method                      | paramValue
        "returnStringArg"           | "Expected Param Value"
        "returnStringListArg"       | ["Expected Param Value"]
        "returnStringCollectionArg" | ["Expected Param Value"]

        "returnDoubleArg"           | 1.234
        "returnDoubleWrapperArg"    | 1.234
        "returnDoubleListArg"       | [1.234]
        "returnDoubleCollectionArg" | [1.234]
    }

    def "unsupported array params data types"() {
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
        method                        | paramValue
        "returnStringArrayArg"        | ["Expected Param Value"]
        "returnStringSetArg"          | ["Expected Param Value"]

        "returnDoubleArrayArg"        | [1.234]
        "returnDoubleWrapperArrayArg" | [1.234]
        "returnDoubleSetArg"          | [1.234]

        "returnFloatArg"              | 1.234
        "returnFloatWrapperArg"       | 1.234
        "returnFloatArrayArg"         | [1.234]
        "returnFloatWrapperArrayArg"  | [1.234]
// TODO:        "returnFloatListArg"            | [1.234]
        "returnFloatSetArg"           | [1.234]
// TODO:        "returnFloatCollectionArg"      | [1.234]

        "returnBigDecimalArg"         | 1.234
        "returnBigDecimalArrayArg"    | [1.234]
// TODO:        "returnBigDecimalListArg"       | [1.234]
        "returnBigDecimalSetArg"      | [1.234]
// TODO:        "returnBigDecimalCollectionArg" | [1.234]

        "returnEnumArg"               | TimeUnit.MINUTES.name()
        "returnEnumArrayArg"          | [TimeUnit.MINUTES.name()]
// TODO:        "returnEnumListArg"             | [TimeUnit.MINUTES.name()]
        "returnEnumSetArg"            | [TimeUnit.MINUTES.name()]
// TODO:        "returnEnumCollectionArg"       | [TimeUnit.MINUTES.name()]
    }

    def "supported object params data types"() {
        when:
        def response = callCalc(new Request(
                id: UUID.randomUUID(),
                method: "returnObjectArg",
                params: [(paramName): paramValue]))

        then:
        response.error == null
        response.result[paramName] == paramValue

        where:
        paramName         | paramValue
        "stringValue"     | "Expected Param Value"
        "floatValue"      | 1.234
        "doubleValue"     | 1.234
        "bigDecimalValue" | 1.234
        "enumValue"       | TimeUnit.MINUTES.name()
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

    def "should fail when params contains incompatible value"() {
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
        method                        | paramValue
        "returnDoubleArg"             | "abc"
        "returnDoubleWrapperArg"      | "abc"
        "returnDoubleArrayArg"        | ["abc"]
        "returnDoubleWrapperArrayArg" | ["abc"]
// TODO:        "returnDoubleListArg"         | ["abc"]
        "returnDoubleSetArg"          | ["abc"]
// TODO:        "returnDoubleCollectionArg"   | ["abc"]

// TODO:        "returnDoubleArg"             | "1.234"
// TODO:        "returnDoubleWrapperArg"      | "1.234"
// TODO:        "returnDoubleArrayArg"        | ["1.234"]
// TODO:        "returnDoubleArrayArg"        | ["1.234"]
// TODO:        "returnDoubleWrapperArrayArg" | ["1.234"]
// TODO:        "returnDoubleListArg"         | ["1.234"]
// TODO:        "returnDoubleSetArg"          | ["1.234"]
// TODO:        "returnDoubleCollectionArg"   | ["1.234"]
    }

    def "does not support overloaded method"() {
        when:
        def exporter = new JsonRpcServiceExporter(
                serviceInterface: OverloadedMethodService,
                service: OverloadedMethodServiceImpl)
        exporter.afterPropertiesSet()

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "Duplicate method name is not supported: overloaded"
    }

    private Map callCalc(Request request) {
        return restTemplate.postForObject(
                "http://localhost:${port}/calc",
                new HttpEntity(request, new HttpHeaders(contentType: MediaType.APPLICATION_JSON)),
                Map)
    }

    @Configuration
    @EnableAutoConfiguration
    static class Application {

        @Bean("/calc")
        JsonRpcServiceExporter calcServiceJsonRpcServiceExporter() {
            return new JsonRpcServiceExporter(
                    serviceInterface: CalcService,
                    service: calcService())
        }

        @Bean
        CalcService calcService() {
            return new CalcServiceImpl()
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

        float returnFloatArg(float value)

        Float returnFloatWrapperArg(Float value)

        float[] returnFloatArrayArg(float[] value)

        Float[] returnFloatWrapperArrayArg(Float[] value)

        List<Float> returnFloatListArg(List<Float> value)

        Set<Float> returnFloatSetArg(Set<Float> value)

        Collection<Float> returnFloatCollectionArg(Collection<Float> value)

        double returnDoubleArg(double value)

        Double returnDoubleWrapperArg(Double value)

        double[] returnDoubleArrayArg(double[] value)

        Double[] returnDoubleWrapperArrayArg(Double[] value)

        List<Double> returnDoubleListArg(List<Double> value)

        Set<Double> returnDoubleSetArg(Set<Double> value)

        Collection<Double> returnDoubleCollectionArg(Collection<Double> value)

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

        DataTypeObject returnObjectArg(DataTypeObject value)
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
        DataTypeObject returnObjectArg(DataTypeObject value) {
            return value
        }
    }

    static class SubtractObject {

        int firstValue
        int secondValue
    }

    static class DataTypeObject {

        String stringValue
        float floatValue
        double doubleValue
        BigDecimal bigDecimalValue
        TimeUnit enumValue
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

    static class Request {

        final String jsonrpc = "2.0"
        String id
        String method
        Object params
    }
}
