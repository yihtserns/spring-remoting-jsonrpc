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
        method            | paramValue
        "returnStringArg" | "Expected Param Value"
        "returnDoubleArg" | 1.234
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
        method                | paramValue
        "returnFloatArg"      | 1.234
        "returnBigDecimalArg" | 1.234
        "returnEnumArg"       | TimeUnit.MINUTES.name()
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

        int returnFloatArg(float value)

        double returnDoubleArg(double value)

        BigDecimal returnBigDecimalArg(BigDecimal value)

        TimeUnit returnEnumArg(TimeUnit value)

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
        int returnFloatArg(float value) {
            return value
        }

        @Override
        double returnDoubleArg(double value) {
            return value
        }

        @Override
        BigDecimal returnBigDecimalArg(BigDecimal value) {
            return value
        }

        @Override
        TimeUnit returnEnumArg(TimeUnit value) {
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

    static class Request {

        final String jsonrpc = "2.0"
        String id
        String method
        Object params
    }
}
