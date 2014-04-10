//  Copyright 2012 Herman De Beukelaer
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package org.ugent.caagt.genestacker;

/**
 * Represents a genetic map that indicates the distance between any pair of genetic
 * markers on the genotype.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class GeneticMap {
    
    // distances between targets per chromosome, in centimorgans (cM)
    private double[][] distances;
    
    // recombination probabilities inferred from the distance map
    // 3D array:
    //  - first dimension represents different chromosomes
    //  - second and third dimension represent recombination probabilities
    //    between any pair of markers on a specific chromosome
    private double[][][] r;
    
    // map function: distances -> recombination fractions
    private DistanceMapFunction mapFunction;
    
    /**
     * Create a new genetic map with given distances between markers, per
     * chromosome (in cM units), with default Haldane map function.
     * 
     * @param distances 2D array of distances between subsequent loci per chromosome
     */
    public GeneticMap(double[][] distances){
        this(distances, new HaldaneMapFunction());
    }
    
    /**
     * Create a new genetic map with given distances between markers, per
     * chromosome (in cM units).
     * 
     * @param distances 2D array of distances between subsequent loci per chromosome
     * @param mapFunction mapping function applied to convert distances to recombination rates
     */
    public GeneticMap(double[][] distances, DistanceMapFunction mapFunction){
        this.distances = distances;
        this.mapFunction = mapFunction;
        computeRecombinationProbabilities();
    }
    
    /**
     * Compute and store recombination probabilities from given distances.
     */
    private void computeRecombinationProbabilities(){
        r = new double[distances.length][][];
        // loop over chromosomes
        for(int i=0; i<distances.length; i++){
            double[] d = distances[i];
            r[i] = new double[d.length][];
            // loop over all distinct pairs of targets in chromosome
            for(int j=0; j<d.length; j++){
                r[i][j] = new double[j+1];
                for(int k=0; k<=j; k++){
                    // compute total distance between targets
                    double dist = 0;
                    for(int l=k; l<=j; l++){
                        dist += d[l];
                    }
                    // convert distance to probability
                    double pr = mapFunction.computeRecombinationFraction(dist);
                    // store probability
                    r[i][j][k] = pr;
                }
            }
        }
    }
    
    public void setDistanceMapFunction(DistanceMapFunction mapFunction){
        this.mapFunction = mapFunction;
    }
    
    public double[][] getDistances(){
        return distances;
    }
    
    public double[][][] getRecombinationProbabilities(){
        return r;
    }
    
    public double getRecombinationProbability(int chromosomeIndex, int targetLocus1, int targetLocus2){
        if(targetLocus1 == targetLocus2){
            return 0;
        } else {
            int j = Math.max(targetLocus1, targetLocus2)-1;
            int k = Math.min(targetLocus1, targetLocus2);
            return r[chromosomeIndex][j][k];
        }
    }
    
    public int nrOfChromosomes(){
        return distances.length;
    }
    
    public int nrOfLociOnChromosome(int chromIndex){
        return distances[chromIndex].length+1;
    }

    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        for(int i=0; i<distances.length; i++){
            str.append("[");
            for(int j=0; j<distances[i].length; j++){
                str.append(distances[i][j]);
                if(j < distances[i].length-1){
                    str.append(", ");
                }
            }
            str.append("]");
        }
        return str.toString();
    }
    
}
