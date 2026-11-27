# Quarkus 4 consumer sample

This child module builds a Quarkus 4 application with the `quarkus4-consumer`
profile. It inherits the extension version from the root POM and consumes the
extension JARs from the local Maven repository. Quarkus discovers the
deployment JAR through the runtime JAR's extension descriptor.

With JDK 21 or newer, run these commands from the repository root:

```sh
./mvnw -pl deployment -am install -DskipTests
./mvnw -pl quarkus4-consumer -Pquarkus4-consumer verify
```

The first command installs the extension using the root build's Quarkus 3
version. If the JARs and their POMs are already installed, run only the second
command.

The integration test starts an embedded Hazelcast member on a free loopback
port and launches the packaged application. The application uses the injected
`HazelcastInstance` to write, read, and remove a sample entry.

To run the packaged sample against a running Hazelcast member, supply its
address and cluster name:

```sh
java -Dquarkus.hazelcast-client.cluster-members=127.0.0.1:5701 \
     -Dquarkus.hazelcast-client.cluster-name=dev \
     -jar quarkus4-consumer/target/quarkus-app/quarkus-run.jar
```
