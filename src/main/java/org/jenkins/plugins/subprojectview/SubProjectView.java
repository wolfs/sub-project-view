package org.jenkins.plugins.subprojectview;

import com.google.common.collect.ImmutableList;
import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.model.ListView;
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

    public List<AbstractProject<?,?>> getSubProjects(AbstractProject<?,?> project) {
        List<AbstractProject<?,?>> subProjects = new ArrayList<AbstractProject<?, ?>>();
        if(project instanceof FreeStyleProject) {
            FreeStyleProject proj = (FreeStyleProject) project;
            List<Builder> builders = proj.getBuilders();
            for (Builder builder : builders) {
                if (builder instanceof TriggerBuilder) {
                    TriggerBuilder tBuilder = (TriggerBuilder) builder;
                    for (BlockableBuildTriggerConfig config : tBuilder.getConfigs()) {
                        for (AbstractProject<?,?> abstractProject : config.getProjectList()) {
                            subProjects.add( abstractProject);
                        }
                    }
                }
            }
        }
        return subProjects;
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
