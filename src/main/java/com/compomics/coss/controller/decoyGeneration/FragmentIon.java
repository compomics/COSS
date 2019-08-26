package com.compomics.coss.controller.decoyGeneration;

import com.compomics.util.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Genet
 */
public class FragmentIon {

    private String sequence = "";
    private Map<Integer, List<String>> modifications;
    private Map frag_ion;

    /**
     * constructor of this class, instantiating map to frag_ion and fragment seq
     *
     * @param aa_sequence amino acid sequence to be fragment
     * @param modifications any modification on the peptide: Key: modified amino
     * acid position & value: modification type
     */
    public FragmentIon(String aa_sequence, Map<Integer, List<String>> modifications) {
        frag_ion = new HashMap<String, Double>();
        this.sequence = aa_sequence;
        this.modifications = modifications;
        fragment();
    }

    private void fragment() {

        int len_seq = sequence.length();
        int last_index = len_seq - 1;

        for (int i = 0; i < len_seq; i++) {
            String b_ion = sequence.substring(0, i+1);
            String y_ion = sequence.substring(last_index - i, len_seq);

            int len_chars = i + 1;
            String suffix = Integer.toString(len_chars); //number to append to the ion type
            double b_mass = 0;
            double y_mass = 0;
            double a_mass = 0;

            int y_index_track =last_index - i;
            for (int j = 0; j < len_chars; j++) {

                b_mass += AA_Mass.getAA_mass(b_ion.charAt(j));
                y_mass += AA_Mass.getAA_mass(y_ion.charAt(j));

                if (!modifications.isEmpty()) {
                    if (modifications.containsKey(j)) {
                        //iterate over list of modification at this AA position
                        List<String> mods = modifications.get(j);
                        double mod = 0.0;
                        for (int k = 0; k < mods.size(); k++) {
                            mod += Modification_Mass.getMass(mods.get(k));

                        }
                        b_mass += mod;
                    }
                    if (modifications.containsKey(y_index_track + j)) {
                        //iterate over list of modification at this AA position
                        List<String> mods = modifications.get(y_index_track + j);
                        double mod = 0.0;
                        for (int k = 0; k < mods.size(); k++) {
                            mod += Modification_Mass.getMass(mods.get(k));

                        }
                        y_mass += mod;
                    }
                }

            }
            y_mass += AtomMass.getAtomMass("H1") * 2 + AtomMass.getAtomMass("O16");
            a_mass = b_mass - (AtomMass.getAtomMass("C12") + AtomMass.getAtomMass("O16"));
            frag_ion.put("y" + suffix, y_mass);
            frag_ion.put("b" + suffix, b_mass);
            frag_ion.put("a" + suffix, a_mass);

        }
    }

    /**
     * return fragment ion , mass map of sequence
     *
     * @return
     */
    public Map getFragmentIon() {
        return frag_ion;
    }

}
