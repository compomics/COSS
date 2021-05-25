package com.compomics.oglycans;

import com.compomics.coss.model.ResourceUtils;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.biology.proteins.Protein;
import com.compomics.util.experiment.identification.protein_sequences.digestion.ExtendedPeptide;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Playground {

    public static void main(String[] args) {

        try {
<<<<<<< HEAD
            FastaReader fastaReader = new FastaReader();

            File output_file=new File("test.msp");
            
//            File mgf_file = ResourceUtils.getResourceByRelativePath("CMB-763_EThcD_export.mgf").getFile(); 
            
            List<Peptide> peptides = fastaReader.readPeptideFasta(ResourceUtils.getResourceByRelativePath("GlycopeptidePool.fasta").getFile());            
            Map<Protein, List<ExtendedPeptide>> proteins = fastaReader.readProteinFasta(ResourceUtils.getResourceByRelativePath("PeptidePoolContatenated.fasta").getFile());    
            
            
            ArrayList<String> pep_string = new ArrayList<>();
            for (Peptide p : peptides) {
                pep_string.add(p.getSequence());
            }
=======
            PeptideGenerator peptideGenerator = new PeptideGenerator();

            File output_file = new File("test.msp");

//            File mgf_file = ResourceUtils.getResourceByRelativePath("CMB-763_EThcD_export.mgf").getFile();

            List<Peptide> peptides = peptideGenerator.readPeptideFasta(ResourceUtils.getResourceByRelativePath("GlycopeptidePool.fasta").getFile());
            //peptides.forEach(peptide -> System.out.println(peptide.getSequence() + " " + peptide.getMass()));

            Map<Protein, List<Peptide>> proteins = peptideGenerator.readProteinFasta(ResourceUtils.getResourceByRelativePath("PeptidePoolContatenated.fasta").getFile());
            //proteins.forEach((protein, extendedPeptides) -> extendedPeptides.forEach(extendedPeptide -> System.out.println(extendedPeptide.peptide.getSequence() + " " + extendedPeptide.peptide.getMass())));

>>>>>>> e3cb71c88958998af128049902337ed6eda03ebf
            Generate_spectra gen = new Generate_spectra();
            //gen.start(peptides, output_file);

//            File mgf_file = ResourceUtils.getResourceByRelativePath("oglycans_test_1.fasta").getFile();
//
//            List<Peptide> peptides = fastaReader.readPeptideFasta(ResourceUtils.getResourceByRelativePath("oglycans_test_1.fasta").getFile());
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
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
