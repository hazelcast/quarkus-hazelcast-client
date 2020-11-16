package io.quarkus.it.hazelcast.client;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.hazelcast.HazelcastServerTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;

@QuarkusTest
@QuarkusTestResource(HazelcastServerTestResource.class)
public class HazelcastClientFunctionalityTest {

    @Test
    public void dataStructuresSmokeTest() {
        RestAssured
          .when().get("/hazelcast-client/smoke-test")
          .then().body(is("OK"));
    }

    @Test
    public void CPSubsystemSmokeTest() {
        RestAssured
          .when().get("/hazelcast-client/smoke-test/cp")
          .then().body(is("OK"));
    }

    @Test
    public void shouldReadFromDistributedMap() {
        RestAssured
          .when().get("/hazelcast-client/ds/get?key=nonexisting")
          .then().body(is("default"));
    }

    @Test
    public void shouldWriteDataSerializableToDistributedMap() {
        RestAssured
          .when().post("/hazelcast-client/ds/put?key=foo&value=foo_value")
          .thenReturn();

        RestAssured
          .when().get("/hazelcast-client/ds/get?key=foo")
          .then().body(is("foo_value"));
    }

    @Test
    public void shouldWriteIdentifiedDataSerializableToDistributedMap() {
        RestAssured
          .when().post("/hazelcast-client/ids/put?key=foo&value=foo_value")
          .thenReturn();

        RestAssured
          .when().get("/hazelcast-client/ids/get?key=foo")
          .then().body(is("foo_value"));
    }

    @Test
    public void shouldWritePortableToDistributedMap() {
        RestAssured
          .when().post("/hazelcast-client/ptable/put?key=foo&value=foo_value")
          .thenReturn();

        RestAssured
          .when().get("/hazelcast-client/ptable/get?key=foo")
          .then().body(is("foo_value"));
    }

    @Test
    public void shouldIncrementAtomicLong() {
        RestAssured
          .when().get("/hazelcast-client/cp/atomic-long/increment?name=foo")
          .then().body(is("1"));

        RestAssured
          .when().get("/hazelcast-client/cp/atomic-long/increment?name=foo")
          .then().body(is("2"));
    }
}
