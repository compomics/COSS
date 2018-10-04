/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller.matching;

import com.compomics.ms2io.Spectrum;
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
public class TheData {

    protected BlockingQueue<Spectrum> expSpec = null;
    protected BlockingQueue<ArrayList<Spectrum>> selectedLibSpec = null;

    protected TheData(ArrayBlockingQueue<Spectrum> expS, ArrayBlockingQueue<ArrayList<Spectrum>> libS) {

        this.expSpec = expS;
        this.selectedLibSpec = libS;

    }

    protected void putExpSpec(Spectrum s) throws InterruptedException {
        this.expSpec.put(s);
    }

    protected void putLibSpec(ArrayList<Spectrum> s) throws InterruptedException {
        this.selectedLibSpec.put(s);
    }

    protected Spectrum pollExpSpec() throws InterruptedException {
        return this.expSpec.poll(1, TimeUnit.SECONDS);
    }

    protected ArrayList<Spectrum> pollLibSpec() throws InterruptedException {
        return this.selectedLibSpec.poll(1, TimeUnit.SECONDS);
    }

}
