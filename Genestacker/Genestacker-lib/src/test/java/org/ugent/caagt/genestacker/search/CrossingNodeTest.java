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

package org.ugent.caagt.genestacker.search;

import junit.framework.TestCase;
import org.junit.Test;
import org.ugent.caagt.genestacker.exceptions.ImpossibleCrossingException;

/**
 *
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class CrossingNodeTest extends TestCase {

    public CrossingNodeTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testException() {

        System.out.println("\n### TEST CROSSING NODE CONSTRUCTOR EXCEPTION ###\n");

        // create two dummy plant nodes with different generation
        PlantNode p1 = new PlantNode(null, 1, null);
        PlantNode p2 = new PlantNode(null, 2, null);

        // try to cross, should throw exception
        boolean thrown = false;
        try{
            CrossingNode crossing = new CrossingNode(p1, p2);
        } catch(ImpossibleCrossingException ex){
            thrown = true;
        }
        assertTrue(thrown);

    }

}