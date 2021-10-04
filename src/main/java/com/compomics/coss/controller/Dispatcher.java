package com.compomics.coss.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import com.compomics.coss.controller.matching.CosineSimilarity;
import com.compomics.coss.controller.matching.DataProducer;
import com.compomics.coss.controller.matching.DotProduct;
import com.compomics.coss.controller.matching.MSRobin;
import com.compomics.coss.controller.matching.Matcher;
import com.compomics.coss.controller.matching.Intensity_MSE;
import com.compomics.coss.controller.matching.Score;
import com.compomics.coss.model.TheDataUnderComparison;
import com.compomics.ms2io.model.Spectrum;
import com.compomics.coss.model.ComparisonResult;
import java.util.Collections;
import com.compomics.coss.model.ConfigData;
import java.util.logging.Logger;

/**
 *
 *
 *
 * @author Genet
 */
public class Dispatcher {

    private final ConfigData confData;
    private final UpdateListener listener;
    private final org.apache.log4j.Logger log;

    private DataProducer producer;
    private Matcher match;

    public Dispatcher(ConfigData cnfData, UpdateListener lstner, org.apache.log4j.Logger log) {
        this.listener = lstner;
        this.confData = cnfData;
        this.log = log;

    }

    /**
     * stops matching process upon cancel event
     */
    public void stopMatching() {

        match.cancel();
        producer.cancel();
        if (this.listener != null) {
            this.listener.updateprogress(0, 0);
        }

    }

    /**
     * dispatch the work of reading and matching to the algorithm selected
     *
     * @return:returns the comparison result computed
     */
    public List<ComparisonResult> dispatch() {

        List<ComparisonResult> simResult = new ArrayList<>();
        try {

            Score scoreObj = null;
            switch (confData.getScoringFunction()) {
                case 0:
                    scoreObj = new MSRobin(this.confData, this.log);
                    //scoreObj = new TestScore(confData, log);
                    break;
                case 1:
                    scoreObj = new CosineSimilarity(this.confData, this.log);
                    break;
                case 2:
                    scoreObj = new Intensity_MSE(this.confData, this.log);
                    break;

                case 3:
                    scoreObj = new DotProduct(this.confData, this.log);
                    break;
                default:
                    scoreObj = new MSRobin(this.confData, this.log);
                    break;

            }

            if (scoreObj != null) {
                ArrayBlockingQueue<Spectrum> expspec = new ArrayBlockingQueue<>(1000, true);
                ArrayBlockingQueue<ArrayList<Spectrum>> libSelected = new ArrayBlockingQueue<>(1000, true);
                TheDataUnderComparison data = new TheDataUnderComparison(expspec, libSelected);

                producer = new DataProducer(data, confData);
                match = new Matcher(scoreObj, producer, data, confData, listener, log);

                ExecutorService executor = Executors.newFixedThreadPool(2);
                Future future1 = executor.submit(producer);
                Future<List<ComparisonResult>> future = executor.submit(match);
                 future1.get();
                simResult = future.get();
                executor.shutdown();
//                ExecutorService producerThread = Executors.newFixedThreadPool(1);
//                ExecutorService consumerThread = Executors.newFixedThreadPool(5);
//
//                List<ComparisonResult> simResult1 = new ArrayList<>();
//                List<ComparisonResult> simResult2 = new ArrayList<>();
//                List<ComparisonResult> simResult3 = new ArrayList<>();
//                List<ComparisonResult> simResult4 = new ArrayList<>();
//                List<ComparisonResult> simResult5 = new ArrayList<>();
//                
//                Future<List<ComparisonResult>> future1 = null;
//                Future<List<ComparisonResult>> future2 = null;
//                Future<List<ComparisonResult>> future3 = null;
//                Future<List<ComparisonResult>> future4 = null;
//                Future<List<ComparisonResult>> future5 = null;
//                
//                Future futureP = producerThread.submit(producer);
//                
//                for (int i = 0; i < 5; i++) {
//
//                    future1=consumerThread.submit(match);
//                    future2=consumerThread.submit(match);
//                    future3=consumerThread.submit(match);
//                    future4=consumerThread.submit(match);
//                    future5=consumerThread.submit(match);
//                }
//
//                futureP.get();
//                simResult1 = future1.get();
//                simResult2 = future2.get();
//                simResult3 = future3.get();
//                simResult4 = future4.get();
//                simResult5 = future5.get();
//                
//                simResult = Stream.of(simResult1, simResult2, simResult3,simResult4, simResult5)
//                                      .flatMap(Collection::stream)
//                                      .collect(Collectors.toList()); 
                if (simResult != null) {
                    Collections.sort(simResult);
                    Collections.reverse(simResult);
                }
            }

        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(Dispatcher.class.getName()).log(Level.SEVERE, null, ex);
        }

        return simResult;
    }

}
