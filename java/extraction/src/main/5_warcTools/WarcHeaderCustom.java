import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import de.webis.chatnoir2.mapfile_generator.warc.WarcHeader;

@SuppressWarnings("serial")
public class WarcHeaderCustom implements Serializable {

	private String targetURI = "";
	private String recordID = "";
	private String trecID = "";
	private String infoID = "";
	private String date = "";

	public WarcHeaderCustom(String targetURI, String recordID, String trecID, String infoID, String date) {
		this.targetURI = targetURI;
		this.recordID = recordID;
		this.trecID = trecID;
		this.infoID = infoID;
		this.date = date;
	}

	public WarcHeaderCustom(WarcHeader header) {
		TreeMap<String, String> meta = header.getHeaderMetadata();

		for (Map.Entry<String, String> thisEntry : meta.entrySet()) {
			if (thisEntry.getKey().equals("WARC-Target-URI"))
				targetURI = thisEntry.getValue();
			if (thisEntry.getKey().equals("WARC-Record-ID"))
				recordID = thisEntry.getValue();
			if (thisEntry.getKey().equals("WARC-TREC-ID"))
				trecID = thisEntry.getValue();
			if (thisEntry.getKey().equals("WARC-Warcinfo-ID"))
				infoID = thisEntry.getValue();
			if (thisEntry.getKey().equals("WARC-Date"))
				date = thisEntry.getValue();
		}
	}

	public String getTargetURI() {
		return targetURI;
	}

	public String getRecordID() {
		return recordID;
	}

	public String getTrecID() {
		return trecID;
	}

	public String getInfoID() {
		return infoID;
	}

	public String getDate() {
		return date;
	}
}
