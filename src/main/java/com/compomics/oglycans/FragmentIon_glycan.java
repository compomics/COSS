package com.compomics.oglycans;

import java.util.HashMap;
import java.util.Map;
import com.compomics.util.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 *
 * @author Genet
 */
public class FragmentIon_glycan {

    private String sequence = "";
    private ArrayList<Double> frag_ion;

    /**
     * constructor of this class, instantiating map to frag_ion and fragment seq
     *
     * @param aa_sequence amino acid sequence to be fragment
     * @param modifications any modification on the peptide: Key: modified amino
     * acid position & value: modification type
     */
    public FragmentIon_glycan(String aa_sequence) {
        frag_ion = new ArrayList<>();        
        this.sequence = aa_sequence;
        fragment();
    }

    private void fragment() {

        this.sequence=this.sequence.replaceAll("\\s+","");
        int len_seq = sequence.length();
        int last_index = len_seq - 1;
        char bionAA;
        char yionAA;

        boolean b_modified=false;
        boolean y_modified=false;
        for (int i = 0; i < len_seq; i++) {
            String b_ion = sequence.substring(0, i+1);
            String y_ion = sequence.substring(last_index - i, len_seq);

            int len_chars = i + 1;
            //String suffix = Integer.toString(len_chars); //number to append to the ion type
            
            double b_mass = 0;           
            double a_mass = 0;
            double c_mass = 0;
            double y_mass = 0;
            double z_mass = 0;

            for (int j = 0; j < len_chars; j++) {

                bionAA=b_ion.charAt(j);
                yionAA=y_ion.charAt(j);
                
                b_mass += AA_Mass.getAA_mass(bionAA);
                y_mass += AA_Mass.getAA_mass(yionAA);

                //check if the aa from N term and aa from C term modified
                //modification not applied on b and y ion, but keep track if it exists 
                //mass of c and z ions then shifted if modification exists
                if (true){ //modification is possible for all s and t aa
                    if (bionAA == 'T' || bionAA == 'S') {
                        //b_mass += 503.3;
                        b_modified=true;
                    }
                    if (yionAA == 'T' || yionAA == 'S') {
                        //y_mass += 503.3;
                        y_modified=true;
                    }
                }

            }
            
            
            y_mass += AtomMass.getAtomMass("H1") * 2 + AtomMass.getAtomMass("O16");
            a_mass = b_mass - (AtomMass.getAtomMass("C12") + AtomMass.getAtomMass("O16"));
            frag_ion.add(y_mass);
            frag_ion.add(b_mass);
            frag_ion.add(a_mass);
            
            //fragments added for glycan fragments
            c_mass = b_mass + (AtomMass.getAtomMass("C12") + AtomMass.getAtomMass("O16"));
            z_mass = y_mass - (AtomMass.getAtomMass("N15") + AtomMass.getAtomMass("H1"));
            
            //in the case of ETD fragmentation mass shift due to modification is possible, if the aa is S or T
            //and applied on c and z ions
            if(b_modified){
                c_mass+=503.3;
            }
            if(y_modified){
                z_mass+=503.3;
            }
            frag_ion.add(c_mass);
            frag_ion.add(z_mass);                        
        }
        frag_ion = (ArrayList)frag_ion.stream().distinct().collect(Collectors.toList());
    }

    /**
     * return fragment ion , mass map of sequence
     *
     * @return
     */
    public ArrayList getFragmentIon() {
        return frag_ion;
    }

}
