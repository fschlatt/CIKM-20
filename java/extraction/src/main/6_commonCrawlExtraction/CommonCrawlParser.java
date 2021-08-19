/*
 * MIT License
 *
 * Copyright (c) 2020 Stefan Heindorf, Yan Scholten, Henning Wachsmuth,
 * Axel-Cyrille Ngonga Ngomo, Martin Potthast
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import de.webis.chatnoir2.mapfile_generator.inputformats.WarcInputFormat;
import de.webis.chatnoir2.mapfile_generator.warc.WarcRecord;

public final class CommonCrawlParser {

  private static final int N_EXTRACTION_THREADS = 16;
  private static Logger logger = LogManager.getLogger(Main.class);

  private static String loadIgnoreUriPatternString(String ignoreUriPath) throws FileNotFoundException {
    File ignoreUrisFile = new File(ignoreUriPath);
    FileReader fr = new FileReader(ignoreUrisFile);
    BufferedReader bufferedReader = new BufferedReader(fr);

    List<String> ignoreUris = new ArrayList<String>();
    try {
      for (String uri = bufferedReader.readLine(); uri != null; uri = bufferedReader.readLine()) {
        ignoreUris.add("\\Q" + uri + "\\E");
      }
      bufferedReader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return StringUtils.join(ignoreUris, "|");
  }

  public static void parse(JavaSparkContext sc, final String path, Class<? extends WarcInputFormat> inputFormat,
      String output, final String pathPatterns, final String pathStopWordList, final String ignoreUriPath) {
    String ignoreUriPatternString;
    try {
      ignoreUriPatternString = loadIgnoreUriPatternString(ignoreUriPath);
      JavaRDD<WarcRecord> warcRecords = WARCParsingUtil.records(sc, path, inputFormat).values();
      JavaRDD<String> causalClaims = warcRecords
          .flatMap(r -> read(r, pathPatterns, pathStopWordList, ignoreUriPatternString));
      causalClaims.saveAsTextFile(output);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  private static List<String> extractText(final String warcRecordIdUri, final String warcTargetUriStr,
      final String warcDate, final String html, PotthastJerichoExtractor textExtractor, final String pathPatterns) {
    List<String> sentences = textExtractor.extract(html);

    List<String> results = new ArrayList<String>();
    if (sentences == null || sentences.isEmpty()) {
      return results;
    }

    String text = StringUtils.join(sentences, "");
    if (text.trim().equals("")) {
      return results;
    }

    LinkedList<CommonCrawlSentence> CommonCrawlSentences = new LinkedList<>();
    for (String sentenceSurface : sentences) {
      CommonCrawlSentences.add(new CommonCrawlSentence(warcRecordIdUri, warcTargetUriStr, warcDate, sentenceSurface));
    }

    MainExtractor extractor = new MainExtractor(pathPatterns, N_EXTRACTION_THREADS);
    extractor.parse(CommonCrawlSentences);

    for (GeneralSentence sentence : extractor.getAllSentences()) {
      StringBuilder result = new StringBuilder();
      result.append("common_crawl_sentence\t");
      result.append(sentence.printSentence());
      results.add(result.toString());
    }
    return results;
  }

  private static Iterator<String> read(WarcRecord record, final String pathPatterns, final String pathStopWordList,
      final String ignoreUriPatternString) throws IOException {

    PotthastJerichoExtractor textExtractor = new PotthastJerichoExtractor(pathStopWordList);

    if (record == null) {
      logger.warn("Unable to parse warc-record! record is null");
      return Collections.emptyIterator();
    }

    if (record.getHeader() == null) {
      logger.warn("Unable to parse warc-header! Header is null");
      return Collections.emptyIterator();
    }

    WarcHeaderCustom header = new WarcHeaderCustom(record.getHeader());

    if (record.getContent() == null) {
      logger.warn("Unable to parse warc-record! record.getContent() is null");
      return Collections.emptyIterator();
    }

    if (header.getTargetURI() == null) {
      logger.warn("Unable to parse warc-header! header.getTargetURI() is null");
      return Collections.emptyIterator();
    }

    String warcTargetUriStr = header.getTargetURI();
    if (!ignoreUriPatternString.isEmpty()) {
      Pattern ignoreUriPattern = Pattern.compile(ignoreUriPatternString);
      Matcher matcher = ignoreUriPattern.matcher(warcTargetUriStr);
      if (matcher.find()) {
        return Collections.emptyIterator();
      }
    }
    String warcRecordIdUri = header.getRecordID();
    String warcDate = header.getDate();
    String html = record.getContent();
    List<String> results = extractText(warcRecordIdUri, warcTargetUriStr, warcDate, html, textExtractor, pathPatterns);

    return results.iterator();
  }
}
