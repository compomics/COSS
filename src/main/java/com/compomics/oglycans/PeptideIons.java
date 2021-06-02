package com.compomics.oglycans;

import com.compomics.ms2io.model.Modification;
import com.compomics.util.AA_Mass;
import com.compomics.util.AtomMass;
import com.compomics.util.experiment.biology.ions.Ion;
import com.compomics.util.experiment.biology.ions.impl.PeptideFragmentIon;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.biology.proteins.Protein;
import com.compomics.util.experiment.identification.protein_sequences.SingleProteinSequenceProvider;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Genet
 */
public class PeptideIons {

    private String sequence = "";
    private ArrayList<Double> ions;
    private Map<Integer, Double> fragmentIons;
    private Map<Integer, Modification> modifications;
    private HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> utlitiesFragmentIons;

    /**
     * Constructor that takes a utilities {@link Peptide} object.
     *
     * @param peptide the peptide object
     */
    public PeptideIons(Peptide peptide, boolean decoy) {
        modifications = new HashMap<>();
        ions = new ArrayList<>();
        this.sequence = peptide.getSequence();
        SequenceProvider sequenceProvider = new SingleProteinSequenceProvider(new Protein("DUMMY_ACCESSION", peptide.getSequence()));
        String[] variableModifications = peptide.getIndexedVariableModifications();
        String[] fixedModifications = peptide.getFixedModifications(Playground.modificationParameters, sequenceProvider, SequenceMatchingParameters.getDefaultSequenceMatching());
        int index;
        this.utlitiesFragmentIons = IonFactory.getInstance().getFragmentIons(peptide, Playground.modificationParameters, sequenceProvider, SequenceMatchingParameters.getDefaultSequenceMatching());
        //printUtilitiesFragmentIons();
        for (int i = 0; i < variableModifications.length; i++) {
            if (variableModifications[i] != null) {
                com.compomics.util.experiment.biology.modifications.Modification utilitiesModification = Playground.utilitiesModifications.get(variableModifications[i]);

                if (variableModifications[i].equals(Playground.pyroGly.getName())) {
                    index = i;
                } else {
                    index = i - 1;
                }

                if (decoy) {
                    if (index != this.sequence.length() - 1) {
                        index = this.sequence.length() - 2 - index;
                    }
                }

                Modification modification = new Modification(index, peptide.getSequence().charAt(index), utilitiesModification.getMass(), utilitiesModification.getName());
                modifications.put(index, modification);
            }
        }

        for (int i = 0; i < fixedModifications.length; i++) {

            if (fixedModifications[i] != null) {
                com.compomics.util.experiment.biology.modifications.Modification utilitiesModification = Playground.utilitiesModifications.get(fixedModifications[i]);
                index = i - 1;
                Modification modification = new Modification(index, peptide.getSequence().charAt(index), utilitiesModification.getMass(), utilitiesModification.getName());
                modifications.put(index, modification);
            }
        }
        //fragment();
    }

    /**
     * Generate the (fragment, precursor) ions for the given peptide.
     *
     * @param fragmentIonCharges the fragment ion charges to consider
     * @return the list of ions
     */
    public List<Double> generateIons(List<Integer> fragmentIonCharges) {
        HashMap<Integer, ArrayList<Ion>> peptideFragmentIons = utlitiesFragmentIons.get(Ion.IonType.PEPTIDE_FRAGMENT_ION.index);
        for (Integer charge : fragmentIonCharges) {
            // y ions
            ArrayList<Ion> ions = peptideFragmentIons.get(PeptideFragmentIon.Y_ION);
            this.ions.addAll(ions.stream().filter(ion -> ion.getName().equals("y")).map(ion -> ion.getTheoreticMz(charge)).collect(Collectors.toList()));
            // b ions
            ions = peptideFragmentIons.get(PeptideFragmentIon.B_ION);
            this.ions.addAll(ions.stream().filter(ion -> ion.getName().equals("b")).map(ion -> ion.getTheoreticMz(charge)).collect(Collectors.toList()));
            // z ions
            ions = peptideFragmentIons.get(PeptideFragmentIon.Z_ION);
            this.ions.addAll(ions.stream().filter(ion -> ion.getName().equals("z")).map(ion -> ion.getTheoreticMz(charge)).collect(Collectors.toList()));
            // c ions
            ions = peptideFragmentIons.get(PeptideFragmentIon.C_ION);
            this.ions.addAll(ions.stream().filter(ion -> ion.getName().equals("c")).map(ion -> ion.getTheoreticMz(charge)).collect(Collectors.toList()));

            HashMap<Integer, ArrayList<Ion>> precursorIons = utlitiesFragmentIons.get(Ion.IonType.PRECURSOR_ION.index);
            this.ions.addAll(precursorIons.get(0).stream().map(ion -> ion.getTheoreticMz(charge)).collect(Collectors.toList()));
        }

        Collections.sort(ions);
        return ions;
    }

