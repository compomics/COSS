package com.compomics.oglycans;

import com.compomics.util.experiment.biology.enzymes.EnzymeFactory;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.biology.proteins.Protein;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.protein_sequences.SingleProteinSequenceProvider;
import com.compomics.util.experiment.identification.protein_sequences.digestion.ExtendedPeptide;
import com.compomics.util.experiment.identification.protein_sequences.digestion.ProteinIteratorUtils;
import com.compomics.util.experiment.identification.protein_sequences.digestion.SequenceIterator;
import com.compomics.util.experiment.identification.protein_sequences.digestion.iterators.SpecificSingleEnzymeIterator;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import com.compomics.util.experiment.io.biology.protein.iterators.FastaIterator;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PeptideGenerator {

    private static final Logger LOGGER = Logger.getLogger(PeptideGenerator.class);

    private EnzymeFactory enzymeFactory = EnzymeFactory.getInstance();
    private ProteinIteratorUtils proteinIteratorUtils;
    private List<Modification> variableModifications;

    public PeptideGenerator(List<Modification> variableModifications) {
        proteinIteratorUtils = new ProteinIteratorUtils(Playground.modificationParameters.getFixedModifications(), 1);
        this.variableModifications = variableModifications;
    }

    /**
     * Return a list of all peptide modification combinations from the given peptide FASTA file.
     *
     * @param fastaFile the FASTA file
     * @return a list of {@link Peptide} objects
     * @throws FileNotFoundException
     */
    public List<Peptide> readPeptideFasta(File fastaFile) throws FileNotFoundException {
        List<Peptide> peptides = new ArrayList<>();

        FastaIterator fastaIterator = new FastaIterator(fastaFile);
        Protein protein;
        while ((protein = fastaIterator.getNextProtein()) != null) {
            List<Peptide> peptideModificationCombinations = getPeptideModificationCombinations(protein, protein.getSequence());
            peptides.addAll(peptideModificationCombinations);
        }

        return peptides;
    }

    /**
     * Returns a map of all peptide modification combinations for each protein from the given protein FASTA file.
     *
     * @param fastaFile the FASTA file
     * @return a list of peptides for each protein
     * @throws FileNotFoundException
     * @throws InterruptedException
     */
    public Map<Protein, List<Peptide>> readProteinFasta(File fastaFile) throws FileNotFoundException, InterruptedException {
        Map<Protein, List<Peptide>> proteinPeptides = new HashMap<>();

        FastaIterator fastaIterator = new FastaIterator(fastaFile);
        Protein protein;
        while ((protein = fastaIterator.getNextProtein()) != null) {
            List<ExtendedPeptide> extendedPeptides = digestProtein(protein);
            List<Peptide> peptides = new ArrayList<>();
            for (ExtendedPeptide extendedPeptide : extendedPeptides) {
                List<Peptide> peptideCombinations = getPeptideModificationCombinations(protein, extendedPeptide.peptide.getSequence());
                peptides.addAll(peptideCombinations);
            }
            proteinPeptides.put(protein, peptides);
        }

        return proteinPeptides;
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

    private List<Peptide> getPeptideModificationCombinations(Protein protein, String peptideSequence) {
        List<Peptide> peptides = new ArrayList<>();

        // I'm not sure if this is important
        SequenceProvider sequenceProvider = new SingleProteinSequenceProvider(protein);

        LOGGER.info("Calculating modification combinations for " + peptideSequence);

        List<List<String[]>> singleModificationCombinations = new ArrayList<>();
        List<String[]> uniqueModificationCombinations = new ArrayList<>();
        for (Modification variableModification : variableModifications) {
            List<String[]> modificationSiteCombinations = CombinationUtils.getModificationSiteCombinations(variableModification, protein, peptideSequence);
            singleModificationCombinations.add(modificationSiteCombinations);
            modificationSiteCombinations.forEach(combination -> CombinationUtils.addArrayToList(uniqueModificationCombinations, combination));
        }

        // for each variable modification, combine the possible modifications with the other modification combinations
        // and only keep the unique combinations
        for (int i = 0; i < variableModifications.size(); i++) {
            List<String[]> newCombinations = CombinationUtils.combineModificationCombinations(uniqueModificationCombinations);
            newCombinations.forEach(newCombination -> CombinationUtils.addArrayToList(uniqueModificationCombinations, newCombination));
        }
        for (String[] uniqueModificationCombination : uniqueModificationCombinations) {
            Peptide peptide = new Peptide(peptideSequence);
            for (int i = 0; i < peptide.getSequence().length(); i++) {
                if (uniqueModificationCombination[i] != null) {
                    // check for pyroGlu
                    // TODO right now pyroGlu is on index 0 of the peptide because it's an N-terminal modification, check if this is OK
                    //if(ModificationFactory.getInstance().getModification(uniqueModificationCombination[i]).getModificationType() == ModificationType.modnaa_peptide){
                    if (uniqueModificationCombination[i].equals(Playground.pyroGly.getName())) {
                        peptide.addVariableModification(new ModificationMatch(uniqueModificationCombination[i], i));
                    } else {
                        peptide.addVariableModification(new ModificationMatch(uniqueModificationCombination[i], i + 1));
                    }
                }
            }
            peptide.estimateTheoreticMass(Playground.modificationParameters, sequenceProvider, SequenceMatchingParameters.getDefaultSequenceMatching());
            //System.out.print(peptide.getSequence() + " " + peptide.getMass() + " -> ");
            //System.out.println(Arrays.toString(peptide.getIndexedVariableModifications()));
            peptides.add(peptide);
        }

        LOGGER.info("Found " + peptides.size() + " modification combinations for " + peptideSequence);

        return peptides;
    }

}
