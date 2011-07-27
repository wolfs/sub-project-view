package org.jenkins.plugins.subprojectview;

import hudson.Extension;
import hudson.model.PageDecorator;

/**
 * @author wolfs
 */
@Extension(ordinal = 4)
public class TreeTable extends PageDecorator {
    public TreeTable() {
        super(TreeTable.class);
    }
}
