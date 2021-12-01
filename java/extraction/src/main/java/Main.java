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

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

public final class Main {

  private static Logger logger = LogManager.getLogger(Main.class);

  private Main() {
  }

  public static void main(final String[] args) {
    logger.info("Arguments: ");
    Arrays.stream(args).forEach(logger::info);

    if (args[0].contains("enwiki")) {
      WikipediaParser p = new WikipediaParser();

      String wikipediaDump = args[0];
      String patterns = args[1];
      String output = args[2];
      p.parseDump(wikipediaDump, patterns, output);
    } else if (args[0].contains("clue")) {
      SparkConf conf = new SparkConf();
      JavaSparkContext sc = new JavaSparkContext(conf);

      String cluewebPath = args[0];
      cluewebPath = cluewebPath.replace("\"", "");
      String patterns = args[1];
      String enStopWordList = args[2];
      String output = args[3];
      String ignoreUriPath = args[4];
      ClueWebParser.parse(sc, cluewebPath, FailsafeWarcInputFormats.FailsafeClueWeb12InputFormat.class, output,
          patterns, enStopWordList, ignoreUriPath);
      sc.close();
    } else if (args[0].contains("common")) {
      SparkConf conf = new SparkConf();
      JavaSparkContext sc = new JavaSparkContext(conf);

      String commonCrawlPath = args[0];
      String patterns = args[1];
      String enStopWordList = args[2];
      String output = args[3];
      String ignoreUriPath = args[4];
      CommonCrawlParser.parse(sc, commonCrawlPath, FailsafeWarcInputFormats.FailsafeCommonCrawlInputFormat.class,
          output, patterns, enStopWordList, ignoreUriPath);
      sc.close();
    }
    logger.info("Finished");
  }

}
