package test.kinesis;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kinesis.stormspout.InitialPositionInStream;
import com.amazonaws.services.kinesis.stormspout.KinesisSpout;
import com.amazonaws.services.kinesis.stormspout.KinesisSpoutConfig;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.shade.org.apache.zookeeper.KeeperException;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.utils.Utils;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.kinesis.bolts.ProcessEventBolt;
import test.kinesis.config.ConfigKeys;
import test.kinesis.config.ParseConfig;
import test.kinesis.spouts.MainKinesisRecordScheme;
import test.kinesis.utils.CustomCredentialsProviderChain;

import java.io.IOException;
import java.util.Map;

public class ProcessKinesisEvents {

    private final Logger LOG = LoggerFactory.getLogger(ProcessKinesisEvents.class);
    private final String topologyName = this.getClass().getSimpleName();
    private TopologyBuilder topoBuilder = new TopologyBuilder();
    private Config topoConf = new Config();
    private LocalCluster cluster;


    private ProcessKinesisEvents(Map<String, Object> globalConf) {

        //Set the access to Kinesis Stream
        CustomCredentialsProviderChain awsCredentials = new CustomCredentialsProviderChain(
                globalConf.get(ConfigKeys.AWS_CREDENTIALS_PROFILE).toString()
        );


        //
        KinesisSpoutConfig kinesisConf = new KinesisSpoutConfig(
                globalConf.get(ConfigKeys.KINESIS_STREAM_NAME).toString(),
                globalConf.get(ConfigKeys.KINESIS_ZOOKEEPER_END_POINTS).toString()
        )
                .withZookeeperPrefix((String) globalConf.get(ConfigKeys.KINESIS_ZOOKEEPER_DATA_PREFIX))
                .withKinesisRecordScheme(new MainKinesisRecordScheme())
                .withInitialPositionInStream((InitialPositionInStream) globalConf.get(ConfigKeys.KINESIS_STREAM_INITIAL_POSITION))
                .withRecordRetryLimit((Integer) globalConf.get(ConfigKeys.KINESIS_RECORD_RETRY_LIMIT))
                .withRegion((Regions) globalConf.get(ConfigKeys.KINESIS_REGION_NAME));

        //
        final KinesisSpout GetEventsSpout = new KinesisSpout(kinesisConf, awsCredentials, new ClientConfiguration());

        //**************************************************************************//
        //*************************** Topology Structure ***************************//
        this.topoBuilder.setSpout("GetEventsSpout", GetEventsSpout, 1);
        this.topoBuilder.setBolt(ProcessEventBolt.NAME, new ProcessEventBolt(), 2).shuffleGrouping("GetEventsSpout");
    }

    private void runDevelop() throws InvalidTopologyException, AuthorizationException, AlreadyAliveException {
        LOG.info("-- Running storm topology in develop mode --");
        LOG.info("-- Submitting topology " + topologyName + " --");

        System.out.println("Not implemented yet");

/*        int clientPort = 21818; // none-standard
        int numConnections = 5000;
        int tickTime = 2000;
        String dataDirectory = System.getProperty("java.io.tmpdir");

        File dir = new File(dataDirectory, "zookeeper").getAbsoluteFile();

        ZooKeeperServer server = new ZooKeeperServer(dir, dir, tickTime);
        NIOServerCnxn.Factory standaloneServerFactory = new NIOServerCnxn.Factory(new InetSocketAddress(clientPort), numConnections);

        standaloneServerFactory.startup(server); // start the server.*/

        //StormSubmitter.submitTopologyWithProgressBar(topologyName, topoConf, topoBuilder.createTopology());

        //standaloneServerFactory.shutdown()
    }

    private void runLocal(int runTime) {
        LOG.info("-- Running storm topology in local mode --");
        LOG.info("-- Submitting topology " + topologyName + " to local cluster --");

        //topoConf.setDebug(true);
        topoConf.setNumWorkers(2);

        cluster = new LocalCluster();
        cluster.submitTopology(topologyName, topoConf, topoBuilder.createTopology());
        if (runTime > 0) {
            Utils.sleep(runTime);
            shutDownLocal();
        }
    }

    private void shutDownLocal() {
        if (cluster != null) {
            cluster.killTopology(topologyName);
            cluster.shutdown();
        }
    }

    private void runCluster() throws AlreadyAliveException, InvalidTopologyException, AuthorizationException {
        LOG.debug("-- Running storm topology in cluster mode --");
        LOG.info("-- Submitting topology " + topologyName + " to cluster --");

        topoConf.setNumWorkers(2);
        topoConf.setMaxSpoutPending(1000);

        StormSubmitter.submitTopology(topologyName, topoConf, topoBuilder.createTopology());
    }

    private static void printUsageAndExit() {
        System.out.println("Usage: " + ProcessKinesisEvents.class.getName() + " <yamlFileConfig> <local|cluster>");
        System.exit(-1);
    }

    public static void main(String[] args) throws IllegalArgumentException, KeeperException, InterruptedException,
            AlreadyAliveException, InvalidTopologyException, IOException, NotFound, AuthorizationException {
        String fileConfig = null;
        String mode = null;

        if (args.length != 2) {
            printUsageAndExit();
        } else {
            fileConfig = args[0];
            mode = args[1];
        }

        // Global Configuration for Topology
        ParseConfig config = new ParseConfig(fileConfig);

        if (mode != null) {

            ProcessKinesisEvents topology = new ProcessKinesisEvents(config.getConf());

            if (mode.equals("develop")) {
                topology.runDevelop();
            } else if (mode.equals("local")) {
                topology.runLocal(100000);
            } else if (mode.equals("cluster")) {
                topology.runCluster();
            }
        } else {
            printUsageAndExit();
        }

    }
}
