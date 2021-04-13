package com.compomics.coss.controller.rescoring;

import com.compomics.coss.model.ComparisonResult;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.compress.compressors.FileNameUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

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
     *
     * @throws IOException
     */
    public boolean start_rescoring(String output_path) throws IOException {
        String cwd = System.getProperty("user.dir");
        File f = new File(cwd + "\\percolator-v3-04\\bin");
        String path_percolatorIn = (cwd + "\\percolator-v3-04\\bin\\pin.tab");

       
        
        String path_percolatorOut =  FilenameUtils.removeExtension(output_path)+ "_rescored.tab";
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
            error_msg="";
            boolean started=false;
            boolean ended=false;
            StringBuilder sb=new StringBuilder();
            String id="";
            
            while ((line = reader.readLine()) != null) {
                if(line.contains("Error") || line.contains("Exception caught")){
                    error_msg+=line + "\n";
                }else if(line.startsWith("PSMId")){
                    started=true;
                    ended=false;
                }else if(started && !ended){
                    
                    String[] psm_split=line.split("\t");
                    sb.append(psm_split[1]);
                    sb.append(",");
                    sb.append(psm_split[2]);  
                    String title = psm_split[0];                    
                    id=title.substring(title.indexOf("Index")).split("=")[1];
                    int index=Integer.parseInt(id);
                    rescored_result.put(index, sb.toString());
                    sb.setLength(0);
                    
                    
                }else if(line.contains("percolator finished")){
                    ended=true;
                    started=false;
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

        return (exitvalue==0);
    }

}
