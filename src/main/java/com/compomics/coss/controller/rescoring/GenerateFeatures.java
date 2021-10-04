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

    public void generate(List<ComparisonResult> result, File feature_file) throws IOException {
        Spectrum spec;
        Spectrum matechedSpec;
        String delm = "\t";

        MatchedLibSpectra matchedLib;
        String protein = "";
        String[] columns = {"id", "label", "ScanNr", "RetentionT", "precMass", "ChargeQuery", "Score", "CosineSim", "MSE_Int", "MSE_MZ", "spearman_corr", "pearson_corr", "pearson_log2_corr", "Score_2nd", "Score_3rd", "#MatchedPeaksQueryFraction", "SumMatchedIntQueryFraction", "SumMatchedIntQueryFraction", "SumMatchedIntLibFraction", "Peptide", "Proteins"};

        FileWriter fileOut = new FileWriter(feature_file);

        //writing the column name
        fileOut.write(Arrays.asList(columns).stream().collect(Collectors.joining(delm)));
        fileOut.write("\n");
        int result_index = 0;
        int lenMSpecs = 1;
        double score2 = 0;
        double score3 = 0;
        for (ComparisonResult res : result) {
            score2 = 0;
            score3 = 0;
            int s = 0;//only the first rank is taken for rescoring
            spec = res.getEspSpectrum();
            lenMSpecs = res.getMatchedLibSpec().size();

            matchedLib = res.getMatchedLibSpec().get(s);
            matechedSpec = matchedLib.getSpectrum();

            fileOut.write(spec.getTitle() + " Index=" + Integer.toString(result_index) + delm); // id

            int label = matchedLib.getSource();
            if (label == 0) {
                label = -1;
            }

            fileOut.write(Integer.toString(label) + delm);//label decoy or library -1 decoy 1 for library

            fileOut.write(spec.getScanNumber() + delm);// ScanNr

            fileOut.write(spec.getRtTime() + delm); //RetentionT

            fileOut.write(Double.toString(spec.getPCMass()) + delm);//precMass

            fileOut.write(spec.getCharge_asStr() + delm); //ChargeQuery

            fileOut.write(Double.toString(res.getTopScore()) + delm);//Score

            //additional scores
            fileOut.write(Double.toString(matchedLib.getScore_cosinesim()) + delm);//cosine sim Score
            fileOut.write(Double.toString(matchedLib.getScore_mse_int()) + delm);//mean square error of intensity
            fileOut.write(Double.toString(matchedLib.getScore_mse_mz()) + delm);//mean square error of mz
            fileOut.write(Double.toString(matchedLib.getCorrelation_spearman()) + delm);//spearman correlation
            fileOut.write(Double.toString(matchedLib.getCorrelation_pearson()) + delm);//pearson correlation
            fileOut.write(Double.toString(matchedLib.getCorrelation_pearson_log2()) + delm);//pearson correlation after log2 transform

            if (lenMSpecs > 1) { //if second match exists
                score2 = res.getMatchedLibSpec().get(1).getScore();
            }
            if (lenMSpecs > 2) { //if there is third matching spectrum
                score3 = res.getMatchedLibSpec().get(1).getScore();
            }

            fileOut.write(Double.toString(score2) + delm);
            fileOut.write(Double.toString(score3) + delm);
            //end of additional scores

            
            fileOut.write(Integer.toString(matchedLib.getNumMatchedPeaks()/matchedLib.getTotalFilteredNumPeaks_Exp()) + delm); //#MatchedPeaks fraction experimental spec
            fileOut.write(Integer.toString(matchedLib.getNumMatchedPeaks()/matchedLib.getTotalFilteredNumPeaks_Lib()) + delm); //#MatchedPeaks fraction library spec
            fileOut.write(Double.toString(matchedLib.getSumMatchedInt_Exp()/matchedLib.getSumFilteredIntensity_Exp()) + delm); //#MatchedPeaksSumIntensity fraction of query spec
            fileOut.write(Double.toString(matchedLib.getSumMatchedInt_Lib()/matchedLib.getSumFilteredIntensity_Lib()) + delm); //#MatchedPeaksSumIntensity fraction of lib spec

            fileOut.write(matechedSpec.getSequence() + delm);//peptide

            protein = matechedSpec.getProtein(); //proteins
            if (protein.isEmpty()) {
                //this field is need by percolator.
                protein = "P54652"; //random protein assignment for percolator if it is not given. this doesn't affect the result.
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
