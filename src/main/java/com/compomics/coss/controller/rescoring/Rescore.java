package com.compomics.coss.controller.rescoring;

import com.compomics.coss.model.ComparisonResult;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Genet
 */
public class Rescore {

    List<ComparisonResult> result;

    public Rescore(List<ComparisonResult> result) {
        this.result = result;

    }

    /**
     * this function re-scores the result given in the pin file and writes the
     * re-scored output to pout file.
     *
     * @throws IOException
     */
    public boolean start_rescoring() throws IOException {
        String cwd = System.getProperty("user.dir");
        File f = new File(cwd + "\\percolator-v3-04\\bin");
        String path_percolatorIn = (cwd + "\\percolator-v3-04\\bin\\pin.tab");
        String path_percolatorOut = (cwd + "\\pout.tab");

        File feature_file = new File(path_percolatorIn);

        GenerateFeatures features = new GenerateFeatures();
        features.generate(result, feature_file);

        String[] cmd = {"cmd", "/c", "percolator.exe", path_percolatorIn, "-m", path_percolatorOut};

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(f);
        pb.redirectErrorStream(true);

        Process process = null;
        int exitvalue = 1;
        try {
            process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            exitvalue = process.waitFor();
            System.out.println("percolator finished with exit value: " + Integer.toString(exitvalue));

        } catch (IOException ex) {
            ex.printStackTrace();

        } catch (InterruptedException ex) {
            Logger.getLogger(Rescore.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            process.destroy();
        }

        return (exitvalue==0);
    }

}
