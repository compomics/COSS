package com.compomics.coss.oglycans;

import com.compomics.oglycans.CombinationUtils;
import com.compomics.oglycans.PeptideGenerator;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationCategory;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.modifications.ModificationType;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.biology.proteins.Protein;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PeptideGeneratorTest {

    private PeptideGenerator peptideGenerator = new PeptideGenerator();

    @Test
    public void testReadPeptideFasta1() throws FileNotFoundException {
        List<Peptide> peptides = peptideGenerator.readPeptideFasta(new File("src/test/resources/oglycans_test_1.fasta"));

        Assert.assertEquals(7, peptides.size());
    }

    @Test
    public void testReadPeptideFasta2() throws FileNotFoundException {
        List<Peptide> peptides = peptideGenerator.readPeptideFasta(new File("src/test/resources/oglycans_test_2.fasta"));

        Assert.assertEquals(7, peptides.size());
    }

}
