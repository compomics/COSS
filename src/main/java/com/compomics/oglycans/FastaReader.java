package com.compomics.oglycans;

import com.compomics.util.experiment.biology.enzymes.Enzyme;
import com.compomics.util.experiment.biology.enzymes.EnzymeFactory;
import com.compomics.util.experiment.biology.proteins.Protein;
import com.compomics.util.experiment.identification.protein_sequences.digestion.ExtendedPeptide;
import com.compomics.util.experiment.identification.protein_sequences.digestion.ProteinIteratorUtils;
import com.compomics.util.experiment.identification.protein_sequences.digestion.SequenceIterator;
import com.compomics.util.experiment.identification.protein_sequences.digestion.iterators.SpecificSingleEnzymeIterator;
import com.compomics.util.experiment.io.biology.protein.iterators.FastaIterator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class FastaReader {

    private EnzymeFactory enzymeFactory = EnzymeFactory.getInstance();

    public FastaReader() {
        ArrayList<Enzyme> enzymes = enzymeFactory.getEnzymes();
    }

    public void readFasta(File fastaFile) throws FileNotFoundException, InterruptedException {
        FastaIterator fastaIterator = new FastaIterator(fastaFile);
        ProteinIteratorUtils proteinIteratorUtils = new ProteinIteratorUtils(new ArrayList<>(), 1);

        Protein protein;
        while((protein = fastaIterator.getNextProtein()) != null){
            System.out.println("protein: " + protein.getAccession());

            SequenceIterator sequenceIterator = new SpecificSingleEnzymeIterator(proteinIteratorUtils, protein.getSequence(), enzymeFactory.getEnzyme("Trypsin"), 2, 0.0, 10000.0);
            ExtendedPeptide extendedPeptide;
            while((extendedPeptide = sequenceIterator.getNextPeptide()) != null){
                System.out.println("peptide: " + extendedPeptide.peptide.getSequence());

                pe
            }

            //int[] observableAminoAcids = protein.getObservableAminoAcids(enzymes, 20);

            System.out.println();
        }

        System.out.println("");

    }

}
