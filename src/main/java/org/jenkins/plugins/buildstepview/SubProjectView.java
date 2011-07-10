package org.jenkins.plugins.buildstepview;

import com.google.common.collect.ImmutableList;
import hudson.Extension;
import hudson.Indenter;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.model.ListView;
import hudson.model.TopLevelItem;
import hudson.model.View;
import hudson.model.ViewDescriptor;
import hudson.plugins.parameterizedtrigger.BlockableBuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.TriggerBuilder;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author wolfs
 */
public class SubProjectView extends ListView {

    @DataBoundConstructor
    public SubProjectView(String name) {
        super(name);
    }

    public Collection<View> getViews() {
        return ImmutableList.<View>of(this);
    }

    public AbstractProject<?,?> getBaseProject() {
        return (AbstractProject<?, ?>) getItems().iterator().next();
    }

    public Indenter<AbstractProject<?,?>> getIndenter() {
        return new Indenter<AbstractProject<?, ?>>() {
            @Override
            protected int getNestLevel(AbstractProject<?, ?> job) {
                if (subProjects.contains(job)) {
                    return 1;
                } else {
                    return 0;
                }
            }
        };
    }

    private List<AbstractProject> subProjects;

    @Override
    public List<TopLevelItem> getItems() {
        ArrayList<TopLevelItem> newItems = new ArrayList<TopLevelItem>();
        subProjects = new ArrayList<AbstractProject>();
        List<TopLevelItem> items = super.getItems();
        for (TopLevelItem item : items) {
            newItems.add(item);
            if(item instanceof FreeStyleProject) {
                FreeStyleProject proj = (FreeStyleProject) item;
                List<Builder> builders = proj.getBuilders();
                for (Builder builder : builders) {
                    if (builder instanceof TriggerBuilder) {
                        TriggerBuilder tBuilder = (TriggerBuilder) builder;
                        for (BlockableBuildTriggerConfig config : tBuilder.getConfigs()) {
                            List<AbstractProject> projectList = config.getProjectList();
                            subProjects.addAll(projectList);
                            for (AbstractProject abstractProject : projectList) {
                                if (abstractProject instanceof TopLevelItem) {
                                    newItems.add((TopLevelItem) abstractProject);
                                }
                            }
                        }
                    }
                }
            }
        }
        return newItems;
    }

    @Extension
    public static final class DescriptorImpl extends ViewDescriptor {
        public String getDisplayName() {
            return "Sub Project View";
        }

        /**
         * Checks if the include regular expression is valid.
         */
        public FormValidation doCheckIncludeRegex( @QueryParameter String value ) throws IOException, ServletException, InterruptedException  {
            String v = Util.fixEmpty(value);
            if (v != null) {
                try {
                    Pattern.compile(v);
                } catch (PatternSyntaxException pse) {
                    return FormValidation.error(pse.getMessage());
                }
            }
            return FormValidation.ok();
        }
    }



}
