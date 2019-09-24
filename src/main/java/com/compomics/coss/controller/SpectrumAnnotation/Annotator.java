/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller.SpectrumAnnotation;

import com.compomics.ms2io.model.Modification;
import com.compomics.ms2io.model.Peak;
import com.compomics.ms2io.model.Spectrum;
import com.compomics.util.AtomMass;
import com.compomics.util.FragmentIon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 *
 * @author Genet
 */
public class Annotator implements Callable<Spectrum> {
    
    private final Spectrum spectrum;
    private final double fragTol;
    public Annotator(Spectrum spec, double fragTol){
        this.spectrum = spec;
        this.fragTol=fragTol;
    }

    @Override
    public Spectrum call() throws Exception {       
             
        double H2O_loss = AtomMass.getAtomMass("H1") + AtomMass.getAtomMass("H1") + AtomMass.getAtomMass("O16");
        double NH3_loss = AtomMass.getAtomMass("H1") + AtomMass.getAtomMass("H1") + AtomMass.getAtomMass("H1") + AtomMass.getAtomMass("N15");
        double H2O_loss_mass, NH3_loss_mass, charge2_mass,charge1_mass, charge3_mass;
        double H2O_loss_1Z_mass, H2O_loss_2Z_mass, H2O_loss_3Z_mass;
        double NH3_loss_1Z_mass,NH3_loss_2Z_mass, NH3_loss_3Z_mass;
        double delta, deltaZ1, deltaZ2, deltaZ3;
        double deltaH2O_, deltaNH3_;
        double deltaH2O_1Z, deltaH2O_2Z, deltaH2O_3Z;
        double deltaNH3_2Z, deltaNH3_3Z, deltaNH3_1Z;
        
        Map<Integer, Modification> modifications = new HashMap<>();
        for (Modification m : this.spectrum.getModifications()) {
            modifications.put(m.getModificationPosition(), m);
        }

        FragmentIon fIon = new FragmentIon(this.spectrum.getSequence(), modifications);
        Map<String, Double> fragments = fIon.getFragmentIon();
        double mass = 0;
     
        ArrayList<Peak> peaks = this.spectrum.getPeakList();
        
        for (Peak p : peaks) {
            double Pk_mass=p.getMz();
            p.setPeakAnnotation("\"?\"");

            for (HashMap.Entry frag : fragments.entrySet()) {
             
              //mass is the current fragment mass to check a match to the current peak's mass(p.getMz)
                mass = (double)frag.getValue();
             
                /**
                 * Neutral charge mass difference
                 */
                delta =Math.abs(mass- Pk_mass);                
                if (delta <= this.fragTol){
                    p.setPeakAnnotation(frag.getKey().toString());
                    break;
                }
               
                /**
                 * current fragment masses affected by charge            
                * charge1_mass = (mass + 1)
                charge2_mass = (charge1_mass + 1)/2
                charge3_mass = (charge2_mass + 1)/3
                 */
                
                charge1_mass = mass + 1;   
                deltaZ1 =  Math.abs(charge1_mass- Pk_mass);
                if (deltaZ1<=this.fragTol) {
                    p.setPeakAnnotation(frag.getKey().toString() + "^1");
                    break;
                }
                
                charge2_mass = (charge1_mass + 1)/2.0;
                deltaZ2 =  Math.abs(charge2_mass-Pk_mass);
                 if (deltaZ2 <= this.fragTol) {                    
                    p.setPeakAnnotation(frag.getKey() + "^2");
                    break;                   
                }
                                 

                
                 /**
                 * current fragment mass affected by neutral loss
                 */
                H2O_loss_mass = mass - H2O_loss;// mass after water loss
                NH3_loss_mass = mass - NH3_loss; // mass after amonia loss
                 
                deltaH2O_ = Math.abs(H2O_loss_mass- Pk_mass);
                deltaNH3_ = Math.abs(NH3_loss_mass - Pk_mass);
                if (deltaH2O_ <= this.fragTol) {
                    p.setPeakAnnotation(frag.getKey().toString() + "-H2O");
                    break;                   

                } else if (deltaNH3_<=this.fragTol) {                    
                    p.setPeakAnnotation(frag.getKey().toString() + "-NH3");
                    break;
                } 
         
                /**
                 * mass affected by both H2O loss and charges 1 and 2
                 */
                
                H2O_loss_1Z_mass = H2O_loss_mass + 1;
                deltaH2O_1Z = Math.abs(H2O_loss_1Z_mass - Pk_mass);                
                if (deltaH2O_1Z <= this.fragTol) {                    
                    p.setPeakAnnotation(frag.getKey().toString() + "-H2O" + "^1");
                    break;                   

                } 
                
               
                H2O_loss_2Z_mass = (H2O_loss_1Z_mass + 1)/ 2.0;;  
                deltaH2O_2Z = Math.abs(H2O_loss_2Z_mass-Pk_mass);
                if (deltaH2O_2Z <= this.fragTol) {
                    
                    p.setPeakAnnotation(frag.getKey().toString() + "-H2O" + "^2");
                    break;
                    

                }
                
                
                 /**
                 * mass affected by both NH3 loss and charges 1 and 2
                 */
                NH3_loss_1Z_mass = NH3_loss_mass + 1;
                deltaNH3_1Z = Math.abs(NH3_loss_1Z_mass-Pk_mass);
                if (deltaNH3_1Z <= this.fragTol) {                    
                    p.setPeakAnnotation(frag.getKey().toString() + "-NH3" + "^1");
                    break;
                    
                } 
                
                NH3_loss_2Z_mass = (NH3_loss_1Z_mass+1)/2.0;
                deltaNH3_2Z = Math.abs(NH3_loss_2Z_mass-Pk_mass);
                if (deltaNH3_2Z <= this.fragTol) {
                    
                    p.setPeakAnnotation(frag.getKey().toString() + "-NH3" + "^2");
                    break;
                    
                }
                
                /**
                 * Charge 3 masses: only charge, charge with H20 loss
                 * and charge with NH3 loss
                 */
                charge3_mass = (charge2_mass + 1)/3.0;
                deltaZ3 = Math.abs(charge3_mass - Pk_mass);
                if (deltaZ3 <= this.fragTol) {
                    p.setPeakAnnotation(frag.getKey() + "^3");
                    break;      
                }
                
                 H2O_loss_3Z_mass = (H2O_loss_2Z_mass+1)/3.0;   
                 deltaH2O_3Z = Math.abs(H2O_loss_3Z_mass-Pk_mass);
                 if (deltaH2O_3Z <= this.fragTol) {                    
                    p.setPeakAnnotation(frag.getKey().toString() + "-H2O" + "^3");
                    break;
                }
                 
                 
                NH3_loss_3Z_mass = (NH3_loss_2Z_mass+1)/3.0;
                deltaNH3_3Z = Math.abs(NH3_loss_3Z_mass - Pk_mass);
                if (deltaNH3_3Z <= this.fragTol) {
                    
                    p.setPeakAnnotation(frag.getKey().toString() + "-NH3" + "^3");
                    break;
                    
                }
               
            }
           
        }
        this.spectrum.setPeakList(peaks);
        return this.spectrum;
    }
    
}
