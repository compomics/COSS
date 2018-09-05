
package com.compomics.coss.controller.preprocessing;

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
