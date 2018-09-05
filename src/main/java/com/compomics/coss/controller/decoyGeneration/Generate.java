/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller.decoyGeneration;

import java.io.File;

/**
 *
 * @author Genet
 */
public class Generate {

    public Generate() {
    }
    
    public void start(File file, int type){
      GenerateDecoyLib gen=null;
      File decoyFile=null;
        switch(type){
            case 0: gen=new FixedMzShift(file);    
            break;       
            
            case 1: gen=new RandomIntensityFixedMz(file);        
            break;
            
            case 2: gen=new RandomMzIntShift(file);        
            break;     
        }
        if(gen!=null){
            decoyFile = gen.Generate();
        }
        

     //Concatenating library with decoy file 
      MergeFiles m=new MergeFiles(file, decoyFile);
      m.Merge();
      
      decoyFile.delete();      
        
    }
    
    
}
