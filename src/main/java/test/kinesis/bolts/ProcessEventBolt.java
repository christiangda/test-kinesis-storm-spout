package test.kinesis.bolts;

import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.kinesis.spouts.MainKinesisRecordScheme;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.Map;

public class ProcessEventBolt  extends BaseBasicBolt {

    private final Logger LOG = LoggerFactory.getLogger(ProcessEventBolt.class);
    public static final String NAME = "ProcessEventBolt";
    public static final String FIELD_EVENT = "FieldEvent";
    private transient CharsetDecoder decoder;

    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        decoder = Charset.forName("UTF-8").newDecoder();

        // In case of malformed data stream (unknown characters)
        decoder.onMalformedInput(CodingErrorAction.IGNORE);
    }
    @Override
    public void execute(Tuple tuple, BasicOutputCollector basicOutputCollector) {

        LOG.debug("Executing {}", NAME);

        String partitionKey = (String) tuple.getValueByField(MainKinesisRecordScheme.FIELD_PARTITION_KEY);
        String sequenceNumber = (String) tuple.getValueByField(MainKinesisRecordScheme.FIELD_SEQUENCE_NUMBER);
        byte[] payload = (byte[]) tuple.getValueByField(MainKinesisRecordScheme.FIELD_RECORD_DATA);

        ByteBuffer buffer = ByteBuffer.wrap(payload);
        String data = null;

        try {
            data = decoder.decode(buffer).toString();
            LOG.info("Got record: partitionKey={}, sequenceNumber={}, data={}", partitionKey, sequenceNumber, data);
        } catch (CharacterCodingException e) {
            LOG.error("Exception when decoding record: partitionKey={}, sequenceNumber={}, exception: ", partitionKey, sequenceNumber, e);
            e.printStackTrace();
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(FIELD_EVENT));
    }
}
