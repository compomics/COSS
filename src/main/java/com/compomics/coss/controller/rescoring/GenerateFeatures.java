package com.compomics.coss.controller.rescoring;

import com.compomics.coss.model.ComparisonResult;
import com.compomics.coss.model.MatchedLibSpectra;
import com.compomics.ms2io.model.Spectrum;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Genet
 */
public class GenerateFeatures {

    public GenerateFeatures() {

    }

    public void generate(List<ComparisonResult> result, File feature_file ) throws IOException {
        Spectrum spec;
        Spectrum matechedSpec;
        String delm = "\t";

        MatchedLibSpectra matchedLib;
        String protein = "";
        String[] columns = {"id", "label", "ScanNr", "RetentionT", "precMass", "ChargeQuery", "Score", "#MatchedPeaks", "MatchedIntQuery", "Peptide", "Proteins"};
        
        FileWriter fileOut = new FileWriter(feature_file);

        //writing the column name
        fileOut.write(Arrays.asList(columns).stream().collect(Collectors.joining(delm)));
        fileOut.write("\n");
        int result_index=0;

        for (ComparisonResult res : result) {
            int s = 0;//only the first rank is taken for rescoring
            spec = res.getEspSpectrum();

            matchedLib=res.getMatchedLibSpec().get(s);
            matechedSpec = matchedLib.getSpectrum();

            
            fileOut.write(spec.getTitle()+" Index="+ Integer.toString(result_index) + delm); // id

            int label = matchedLib.getSource();
            if(label==0){
                label=-1;
            }
            fileOut.write(Integer.toString(label) + delm);//label decoy or library -1 decoy 1 for library

            fileOut.write(spec.getScanNumber() + delm);// ScanNr

            fileOut.write(spec.getRtTime() + delm); //RetentionT
            
            fileOut.write(Double.toString(spec.getPCMass()) + delm);//precMass

             fileOut.write(spec.getCharge_asStr() + delm); //ChargeQuery

            fileOut.write(Double.toString(res.getTopScore()) + delm);//Score
           
            fileOut.write(Integer.toString(matchedLib.getNumMatchedPeaks()) + delm); //#MatchedPeaks

            fileOut.write(Double.toString(matchedLib.getSumMatchedInt_Exp()) + delm); //#MatchedPeaksSumIntensity
         
            fileOut.write(matechedSpec.getSequence() + delm);//peptide
            
            protein = matechedSpec.getProtein(); //proteins
            if(protein.isEmpty()){
                //this field is need by percolator.
                protein="P54652"; //random protein assignment for percolator if it is not given. this doesn't affect the result.
            }
            protein.replaceAll("^\"|\"$", "");
            fileOut.write(protein + delm);

            fileOut.write("\n");
            result_index++;

        }
        fileOut.flush();
        fileOut.close();


    }
}
