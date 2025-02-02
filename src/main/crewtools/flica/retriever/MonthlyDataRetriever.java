/**
 * Copyright 2018 Iron City Software LLC
 *
 * This file is part of CrewTools.
 *
 * CrewTools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CrewTools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CrewTools.  If not, see <http://www.gnu.org/licenses/>.
 */

package crewtools.flica.retriever;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.logging.Logger;

import org.joda.time.YearMonth;

import com.google.common.collect.ImmutableSet;

import crewtools.flica.AwardDomicile;
import crewtools.flica.FlicaConnection;
import crewtools.flica.FlicaService;
import crewtools.flica.Proto.DomicileAward;
import crewtools.flica.Proto.PairingList;
import crewtools.flica.Proto.Rank;
import crewtools.flica.Proto.SeniorityList;
import crewtools.flica.Proto.ThinLineList;
import crewtools.flica.parser.AwardParser;
import crewtools.flica.parser.LineParser;
import crewtools.flica.parser.PairingParser;
import crewtools.flica.parser.ParseException;
import crewtools.flica.parser.SeniorityParser;
import crewtools.flica.stats.DataReader;
import crewtools.util.FlicaConfig;

public class MonthlyDataRetriever {
  private final Logger logger = Logger.getLogger(MonthlyDataRetriever.class.getName());

  private final YearMonth yearMonth;
  private final int seniorityDocumentId;
  private final DataReader dataReader;

  private static final int ROUND_ONE = 1;
  private static final Set<Rank> RANKS = ImmutableSet.of(Rank.FIRST_OFFICER,
      Rank.CAPTAIN);

  public static void main(String args[]) throws Exception {
    new MonthlyDataRetriever(args).run();
  }

  public MonthlyDataRetriever(String args[]) throws IOException {
    if (args.length != 2) {
      System.err.println("MonthlyDataRetriever 2018-01 seniority-document-id");
      System.exit(1);
    }
    this.yearMonth = YearMonth.parse(args[0]);
    this.seniorityDocumentId = Integer.parseInt(args[1]);
    this.dataReader = new DataReader();
  }

  public void run() throws Exception {
    FlicaConfig config = new FlicaConfig();
    FlicaConnection connection = new FlicaConnection(config);
    FlicaService service = new FlicaService(connection);
    service.connect();

    getLinesForAllDomiciles(service);
    getPairingsForAllDomiciles(service);
    getSeniority(service, config);
    getAwards(service);
  }

  private void getLinesForAllDomiciles(FlicaService service)
      throws ParseException, URISyntaxException, IOException {
    for (AwardDomicile awardDomicile : AwardDomicile.values()) {
      for (Rank rank : RANKS) {
        for (int round = 1; round < 3; ++round) {
          if (round == 1 && !rank.equals(Rank.CAPTAIN)) {
            // round 1 lines are the same for captains and first officers.
            continue;
          }
          File outputFile = new File(
              dataReader.getLineFilename(yearMonth, awardDomicile, rank, round));
          if (outputFile.exists()) {
            logger.info("SKIP " + outputFile + " as it exists");
            continue;
          }
          String lines = service.getAllLines(awardDomicile, rank, round,
              yearMonth);
          LineParser lineParser = new LineParser(lines);
          ThinLineList lineList = lineParser.parse();
          FileOutputStream output = new FileOutputStream(outputFile);
          lineList.writeTo(output);
          output.close();
          logger.info("WROTE " + outputFile);
        }
      }
    }
  }

  private void getPairingsForAllDomiciles(FlicaService service)
      throws IOException, URISyntaxException, ParseException {
    for (AwardDomicile awardDomicile : AwardDomicile.values()) {
      File outputFile = new File(dataReader.getPairingFilename(yearMonth, awardDomicile));
      if (outputFile.exists()) {
        logger.info("SKIP " + outputFile + " as it exists");
        continue;
      }
      String pairings = service.getAllPairings(awardDomicile, Rank.FIRST_OFFICER, ROUND_ONE, yearMonth);
      PairingParser pairingParser = new PairingParser(pairings, yearMonth, true);
      PairingList pairingList = pairingParser.parse();
      FileOutputStream output = new FileOutputStream(outputFile);
      pairingList.writeTo(output);
      output.close();
      logger.info("WROTE " + outputFile);
    }
  }

  public void getSeniority(FlicaService service, FlicaConfig config) throws Exception {
    File outputFile = new File(dataReader.getSeniorityFilename(yearMonth));
    if (outputFile.exists()) {
      logger.info("SKIP " + outputFile + " as it exists");
      return;
    }
    byte pdf[] = service.getDocument(AwardDomicile.CLT, Rank.FIRST_OFFICER, ROUND_ONE, yearMonth, seniorityDocumentId,
        "SYSSEN");
    SeniorityParser parser = new SeniorityParser(pdf, config.getDomiciles());
    SeniorityList list = parser.parse();
    list.writeTo(new FileOutputStream(outputFile));
    logger.info("Wrote " + outputFile);
  }

  public void getAwards(FlicaService service) throws Exception {
    for (AwardDomicile awardDomicile : AwardDomicile.values()) {
      for (Rank rank : RANKS) {
        for (int round = 1; round < 3; round++) {
          File outputFile = new File(dataReader.getAwardFilename(
              yearMonth, awardDomicile, rank, round));
          if (outputFile.exists()) {
            logger.info("SKIP " + outputFile + " as it exists");
            return;
          }
          String award = service.getBidAward(awardDomicile, rank, round, yearMonth);
          AwardParser parser = new AwardParser(award, awardDomicile, rank, round);
          DomicileAward protoAward = parser.parse();
          protoAward.writeTo(new FileOutputStream(outputFile));
          logger.info("Wrote " + outputFile);
        }
      }
    }
  }
}
