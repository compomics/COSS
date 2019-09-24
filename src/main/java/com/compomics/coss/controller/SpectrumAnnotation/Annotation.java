package com.compomics.coss.controller.SpectrumAnnotation;

import com.compomics.coss.model.ComparisonResult;
import com.compomics.ms2io.controller.Indexer;
import com.compomics.ms2io.controller.MgfReader;
import com.compomics.ms2io.controller.MspReader;
import com.compomics.ms2io.controller.MspWriter;
import com.compomics.ms2io.controller.SpectraReader;
import com.compomics.ms2io.controller.SpectraWriter;
import com.compomics.ms2io.model.*;
import com.compomics.util.AtomMass;
import com.compomics.util.FragmentIon;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author Genet
 */
public class Annotation {

    /**
     * spectrum file to be annotated
     */
    protected File f;

    /**
     * fragment tolerance
     */
    protected double fragTol;

    /**
     * constructor for annotating the entire spectrum from the given file
     *
     * @param file spectrum file to be annotated
     * @param fragmentTolerance fragment mass error
     */
    public Annotation(File file, double fragmentTolerance) {
        this.f = file;
        this.fragTol = fragmentTolerance;

    }

    /**
     * annotating the given spectrum
     *
     * @param spec spectrum to be annotated
     * @param fragmentTolerance mass error
     */
    public Annotation(Spectrum spec, double fragmentTolerance) {

    }

    /**
     * Annotating the given peak list
     *
     * @param peakList list of peaks to be annotated
     * @param aaSequence aa sequence
     * @param ch charge
     * @param modifications modifications to be considered
     * @param fragmentTolerance mass error
     */
    public Annotation(List<Peak> peakList, String aaSequence, Charge ch, List<Modification> modifications, double fragmentTolerance) {

    }

    public void annotateSpecFile(boolean modifyOriginalFile) throws IOException, InterruptedException, ExecutionException {

        SpectraReader specReader = null;
        Indexer giExp = new Indexer(this.f);
        List<IndexKey> indxList = giExp.generate();
        Spectrum spec1 = null;

        if (this.f.getName().endsWith("mgf")) {
            specReader = new MgfReader(this.f, indxList);

        } else if (this.f.getName().endsWith("msp") || this.f.getName().endsWith("sptxt")) {
            specReader = new MspReader(this.f, indxList);

        }
        String filename = f.getName().substring(0, f.getName().lastIndexOf("."));
        File f_ann = new File(this.f.getParent(), filename + "_annotated.msp");
        SpectraWriter wr = new MspWriter(f_ann);

        int len_index = indxList.size();
        Annotator annottor;

        Future<Spectrum> future;
        ExecutorService executor = Executors.newFixedThreadPool(4);
     
        for (int i = 0; i < len_index ; i++) {
            spec1=specReader.readAt(indxList.get(i).getPos());
            
            annottor = new Annotator(specReader.readAt(indxList.get(i).getPos()), fragTol);    
            future = executor.submit(annottor);
            spec1 = future.get();
            
            
            synchronized(this){
                wr.write(spec1);
                System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b" + Integer.toString(i));
           }

        }
      wr.closeWriter();
      executor.shutdown();

    }

    public Spectrum annotateSpectrum() {
                                                                                                         
        return null;

    }

}
