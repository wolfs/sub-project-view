package org.jenkins.plugins.subprojectview;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.BallColor;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.model.Saveable;
import hudson.plugins.parameterizedtrigger.BlockableBuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.TriggerBuilder;
import hudson.plugins.sectioned_view.ListViewSection;
import hudson.plugins.sectioned_view.SectionedViewSectionDescriptor;
import hudson.tasks.Builder;
import hudson.util.DescribableList;
import hudson.views.ListViewColumn;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wolfs
 */
public class SubProjectViewSection extends ListViewSection {

    private DescribableList<ListViewColumn,Descriptor<ListViewColumn>> columns;

    @DataBoundConstructor
    public SubProjectViewSection(String name, Width width, Positioning alignment) {
        super(name, width, alignment);
    }

    @Override
    public Iterable<ListViewColumn> getColumns() {
        return columns;
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

    @JavaScriptMethod
    public List<Boolean> areRunning(List<String> jobs) {
        ArrayList<Boolean> result = new ArrayList<Boolean>();
        for (String job : jobs) {
            result.add(Hudson.getInstance().getItemByFullName(job,AbstractProject.class).isBuilding());
        }
        return result;
    }


    @JavaScriptMethod
    public JSONObject areRunningBalls(JSONObject... jobs) {
        JSONObject result = new JSONObject();
        for (JSONObject job : jobs) {
            String jobname = job.getString("jobname");
            JSONObject status = job.getJSONObject("status");
            JSONObject ball = getStatusInfo(jobname, status);
            JSONObject jobResult = new JSONObject();
            jobResult.put("status", ball);
            result.put(jobname, jobResult);
        }
        return result;
    }

    private JSONObject getStatusInfo(String jobname, JSONObject status) {
        BallColor iconColor = Hudson.getInstance().getItemByFullName(jobname, AbstractProject.class).getIconColor();
        String size = status.getString("size").substring(4);
        JSONObject ball = new JSONObject();
        ball.put("image",iconColor.getImageOf(size));
        ball.put("description",iconColor.getDescription());
        return ball;
    }


    @Extension
    public static final class DescriptorImpl extends SectionedViewSectionDescriptor {

        @Override
        public SubProjectViewSection newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            SubProjectViewSection section = (SubProjectViewSection) super.newInstance(req, formData);

            if (section.columns == null) {
                section.columns = new DescribableList<ListViewColumn,Descriptor<ListViewColumn>>(Saveable.NOOP);
            }
            try {
                section.columns.rebuildHetero(req, formData, Hudson.getInstance().<ListViewColumn,Descriptor<ListViewColumn>>getDescriptorList(ListViewColumn.class), "columns");
            } catch (IOException e) {
                throw new FormException("Error rebuilding list of columns.", e, "columns");
            }

            return section;
        }

        @Override
        public String getDisplayName() {
            return "Sub-Project View Section";
        }
    }
}
