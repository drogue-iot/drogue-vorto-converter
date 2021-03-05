package io.drogue.iot.vorto;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ConverterTest {

    @Test
    public void testConvert() {

        given()
                .when()
                .header("Ce-Specversion", "1.0")
                .header("Ce-Type", "foo")
                .header("Ce-Source", "bar")
                .header("Ce-Id", "123")
                .header("Content-Type", "application/json")
                .header("Ce-Subject", "TEXT")
                .header("Ce-application", "my")
                .header("Ce-device", "dev1")
                .header("Ce-dataschema", "vorto:vorto.private.ctron:DeviceOne:1.0.0")
                .body("{\"temp\": 1.23}")
                .post("/")

                .then()
                .statusCode(200)
                .body(
                        "path", is("/features"),
                        "topic", is("my/dev1/things/twin/commands/modify"),
                        "value.blockOne.properties.status.temperature", is(1.23f)
                );
    }

    @Test
    public void testIsJson() {
        assertThat(Converter.isJson("text/json"), is(true));
        assertThat(Converter.isJson("application/json"), is(true));
        assertThat(Converter.isJson("application/foo+json; charset=UTF-8"), is(true));
    }
}
