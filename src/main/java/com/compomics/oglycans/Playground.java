package com.compomics.oglycans;

import com.compomics.coss.model.ResourceUtils;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationCategory;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.modifications.ModificationType;
import com.compomics.util.experiment.biology.proteins.Peptide;
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

        try {
            File output_file = new File("test.msp");
            if(output_file.exists()){
                output_file.delete();
            }
            //File mgf_file = ResourceUtils.getResourceByRelativePath("CMB-763_EThcD_export.mgf").getFile();

            PeptideGenerator peptideGenerator = new PeptideGenerator(variableModifications);
            List<Peptide> peptides = peptideGenerator.readPeptideFasta(ResourceUtils.getResourceByRelativePath("GlycopeptidePool.fasta").getFile());
            //Map<Protein, List<Peptide>> proteins = peptideGenerator.readProteinFasta(ResourceUtils.getResourceByRelativePath("PeptidePoolContatenated.fasta").getFile());

            ArrayList<String> pep_string = new ArrayList<>();
            for (Peptide p : peptides) {
                pep_string.add(p.getSequence());

            }
            Generate_spectra gen = new Generate_spectra();
            gen.start(peptides, output_file);
 
 



//            File mgf_file = ResourceUtils.getResourceByRelativePath("GlycopeptidePool.fasta").getFile();
//     
//            List<Peptide> peptides = fastaReader.readPeptideFasta(ResourceUtils.getResourceByRelativePath("GlycopeptidePool.fasta").getFile());
//
//            
//            Map<Protein, List<ExtendedPeptide>> proteins = fastaReader.readProteinFasta(ResourceUtils.getResourceByRelativePath("PeptidePoolContatenated.fasta").getFile());
//            
//            Generate_spectra gen = new Generate_spectra();
//            gen.start(peptides, mgf_file);
            System.out.println("");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