    private void fragment() {
        this.sequence = this.sequence.replaceAll("\\s+", "");
        int len_seq = sequence.length();
        int last_index = len_seq - 1;
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
                        if (Math.abs(mod - 503.3) < 0.001) {
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

                        if (Math.abs(mod - 503.3) < 0.001) {
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


            ions.add(y_mass);
//            frag_ion.add(y_mass / 2);
//
//            frag_ion.add(b_mass);
//            frag_ion.add(b_mass / 2);
//            frag_ion.add(a_mass);
//            frag_ion.add(a_mass / 2);
//            frag_ion.add(c_mass);
//            frag_ion.add(c_mass / 2);
//            frag_ion.add(z_mass);
//            frag_ion.add(z_mass + 1);
//            frag_ion.add(z_mass + 1);
//            frag_ion.add(z_mass / 2);
//            frag_ion.add(z_mass / 3);
//            frag_ion.add(z_mass / 4);


//            frag_ion.add(Math.roung(y_mass*10000)/10000.0d);
//            frag_ion.add(Math.roung(y_mass*10000)/20000.0d);
//
//            frag_ion.add(Math.roung(b_mass*10000)/10000.0d);
//            frag_ion.add(Math.roung(b_mass*10000)/20000.0d);
//            frag_ion.add(Math.roung(a_mass*10000)/10000.0d);
//            frag_ion.add(Math.roung(a_mass*10000)/20000.0d);
//            frag_ion.add(Math.roung(c_mass*10000)/10000.0d);
//            frag_ion.add(Math.roung(c_mass*10000)/20000.0d);
//            frag_ion.add(Math.roung(z_mass*10000)/10000.0d);
//            frag_ion.add(Math.roung(z_mass*10000)/20000.0d);
//            frag_ion.add(Math.roung(z_mass*10000)/30000.0d);
//            frag_ion.add(Math.roung(z_mass*10000)/40000.0d);
        }
        ions = (ArrayList) ions.stream().distinct().collect(Collectors.toList());
    }

    private void printUtilitiesFragmentIons() {
        HashMap<Integer, ArrayList<Ion>> peptideFragments = utlitiesFragmentIons.get(Ion.IonType.PEPTIDE_FRAGMENT_ION.index);
        ArrayList<Ion> ions = peptideFragments.get(PeptideFragmentIon.Y_ION);
        HashMap<Integer, ArrayList<Ion>> precursor = utlitiesFragmentIons.get(Ion.IonType.PRECURSOR_ION.index);
        System.out.println("");
        for (int i = 0; i < ions.size(); i++) {
            if (ions.get(i).getName().equals("y")) {
                System.out.println(ions.get(i).getName() + "   " + ions.get(i).getTheoreticMass() + "    " + ions.get(i).getTheoreticMz(1));
            }
        }

    }

    /**
     * return fragment ion , mass map of sequence
     *
     * @return
     */
    public ArrayList getFragmentIon() {
        return ions;
    }

    /**
     * return modification
     *
     * @return
     */
    public Map<Integer, Modification> getModification() {
        return this.modifications;
    }
}
