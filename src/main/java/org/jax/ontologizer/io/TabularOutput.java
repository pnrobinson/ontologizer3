package org.jax.ontologizer.io;

import org.jax.ontologizer.analysis.GoResult;

import java.util.List;

public class TabularOutput {

    List<String> headerfields;
    List<GoResult> results;

    public TabularOutput(List<String> headerfields, List<GoResult> results) {
        this.headerfields = headerfields;
        this.results = results;
    }

    public void dump() {
        System.out.println(String.join("\t", headerfields));
        for (GoResult gr : results) {
            System.out.println(String.join("\t", gr.getOntologizerRow()));
        }
    }
}
