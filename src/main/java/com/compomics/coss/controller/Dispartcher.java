package com.compomics.coss.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import com.compomics.coss.controller.UpdateListener;
import com.compomics.coss.controller.matching.DataProducer;
import com.compomics.coss.controller.matching.MSRobin;
import com.compomics.coss.controller.matching.Matcher;
import com.compomics.coss.controller.matching.Score;
import com.compomics.coss.model.TheDataUnderComparison;
import com.compomics.ms2io.Spectrum;
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
public class Dispartcher {

    private final ConfigData confData;
    private final UpdateListener listener;
    private final org.apache.log4j.Logger log;

    private DataProducer producer;
    private Matcher match;

    public Dispartcher(ConfigData cnfData, UpdateListener lstner, org.apache.log4j.Logger log){
        this.listener = lstner;
        this.confData = cnfData;
        this.log = log;

    }
   

    public void stopMatching() {

        match.cancel();
        producer.cancel();
        if(this.listener!=null){
            this.listener.updateprogress(0);
        }

    }

    public List<ComparisonResult> dispatch() {

        List<ComparisonResult> simResult = new ArrayList<>();
        try {

            Score scoreObj = null;
            switch (confData.getScoringFunction()) {
                case 0:
                    scoreObj = new MSRobin(this.confData,this.log);
                    break;
//                case 1: scoreObj = new MSRobin(this.confData, this.listener, this.log);
//                break;
//                case 2: scoreObj = new MSRobin(this.confData, this.listener, this.log);
//                break;

            }

            if (scoreObj != null) {
                ArrayBlockingQueue<Spectrum> expspec = new ArrayBlockingQueue<>(20, true);
                ArrayBlockingQueue<ArrayList<Spectrum>> libSelected = new ArrayBlockingQueue<>(20, true);
                TheDataUnderComparison data = new TheDataUnderComparison(expspec, libSelected);
             
                producer = new DataProducer(data, confData);
                match = new Matcher(scoreObj,producer,data, confData, listener, log);

                ExecutorService executor = Executors.newFixedThreadPool(2);

                Future future1 = executor.submit(producer);
                Future<List<ComparisonResult>> future = executor.submit(match);

                future1.get();
                simResult = future.get();
                executor.shutdown();
            }

        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(Dispartcher.class.getName()).log(Level.SEVERE, null, ex);
        }

        Collections.sort(simResult);
        Collections.reverse(simResult);

        return simResult;
    }

}
