package com.jcs.sbs.serviceTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        JCSComputeTestBasic.class, //test case 1
        JCSComputeTestVariableArguments.class,     //test case 2
        JCSComputeTestNegativeCases.class     //test case 3
})
public class SDKTest {
}
