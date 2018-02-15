package com.compomics.coss.Controller;

import java.io.File;
import java.util.ArrayList;



/**
 *ReadSpectralData class take file as an argument and read the spectrum of the file using CompOimcs spectrum factory
 * @author Genet
 */
public class ReadSpectralData {

//    static SpectrumFactory fct = SpectrumFactory.getInstance();
//    ArrayList<MSnSpectrum> spectra;
//
//    public ReadSpectralData() {
//
//        this.spectra = new ArrayList<>();
//
//    }
//
//    /**
//     * read and return spectra from the given file
//     * @param file from which the spectra is read
//     * @return array list of spectra 
//     * @throws Exception if the file is not exists
//     */
//    public ArrayList<MSnSpectrum> readSpectra(File file) throws Exception {
//
//        fct.clearFactory();
//        WaitingHandlerCLIImpl waitingHandlerCLIImpl = new WaitingHandlerCLIImpl();
//        fct.addSpectra(file, waitingHandlerCLIImpl);
//        
//        this.spectra.clear();
//        for (String title : fct.getSpectrumTitles(file.getName())) {
//            MSnSpectrum ms = (MSnSpectrum) fct.getSpectrum(file.getName(), title);
//
//            if (!ms.getPeakList().isEmpty()) {
//                spectra.add(ms);
//
//            }
//
//        }
//
//        fct.clearFactory();
//        return spectra;
//
//    }

}
