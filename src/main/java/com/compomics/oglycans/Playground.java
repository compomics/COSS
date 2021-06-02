package com.compomics.oglycans;

import com.compomics.coss.model.ResourceUtils;
import com.compomics.util.AtomMass;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationCategory;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.modifications.ModificationType;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.io.mass_spectrometry.mgf.MgfFileIterator;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerDummy;
import com.compomics.util.parameters.identification.search.ModificationParameters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Playground {

    private static ModificationFactory modificationFactory = ModificationFactory.getInstance();
    public static final ModificationParameters modificationParameters = new ModificationParameters();
    public static Modification oglycan;
    public static final Modification oxidation = modificationFactory.getModification("Oxidation of M");
    public static final Modification pyroGly = modificationFactory.getModification("Pyrolidone from E");
    public static final Modification carbo = modificationFactory.getModification("Carbamidomethylation of C");
    public static Map<String, Modification> utilitiesModifications = new HashMap<>();
    private static List<Modification> variableModifications = new ArrayList<>();

    static {
        // add O-glycans mod with TMT label to ModificationFactory
        ArrayList<String> residues = new ArrayList<>();
        residues.add("S");
        residues.add("T");
        oglycan = new Modification(ModificationType.modaa, "oglycans", 503.3, residues, ModificationCategory.Common);
        modificationFactory.addUserModification(oglycan);

        modificationParameters.addFixedModification(carbo);
        modificationParameters.addVariableModification(oglycan);
        modificationParameters.addVariableModification(oxidation);
        modificationParameters.addVariableModification(pyroGly);

        utilitiesModifications.put("oglycans", oglycan);
        utilitiesModifications.put("Oxidation of M", oxidation);
        utilitiesModifications.put("Pyrolidone from E", pyroGly);
        utilitiesModifications.put("Carbamidomethylation of C", carbo);

        variableModifications.add(oglycan);
        variableModifications.add(oxidation);
        variableModifications.add(pyroGly);
    }

    public Playground() {
    }

    public static void main(String[] args) {
        List<Integer> precursorCharges = new ArrayList<>();
        precursorCharges.add(2);
        precursorCharges.add(4);
        precursorCharges.add(5);

        try {
            File output_file = new File("test.msp");
            if (output_file.exists()) {
                output_file.delete();
            }

            PeptideGenerator peptideGenerator = new PeptideGenerator(variableModifications);
            List<Peptide> peptides = peptideGenerator.readPeptideFasta(ResourceUtils.getResourceByRelativePath("GlycopeptidePoolSmall.fasta").getFile());

            // read the MGF file
//            MgfFileIterator mgfFileIterator = new MgfFileIterator(new File("/home/niels/Downloads/Niels_glycopeptide_pool/CMB-763_EThcD_export.mgf"), new WaitingHandlerDummy());
//            while (mgfFileIterator.next() != null) {
//                Spectrum spectrum = mgfFileIterator.getSpectrum();
//                for (Peptide peptide : peptides) {
//                    for (Integer precursorCharge : precursorCharges) {
//                        if (Math.abs((peptide.getMass() / precursorCharge) + precursorCharge * AtomMass.getAtomMass("H1") - spectrum.precursor.getMass(precursorCharge)) < 0.5) {
//                            System.out.println("match");
//                        }
//                    }
//                }
//            }

            SpectrumGenerator spectrumGenerator = new SpectrumGenerator();
            spectrumGenerator.generateSpectra(peptides, output_file);

            System.out.println("");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
