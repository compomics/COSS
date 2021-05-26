package com.compomics.coss.oglycans;

import com.compomics.oglycans.CombinationUtils;
import com.compomics.oglycans.PeptideGenerator;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationCategory;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.modifications.ModificationType;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.biology.proteins.Protein;
import com.compomics.util.experiment.identification.protein_sequences.SingleProteinSequenceProvider;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.parameters.identification.search.ModificationParameters;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PeptideGeneratorTest {

    private PeptideGenerator peptideGenerator;

    @Before
    public void initialize() {
        List<Modification> variableModifications = new ArrayList<>();

        Modification oxidation = ModificationFactory.getInstance().getModification("Oxidation of M");
        Modification pyroGly = ModificationFactory.getInstance().getModification("Pyrolidone from E");
        // add O-glycans mod with TMT label to ModificationFactory
        ArrayList<String> residues = new ArrayList<>();
        residues.add("S");
        residues.add("T");
        Modification oglycan = new Modification(ModificationType.modaa, "oglycans", 503.3, residues, ModificationCategory.Common);
        ModificationFactory.getInstance().addUserModification(oglycan);

        variableModifications.add(oglycan);
        variableModifications.add(oxidation);
        variableModifications.add(pyroGly);

        peptideGenerator = new PeptideGenerator(variableModifications);
    }

    @Test
    public void testReadPeptideFasta1() throws FileNotFoundException {
        List<Peptide> peptides = peptideGenerator.readPeptideFasta(new File("src/test/resources/oglycans_test_1.fasta"));

        Assert.assertEquals(7, peptides.size());

        // check the if a fixed modification is taken into account as well
        ModificationParameters modificationParameters = new ModificationParameters();
        Modification carbo = ModificationFactory.getInstance().getModification("Carbamidomethylation of C");
        modificationParameters.addFixedModification(carbo);
        Peptide peptide = peptides.get(0);
        SequenceProvider sequenceProvider = new SingleProteinSequenceProvider(new Protein("TESTACCESSION", peptide.getSequence()));
        String[] fixedModifications = peptide.getFixedModifications(modificationParameters, sequenceProvider, SequenceMatchingParameters.getDefaultSequenceMatching());
        Assert.assertEquals("Carbamidomethylation of C", fixedModifications[4]);
    }

    @Test
    public void testReadPeptideFasta2() throws FileNotFoundException {
        List<Peptide> peptides = peptideGenerator.readPeptideFasta(new File("src/test/resources/oglycans_test_2.fasta"));

        Assert.assertEquals(7, peptides.size());
    }

}
