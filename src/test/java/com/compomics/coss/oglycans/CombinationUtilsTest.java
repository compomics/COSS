package com.compomics.coss.oglycans;

import com.compomics.oglycans.CombinationUtils;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationCategory;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.modifications.ModificationType;
import com.compomics.util.experiment.biology.proteins.Protein;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CombinationUtilsTest {

    private static ModificationFactory modificationFactory = ModificationFactory.getInstance();

    @BeforeClass
    public static void setUpClass() throws Exception {
        // add O-glycans mod with TMT label to ModificationFactory
        ArrayList<String> residues = new ArrayList<>();
        residues.add("S");
        residues.add("T");
        Modification oglycan = new Modification(ModificationType.modaa, "oglycans", 503.3, residues, ModificationCategory.Common);
        modificationFactory.addUserModification(oglycan);
    }

    @Test
    public void testCombineModificationCombinations() {
        List<String[]> modificationCombinations = new ArrayList<>();
        modificationCombinations.add(new String[]{"mod1", null, null});
        modificationCombinations.add(new String[]{null, "mod2", null});
        modificationCombinations.add(new String[]{null, null, "mod3"});

        List<String[]> combinations = CombinationUtils.combineModificationCombinations(modificationCombinations);
        Assert.assertEquals(3, combinations.size());
        Assert.assertTrue(combinations.stream().anyMatch(a -> Arrays.equals(a, new String[]{"mod1", "mod2", null})));
        Assert.assertTrue(combinations.stream().anyMatch(a -> Arrays.equals(a, new String[]{"mod1", null, "mod3"})));
        Assert.assertTrue(combinations.stream().anyMatch(a -> Arrays.equals(a, new String[]{null, "mod2", "mod3"})));
    }

    @Test
    public void testGetCombinationsForModificationOccurrence() {
        List<String[]> possibleModificationSiteCombinations = CombinationUtils.getCombinationsForModificationOccurrence(8, "mod", new int[]{2, 4, 6}, 0);
        Assert.assertEquals(1, possibleModificationSiteCombinations.size());
        Assert.assertTrue(possibleModificationSiteCombinations.stream().anyMatch(a -> Arrays.equals(a, new String[]{null, null, null, null, null, null, null, null})));

        possibleModificationSiteCombinations = CombinationUtils.getCombinationsForModificationOccurrence(8, "mod", new int[]{2, 4, 6}, 1);

        Assert.assertEquals(3, possibleModificationSiteCombinations.size());
        Assert.assertTrue(possibleModificationSiteCombinations.stream().anyMatch(a -> Arrays.equals(a, new String[]{null, "mod", null, null, null, null, null, null})));
        Assert.assertTrue(possibleModificationSiteCombinations.stream().anyMatch(a -> Arrays.equals(a, new String[]{null, null, null, null, null, "mod", null, null})));
        Assert.assertTrue(possibleModificationSiteCombinations.stream().anyMatch(a -> Arrays.equals(a, new String[]{null, null, null, "mod", null, null, null, null})));

        possibleModificationSiteCombinations = CombinationUtils.getCombinationsForModificationOccurrence(8, "mod", new int[]{2, 4, 6}, 2);

        Assert.assertEquals(3, possibleModificationSiteCombinations.size());
        Assert.assertTrue(possibleModificationSiteCombinations.stream().anyMatch(a -> Arrays.equals(a, new String[]{null, "mod", null, "mod", null, null, null, null})));
        Assert.assertTrue(possibleModificationSiteCombinations.stream().anyMatch(a -> Arrays.equals(a, new String[]{null, "mod", null, null, null, "mod", null, null})));
        Assert.assertTrue(possibleModificationSiteCombinations.stream().anyMatch(a -> Arrays.equals(a, new String[]{null, null, null, "mod", null, "mod", null, null})));

        possibleModificationSiteCombinations = CombinationUtils.getCombinationsForModificationOccurrence(8, "mod", new int[]{2, 4, 6}, 3);

        Assert.assertEquals(1, possibleModificationSiteCombinations.size());
        Assert.assertTrue(possibleModificationSiteCombinations.stream().anyMatch(a -> Arrays.equals(a, new String[]{null, "mod", null, "mod", null, "mod", null, null})));
    }

    @Test
    public void testGetModificationSiteCombinations() {
        Protein protein = new Protein("TEST", "TEST");
        List<String[]> combinations = CombinationUtils.getModificationSiteCombinations(modificationFactory.getModification("oglycans"), protein, "TEST");
        Assert.assertEquals(8, combinations.size());
        Assert.assertTrue(combinations.stream().anyMatch(a -> Arrays.equals(a, new String[]{null, null, null, null})));
        Assert.assertTrue(combinations.stream().anyMatch(a -> Arrays.equals(a, new String[]{"oglycans", null, null, null})));
        Assert.assertTrue(combinations.stream().anyMatch(a -> Arrays.equals(a, new String[]{null, null, "oglycans", null})));
        Assert.assertTrue(combinations.stream().anyMatch(a -> Arrays.equals(a, new String[]{null, null, null, "oglycans"})));
        Assert.assertTrue(combinations.stream().anyMatch(a -> Arrays.equals(a, new String[]{"oglycans", null, "oglycans", null})));
        Assert.assertTrue(combinations.stream().anyMatch(a -> Arrays.equals(a, new String[]{"oglycans", null, null, "oglycans"})));
        Assert.assertTrue(combinations.stream().anyMatch(a -> Arrays.equals(a, new String[]{null, null, "oglycans", "oglycans"})));
        Assert.assertTrue(combinations.stream().anyMatch(a -> Arrays.equals(a, new String[]{"oglycans", null, "oglycans", "oglycans"})));
    }

}
