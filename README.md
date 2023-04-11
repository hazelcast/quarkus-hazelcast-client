# Hazelcast Client for Quarkus

<a href="https://github.com/actions/toolkit"><img alt="GitHub Actions status" src="https://github.com/hazelcast/quarkus-hazelcast-client/workflows/CI/badge.svg"></a>
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.hazelcast/quarkus-hazelcast-client/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.hazelcast/quarkus-hazelcast-client) 

[Hazelcast IMDG](https://hazelcast.com/products/imdg/) is a distributed in-memory object store and compute engine that supports a wide variety of data structures such as Map, Set, List, MultiMap, RingBuffer, HyperLogLog. 

Use Hazelcast IMDG to store your data in RAM, spread and replicate it across a cluster of machines, and perform data-local computation on it. 

Hazelcast is:
- cloud and Kubernetes friendly
- often used as a Distributed Cache amongst other use cases

See the official guide [here](https://guides.hazelcast.org/hazelcast-client-quarkus/).

## Features
- The HazelcastInstance bean is initialized lazily by Quarkus, if you want eager initialization, make sure to double-check [Quarkus Documentation](https://quarkus.io/guides/cdi-reference#eager-instantiation-of-beans). 

## Configuration

After configuring `quarkus-universe BOM`:

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>io.quarkus</groupId>
                <artifactId>quarkus-universe-bom</artifactId>
                <version>${insert.newest.quarkus.version.here}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

You can just configure the `hazelcast-client` extension by adding the following dependency:

    <dependency>
        <groupId>com.hazelcast</groupId>
        <artifactId>quarkus-hazelcast-client</artifactId>
    </dependency>
    
***NOTE:*** You can bootstrap a new application quickly by using [code.quarkus.io](https://code.quarkus.io) and choosing `quarkus-hazelcast-client`
    
### Quarkus hazelcast-client configuration

The extension exposes a single native-mode-compatible Hazelcast Client bean (`HazelcastInstance`) which can be directly injected into your beans:

    private final HazelcastInstance hazelcastInstance;
    
    public UsersService(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

By default, client will try to connect to a Hazelcast instance running on the host using port 5701.

Defaults can be customized using `application.properties` entries such as:

    quarkus.hazelcast-client.cluster-members
    quarkus.hazelcast-client.outbound-port-definitions
    quarkus.hazelcast-client.outbound-ports
    quarkus.hazelcast-client.cluster-name
    quarkus.hazelcast-client.labels
    quarkus.hazelcast-client.connection-timeout

For example:

    quarkus.hazelcast-client.cluster-members=localhost:5701
    
If you don't provide a port number, Hazelcast Client will try to autodetect it:

    quarkus.hazelcast-client.cluster-members=localhost
    
You can also pass a comma-separate list of values as a single entry:
    
    quarkus.hazelcast-client.cluster-members=localhost:5701,localhost:5702
    
If you need more, use a standard `hazelcast-client.yml/hazelcast-client.xml`-based configuration (described below) or wire-up your own `HazelcastInstance` bean. 

Keep in mind that you will still be able to benefit from GraalVM compatibility!

### Configuration Files

#### Configuration using `hazelcast-client.yml` or `hazelcast-client.xml`

In order to configure the client using the `hazelcast-client.yml`/`hazelcast-client.xml` file, place the configuration file in the `src/main/resources` directory.

Keep in mind that configuration entries from `hazelcast-client.yml/xml` are overridden by `quarkus.hazelcast-client.*` entries 
so it's a good idea to stick to one of these exclusively.

## Testing

To make testing simple, the extension provides the `HazelcastServerTestResource` which automatically launches an embedded Hazelcast instance with defaults settings and manages its lifecycle:

     @QuarkusTest
     @QuarkusTestResource(HazelcastServerTestResource.class)
     public class HazelcastAwareTest {

         @Test
         public void test() {
             // you can safely call embedded Hazelcast instance from here
         }
     }
     
#### Maven dependency:

    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-test-hazelcast</artifactId>
        <scope>test</scope>
    </dependency>
    
## Compatibility Matrix

- `1.0.x` is compatible with Quarkus 1.6.x-1.7.x (no longer maintained)
- `1.1.1` is compatible with Quarkus 1.8.x+ and Hazelcast 4.0.x and 4.1.x and JDK 8+
- `1.2.0` is compatible with Quarkus 1.13.x+ and Hazelcast 4.x and JDK 8+
- `3.0.0` is compatible with Quarkus 2.x+ and Hazelcast 4.x and JDK 8+

## Limitations (native mode)
There's no support for:
- Default Java serialization
- User code deployment
- Hazelcast SPI support on OSGi
- JCache integration
- JMX
