package io.quarkus.it.hazelcast.client;

import com.hazelcast.cardinality.CardinalityEstimator;
import com.hazelcast.collection.BaseQueue;
import com.hazelcast.collection.IList;
import com.hazelcast.collection.IQueue;
import com.hazelcast.collection.ISet;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.cp.IAtomicLong;
import com.hazelcast.crdt.pncounter.PNCounter;
import com.hazelcast.durableexecutor.DurableExecutorService;
import com.hazelcast.flakeidgen.FlakeIdGenerator;
import com.hazelcast.multimap.MultiMap;
import com.hazelcast.replicatedmap.ReplicatedMap;
import com.hazelcast.ringbuffer.Ringbuffer;
import com.hazelcast.scheduledexecutor.IScheduledExecutorService;
import com.hazelcast.topic.ITopic;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hazelcast-client")
public class RootResource {

    private final HazelcastInstance hazelcastInstance;

    public RootResource(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @POST
    @Path("/ds/put")
    @Produces(MediaType.APPLICATION_JSON)
    public void ds_put(@QueryParam("key") String key, @QueryParam("value") String value) {
        DataSerializableWrapper dataSerializableWrapper = new DataSerializableWrapper();
        dataSerializableWrapper.setValue(value);
        hazelcastInstance.getMap("ds_map").put(key, dataSerializableWrapper);
    }

    @GET
    @Path("/ds/get")
    @Produces(MediaType.APPLICATION_JSON)
    public String ds_get(@QueryParam("key") String key) {
        return hazelcastInstance.<String, DataSerializableWrapper>getMap("ds_map")
          .getOrDefault(key, new DataSerializableWrapper("default")).getValue();
    }

    @POST
    @Path("/ids/put")
    @Produces(MediaType.APPLICATION_JSON)
    public void ids_put(@QueryParam("key") String key, @QueryParam("value") String value) {
        IdentifiedDataSerializableWrapper dataSerializableWrapper = new IdentifiedDataSerializableWrapper();
        dataSerializableWrapper.setValue(value);
        hazelcastInstance.getMap("ids_map").put(key, dataSerializableWrapper);
    }

    @GET
    @Path("/ids/get")
    @Produces(MediaType.APPLICATION_JSON)
    public String ids_get(@QueryParam("key") String key) {
        return hazelcastInstance.<String, IdentifiedDataSerializableWrapper>getMap("ids_map")
          .getOrDefault(key, new IdentifiedDataSerializableWrapper("default")).getValue();
    }

    @POST
    @Path("/ptable/put")
    @Produces(MediaType.APPLICATION_JSON)
    public void ptable_put(@QueryParam("key") String key, @QueryParam("value") String value) {
        PortableWrapper portable = new PortableWrapper("value1");
        portable.setValue(value);
        hazelcastInstance.getMap("ptable_map").put(key, portable);
    }

    @GET
    @Path("/ptable/get")
    @Produces(MediaType.APPLICATION_JSON)
    public String ptable_put_get(@QueryParam("key") String key) {
        return hazelcastInstance.<String, PortableWrapper>getMap("ptable_map")
          .getOrDefault(key, new PortableWrapper("default")).getValue();
    }

    @GET
    @Path("/cp/atomic-long/increment")
    @Produces(MediaType.APPLICATION_JSON)
    public String cp_atomic_long(@QueryParam("name") String name) {
        IAtomicLong atomicLong = hazelcastInstance.getCPSubsystem().getAtomicLong(name);
        return Long.toString(atomicLong.incrementAndGet());
    }

    @GET
    @Path("/smoke-test")
    @Produces(MediaType.APPLICATION_JSON)
    public String ds_smoke_test() {
        ISet<Object> iset = hazelcastInstance.getSet("foo");
        BaseQueue<?> iqueue = hazelcastInstance.getQueue("foo");
        Ringbuffer<Object> ringbuffer = hazelcastInstance.getRingbuffer("foo");
        IList<Object> ilist = hazelcastInstance.getList("foo");
        ReplicatedMap<Object, Object> replicatedMap = hazelcastInstance.getReplicatedMap("foo");
        MultiMap<Object, Object> multiMap = hazelcastInstance.getMultiMap("foo");
        IQueue<Object> iQueue = hazelcastInstance.getQueue("foo");
        ITopic<Object> iTopic = hazelcastInstance.getReliableTopic("foo");
        ITopic<Object> iReliableTopic = hazelcastInstance.getTopic("foo");
        CardinalityEstimator cardinalityEstimator = hazelcastInstance.getCardinalityEstimator("foo");
        FlakeIdGenerator flakeIdGenerator = hazelcastInstance.getFlakeIdGenerator("foo");
        PNCounter pnCounter = hazelcastInstance.getPNCounter("foo");
        IExecutorService executorService = hazelcastInstance.getExecutorService("foo");
        DurableExecutorService durableExecutorService = hazelcastInstance.getDurableExecutorService("foo");
        IScheduledExecutorService scheduledExecutorService = hazelcastInstance.getScheduledExecutorService("foo");
        return "OK";
    }
}
