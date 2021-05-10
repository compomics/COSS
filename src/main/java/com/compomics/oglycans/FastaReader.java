package com.compomics.oglycans;

import com.compomics.util.experiment.biology.enzymes.Enzyme;
import com.compomics.util.experiment.biology.enzymes.EnzymeFactory;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationCategory;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.modifications.ModificationType;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.biology.proteins.Protein;
import com.compomics.util.experiment.identification.protein_sequences.SingleProteinSequenceProvider;
import com.compomics.util.experiment.identification.protein_sequences.digestion.ExtendedPeptide;
import com.compomics.util.experiment.identification.protein_sequences.digestion.ProteinIteratorUtils;
import com.compomics.util.experiment.identification.protein_sequences.digestion.SequenceIterator;
import com.compomics.util.experiment.identification.protein_sequences.digestion.iterators.SpecificSingleEnzymeIterator;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import com.compomics.util.experiment.io.biology.protein.iterators.FastaIterator;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.parameters.identification.search.ModificationParameters;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class FastaReader {

    private EnzymeFactory enzymeFactory = EnzymeFactory.getInstance();
    private ProteinIteratorUtils proteinIteratorUtils;
    ModificationFactory modificationFactory = ModificationFactory.getInstance();
    Modification oglycan;

    public FastaReader() {
        ArrayList<String> residues = new ArrayList<>();
        residues.add("S");
        residues.add("T");
        oglycan = new Modification(ModificationType.modaa, "oglycans", 503.3, residues, ModificationCategory.Common);
        modificationFactory.addUserModification(oglycan);
        ArrayList<String> modifications = new ArrayList<>();
        modifications.add("oglycans");
        proteinIteratorUtils = new ProteinIteratorUtils(modifications, 1);
    }

    /**
     * Return a list of peptides from the given peptide FASTA file.
     *
     * @param fastaFile the FASTA file
     * @return a list of {@link Peptide} objects
     * @throws FileNotFoundException
     */
    public List<Peptide> readPeptideFasta(File fastaFile) throws FileNotFoundException {
        List<Peptide> peptides = new ArrayList<>();

        FastaIterator fastaIterator = new FastaIterator(fastaFile);
        Protein protein;

        ModificationParameters modificationParameters = new ModificationParameters();
        modificationParameters.addFixedModification(oglycan);

        while ((protein = fastaIterator.getNextProtein()) != null) {
            // I'm not sure if this is important
            SequenceProvider sequenceProvider = new SingleProteinSequenceProvider(protein);

            // create a Peptide object because we're dealing with peptides
            Peptide peptide = new Peptide(protein.getSequence());
            peptide.estimateTheoreticMass(modificationParameters, sequenceProvider, SequenceMatchingParameters.getDefaultSequenceMatching());

            peptides.add(peptide);
        }

        return peptides;
    }

    /**
     * Return a Map (key: proteins, value: list of digested peptides) from the given protein FASTA file.
     *
     * @param fastaFile the FASTA file
     * @return a map of protein and associated digested peptides
     * @throws FileNotFoundException
     */
    public Map<Protein, List<ExtendedPeptide>> readProteinFasta(File fastaFile) throws FileNotFoundException, InterruptedException {
        Map<Protein, List<ExtendedPeptide>> proteins = new HashMap<>();

        FastaIterator fastaIterator = new FastaIterator(fastaFile);
        Protein protein;
        while ((protein = fastaIterator.getNextProtein()) != null) {
            List<ExtendedPeptide> extendedPeptides = digestProtein(protein);
            proteins.put(protein, extendedPeptides);
        }

        return proteins;
    }

    private List<ExtendedPeptide> digestProtein(Protein protein) throws InterruptedException {
        List<ExtendedPeptide> extendedPeptides = new ArrayList<>();

        SequenceIterator sequenceIterator = new SpecificSingleEnzymeIterator(proteinIteratorUtils, protein.getSequence(), enzymeFactory.getEnzyme("Trypsin"), 0, 800.0, 10000.0);
        ExtendedPeptide extendedPeptide;
        while ((extendedPeptide = sequenceIterator.getNextPeptide()) != null) {
            extendedPeptides.add(extendedPeptide);
        }

        return extendedPeptides;
    }

}
