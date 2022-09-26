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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Unroll
class JsonRpcServiceExporterSpecification extends Specification {

    @LocalServerPort
    private int port

    @Autowired
    private TestRestTemplate restTemplate

    def "can call using params of int array"() {
        when:
        String id = UUID.randomUUID().toString()
        def response = restTemplate.postForObject(
                "http://localhost:${port}/calc",
                new HttpEntity(
                        [
                                jsonrpc: "2.0",
                                id     : id,
                                method : "subtract",
                                params : [10, 3]
                        ],
                        new HttpHeaders(contentType: MediaType.APPLICATION_JSON)),
                Map)

        then:
        response.id == id
        response.error == null
        response.result == 10 - 3
    }

    def "should fail when params array size is not the same as the method parameters count"() {
        when:
        String id = UUID.randomUUID().toString()
        def response = restTemplate.postForObject(
                "http://localhost:${port}/calc",
                new HttpEntity(
                        [
                                jsonrpc: "2.0",
                                id     : id,
                                method : "subtract",
                                params : params
                        ],
                        new HttpHeaders(contentType: MediaType.APPLICATION_JSON)),
                Map)

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

        int subtract(int x, int y)
    }

    static class CalcServiceImpl implements CalcService {

        @Override
        int subtract(int x, int y) {
            return x - y;
        }
    }
}
