package com.compomics.oglycans;

import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.biology.proteins.Protein;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class CombinationUtils {

    private static final Logger LOGGER = Logger.getLogger(CombinationUtils.class);

    /**
     * Calculate the possible modification combinations for the given modification on the given peptide.
     *
     * @param modification    the modification
     * @param protein         the protein
     * @param peptideSequence the peptide sequence
     * @return the list of possible modification combinations. Each array has the peptide length
     * and contains the modification name on a possible modification site and null anywhere else.
     */
    public static List<String[]> getModificationSiteCombinations(Modification modification, Protein protein, String peptideSequence) {
        List<String[]> modificationSiteCombinations = new ArrayList<>();

        // create a Peptide object because we're dealing with peptides
        Peptide peptide = new Peptide(peptideSequence);

        ArrayList<Integer> potentialModificationSitesNoCombination = peptide.getPotentialModificationSitesNoCombination(modification, protein.getSequence(), 0);
        int[] possibleModificationSites = potentialModificationSitesNoCombination.stream().mapToInt(i -> i).toArray();
        for (int i = 1; i <= potentialModificationSitesNoCombination.size(); i++) {
            List<String[]> possibleModificationSiteCombinations = getCombinationsForModificationOccurrence(peptide.getSequence().length(), modification.getName(), possibleModificationSites, i);
            modificationSiteCombinations.addAll(possibleModificationSiteCombinations);
        }

        return modificationSiteCombinations;
    }

    /**
     * Calculate the the possible modification combinations for the given modification occurrence.
     * For example 3 possible modification [2, 4, 6] sites and 2 modification occurrences should return
     * 3 possible combinations ([2, 4], [2, 6] and [4,6]) as an array of peptide length and the modification name
     * on the sites, null otherwise
     *
     * @param peptideLength             the peptide length
     * @param modificationName          the modification name
     * @param possibleModificationSites the array of possible modification sites
     * @param numberOfModifications     the modification occurrence
     * @return the list of possible combinations
     */
    public static List<String[]> getCombinationsForModificationOccurrence(int peptideLength, String modificationName, int[] possibleModificationSites, int numberOfModifications) {
        List<String[]> combinations = new ArrayList<>();
        Iterator<int[]> iterator = CombinatoricsUtils.combinationsIterator(possibleModificationSites.length, numberOfModifications);
        while (iterator.hasNext()) {
            int[] combination = iterator.next();
            String[] modificationSitesCombination = new String[peptideLength];
            for (int i = 0; i < combination.length; i++) {
                modificationSitesCombination[possibleModificationSites[combination[i]] - 1] = modificationName;
            }
            combinations.add(modificationSitesCombination);
        }

        return combinations;
    }

    /**
     * For each modification combination in the list, combine it with the other modification combinations.
     * For example {[mod1, null, null], [null, mod2, null], [null, null, mod3]} combine to
     * {[mod1, mod2, null], [mod1, null, mod3], [null, mod2, mod3]}
     * IMPORTANT: no mods on the same location are taken into account for the moment!
     *
     * @param modificationCombinations the list of modification combinations
     * @return the list of combined modification combinations
     */
    public static List<String[]> combineModificationCombinations(List<String[]> modificationCombinations) {
        List<String[]> combinedModificationCombinations = new ArrayList<>();

        for (int i = 0; i < modificationCombinations.size(); i++) {
            for (int j = i + 1; j < modificationCombinations.size(); j++) {
                String[] combinedModificationCombination = new String[modificationCombinations.get(i).length];
                String[] firstModificationCombination = modificationCombinations.get(i);
                String[] secondModificationCombination = modificationCombinations.get(j);
                for (int k = 0; k < firstModificationCombination.length; k++) {
                    if (firstModificationCombination[k] != null && secondModificationCombination[k] != null) {
                        if (!firstModificationCombination[k].equals(secondModificationCombination[k])) {
                            LOGGER.warn("2 different modifications on the same location");
                        }
                    }
                    if (firstModificationCombination[k] != null) {
                        combinedModificationCombination[k] = firstModificationCombination[k];
                    }
                    if (secondModificationCombination[k] != null) {
                        combinedModificationCombination[k] = secondModificationCombination[k];
                    }
                }
                combinedModificationCombinations.add(combinedModificationCombination);
            }
        }
        return combinedModificationCombinations;
    }

    /**
     * This method is not used for the moment.
     *
     * @param firstModificationCombinations
     * @param secondModificationCombinations
     * @return
     */
    public static List<String[]> combineModificationCombinations(List<String[]> firstModificationCombinations, List<String[]> secondModificationCombinations) {
        List<String[]> combinedModificationCombinations = new ArrayList<>();

        for (String[] firstModificationCombination : firstModificationCombinations) {
            for (String[] secondModificationCombination : secondModificationCombinations) {
                String[] combinedModificationCombination = new String[firstModificationCombination.length];
                for (int i = 0; i < firstModificationCombination.length; i++) {
                    if (firstModificationCombination[i] != null && secondModificationCombination[i] != null) {
                        System.out.println("-----------");
                    }
                    if (firstModificationCombination[i] != null) {
                        combinedModificationCombination[i] = firstModificationCombination[i];
                    }
                    if (secondModificationCombination[i] != null) {
                        combinedModificationCombination[i] = secondModificationCombination[i];
                    }
                }
                combinedModificationCombinations.add(combinedModificationCombination);
            }
        }

        return combinedModificationCombinations;
    }

    public static void addArrayToList(List<String[]> list, String[] candidate) {
        if (!list.stream().anyMatch(a -> Arrays.equals(a, candidate))) {
            list.add(candidate);
        }
    }
}
