/*
 * MainRelaxedDistance.java
 *
 * Copyright (c) 2019 The Ohio State University
 *
 * This file is distributed under the terms described in LICENSE in the
 * root directory.
 */

public class MainRelaxedDistance extends Main {

    public static void main(String[] args) {
        new MainRelaxedDistance().run(args);
    }

    @Override
    void init() {
        tag = "relaxed";
        // compute a global value that can be used by all local
        // randomizers. the value is in globalLS.

        // option 1: worst case: use |N| from the static graph
        globalLS = 0.5;
        // -1 because the start node cannot ever be removed

//        DiGraph dtree = static_cfg.getDomTree();
//        String[] parts = dir.split("/");
//        dtree.toDot("static_dom_tree" + "_" + parts[parts.length - 1] + ".dot");
    }
}
