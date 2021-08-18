import org.apache.hadoop.io.LongWritable;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;

import de.webis.chatnoir2.mapfile_generator.inputformats.WarcInputFormat;
import de.webis.chatnoir2.mapfile_generator.warc.WarcRecord;

public class WARCParsingUtil {

	private WARCParsingUtil() {
		// hide utility class constructor
	}

	public static JavaPairRDD<LongWritable, WarcRecord> records(JavaSparkContext sc, String path,
			Class<? extends WarcInputFormat> inputFormat) {
		return sc.newAPIHadoopFile(path, inputFormat, LongWritable.class, WarcRecord.class, sc.hadoopConfiguration());
	}

	public static boolean isResponse(WarcRecord record) {
		return record != null && record.getRecordType() != null
				&& "response".equalsIgnoreCase(record.getRecordType().trim());
	}
}
