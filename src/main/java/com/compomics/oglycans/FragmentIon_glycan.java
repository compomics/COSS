package com.compomics.oglycans;

import com.compomics.ms2io.model.Modification;
import com.compomics.util.*;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.biology.proteins.Protein;
import com.compomics.util.experiment.identification.protein_sequences.SingleProteinSequenceProvider;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Genet
 */
public class FragmentIon_glycan {

    private String sequence = "";
    private ArrayList<Double> frag_ion;
    private Map<Integer, Modification> modifications;

    public FragmentIon_glycan(Peptide peptide) {
        modifications = new HashMap<>();
        frag_ion = new ArrayList<>();
        this.sequence = peptide.getSequence();
        SequenceProvider sequenceProvider = new SingleProteinSequenceProvider(new Protein("DUMMY_ACCESSION", peptide.getSequence()));
        String[] variableModifications = peptide.getIndexedVariableModifications();
        String[] fixedModifications = peptide.getFixedModifications(Playground.modificationParameters, sequenceProvider, SequenceMatchingParameters.getDefaultSequenceMatching());
        System.out.println(Arrays.toString(variableModifications));
        System.out.println(Arrays.toString(fixedModifications));
        for (int i = 0; i < variableModifications.length; i++) {
            if (variableModifications[i] != null) {
                com.compomics.util.experiment.biology.modifications.Modification utilitiesModification = Playground.utilitiesModifications.get(variableModifications[i]);
                int index;
                if (variableModifications[i].equals(Playground.pyroGly.getName())) {
                    index = i;
                } else {
                    index = i - 1;
                }
                Modification modification = new Modification(index, peptide.getSequence().charAt(index), utilitiesModification.getMass(), utilitiesModification.getName());
                modifications.put(index, modification);
            }
        }
        for (int i = 0; i < fixedModifications.length; i++) {
            if (fixedModifications[i] != null) {
                com.compomics.util.experiment.biology.modifications.Modification utilitiesModification = Playground.utilitiesModifications.get(fixedModifications[i]);
                Modification modification = new Modification(i, peptide.getSequence().charAt(i - 1), utilitiesModification.getMass(), utilitiesModification.getName());
                modifications.put(i, modification);
            }
        }
        fragment();
    }

    /**
     * constructor of this class, instantiating frag_ion and fragment seq
     *
     * @param aa_sequence   amino acid sequence to be fragment
     * @param modifications any modification on the peptide: Key: modified amino
     *                      acid position & value: modification type
     */
    public FragmentIon_glycan(String aa_sequence, Map<Integer, Modification> modifications) {
        frag_ion = new ArrayList<>();
        this.sequence = aa_sequence;
        this.modifications = modifications;
        fragment();
    }

    private void fragment() {

        this.sequence = this.sequence.replaceAll("\\s+", "");
        int len_seq = sequence.length();
        int last_index = len_seq - 1;
        ;
        for (int i = 0; i < len_seq; i++) {
            String b_ion = sequence.substring(0, i + 1);
            String y_ion = sequence.substring(last_index - i, len_seq);

            int len_chars = i + 1;
            double b_mass = 0;
            double a_mass;
            double c_mass;
            double y_mass = 0;
            double z_mass;
            int bion_oglycans = 0;
            int yion_oglycans = 0;

            for (int j = 0; j < len_chars; j++) {

                //String suffix = Integer.toString(len_chars); //number to append to the ion type

                int y_index_track = last_index - i;
                Modification mods;
                b_mass += AA_Mass.getAA_mass(b_ion.charAt(j));
                y_mass += AA_Mass.getAA_mass(y_ion.charAt(j));

                if (!modifications.isEmpty()) {
                    if (modifications.containsKey(j)) {
                        //iterate over list of modification at this AA position

                        mods = modifications.get(j);
                        double mod = mods.getModificationMassShift();

//                        for (int k = 0; k < mods.size(); k++) {
//                            mod += Modification_Mass.getMassShift(mods.get(k));
//
//                        }
                        b_mass += mod;
                        if (mod == 503.3) {
                            bion_oglycans++;
                        }
                    }
                    if (modifications.containsKey(y_index_track + j)) {
                        //iterate over list of modification at this AA position
                        mods = modifications.get(y_index_track + j);
                        double mod = mods.getModificationMassShift();
//                        for (int k = 0; k < mods.size(); k++) {
//                            mod += Modification_Mass.getMassShift(mods.get(k));
//                        }
                        y_mass += mod;

                        if (mod == 503.3) {
                            yion_oglycans++;
                        }
                    }

                }

            }

            y_mass += AtomMass.getAtomMass("H1") * 2 + AtomMass.getAtomMass("O16");//water loss??            
            a_mass = b_mass - (AtomMass.getAtomMass("C12") + AtomMass.getAtomMass("O16"));
            //fragments added for glycan fragments
            c_mass = b_mass + (AtomMass.getAtomMass("N15") + AtomMass.getAtomMass("H1"));
            z_mass = y_mass - (AtomMass.getAtomMass("N15") + AtomMass.getAtomMass("H1"));

            //remove the oglycan mass shift from b and y ions, if it is already added(yion_oglycan!=0)            
            y_mass = y_mass - yion_oglycans * 503.3;
            b_mass = b_mass - bion_oglycans * 503.3;


            frag_ion.add(y_mass);
            frag_ion.add(b_mass);
            frag_ion.add(a_mass);
            frag_ion.add(c_mass);
            frag_ion.add(z_mass);
        }
        frag_ion = (ArrayList) frag_ion.stream().distinct().collect(Collectors.toList());
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
