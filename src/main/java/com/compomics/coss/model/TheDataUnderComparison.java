/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.model;

import com.compomics.ms2io.model.Spectrum;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * This class creates and holds blocking queues for experimental spectrum and
 * selected library spectrum based on precursor mass. The comparison is done
 * between experimental spectrum against corresponding spectra at the same
 * location in their respective queue.
 *
 * @author Genet
 */
public class TheDataUnderComparison {

    private BlockingQueue<Spectrum> expSpec = null;
    private BlockingQueue<ArrayList<Spectrum>> selectedLibSpec = null;

    public TheDataUnderComparison(ArrayBlockingQueue<Spectrum> expS, ArrayBlockingQueue<ArrayList<Spectrum>> libS) {

        this.expSpec = expS;
        this.selectedLibSpec = libS;

    }

    public void putExpSpec(Spectrum s) throws InterruptedException {
        this.expSpec.put(s);
    }

    public void putLibSpec(ArrayList<Spectrum> s) throws InterruptedException {
        this.selectedLibSpec.put(s);
    }

    public Spectrum pollExpSpec() throws InterruptedException {
        return this.expSpec.poll(1, TimeUnit.SECONDS);
    }

    public ArrayList<Spectrum> pollLibSpec() throws InterruptedException {
        return this.selectedLibSpec.poll(1, TimeUnit.SECONDS);
    }
    
    public BlockingQueue<Spectrum> getExpSpec(){
        return expSpec;
    }
    
    public BlockingQueue<ArrayList<Spectrum>>  getLibSelectedSpec(){
        return selectedLibSpec;
    }

}
