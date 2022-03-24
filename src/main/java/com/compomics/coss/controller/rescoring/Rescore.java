package com.compomics.coss.controller.rescoring;

import com.compomics.coss.model.ComparisonResult;
import com.compomics.coss.model.ConfigData;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.SystemUtils;

/**
 *
 * @author Genet
 */
public class Rescore {

    List<ComparisonResult> result;
    public String error_msg;
    public Map<Integer, String> rescored_result = new HashMap<>();

    public Rescore(List<ComparisonResult> result) {
        this.result = result;

    }

    /**
     * this function re-scores the result given in the pin file and writes the
     * re-scored output to pout file.
     *configData: configuration class object 
     * @throws IOException
     */
    public boolean start_rescoring(ConfigData confData, String output_path) throws IOException {
        File f = new File(confData.getPercolatorPath());
        String path_percolatorOut = FilenameUtils.removeExtension(output_path) + "_rescored.tab";
        String cwd = System.getProperty("user.dir");
        
        String path_percolatorIn = (cwd + "\\percolator-v3-04\\bin\\pin.tab");

        File feature_file = new File(path_percolatorIn);
        GenerateFeatures features = new GenerateFeatures();
        features.generate(result, feature_file);

        String[] cmd;

        if (SystemUtils.IS_OS_WINDOWS) {
            cmd = new String[]{"cmd", "/c", confData.getPercolatorPath(), path_percolatorIn, "-m", path_percolatorOut};

        } else if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC_OSX) {
            cmd = new String[]{"bash", "-c", confData.getPercolatorPath(), path_percolatorIn, "-m", path_percolatorOut};

        } else {
            throw new IllegalStateException("COSS V2 doesn't support the OS of this system");
        }
      
     
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(f);
        pb.redirectErrorStream(true);
        Process process = null;
        int exitvalue = 1;

        try {
            process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            error_msg = "";
            boolean started = false;
            boolean ended = false;
            StringBuilder sb = new StringBuilder();
            String id = "";

            while ((line = reader.readLine()) != null) {
                if (line.contains("Error") || line.contains("Exception caught")) {
                    error_msg += line + "\n";
                } else if (line.startsWith("PSMId")) {
                    started = true;
                    ended = false;
                } else if (started && !ended) {
                    String[] psm_split = line.split("\t");
                    sb.append(psm_split[1]);
                    sb.append(",");
                    sb.append(psm_split[2]);
                    String title = psm_split[0];
                    id = title.substring(title.indexOf("Index")).split("=")[1];
                    int index = Integer.parseInt(id);
                    rescored_result.put(index, sb.toString());
                    sb.setLength(0);

                } else if (line.contains("percolator finished")) {
                    ended = true;
                    started = false;
                }

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

        return (exitvalue == 0);
    }

}
