
package com.compomics.preprocessing;

/**
 *
 * @author Genet
 */
public abstract class Filter {
    
    /**
     *
     * @param originalData
     * @return
     */
    public abstract double[] filter(double[] originalData );
    
    
    
}
