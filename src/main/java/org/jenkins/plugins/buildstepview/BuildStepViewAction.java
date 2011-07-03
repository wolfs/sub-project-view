package org.jenkins.plugins.buildstepview;

import hudson.Util;
import hudson.model.Action;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.plugins.parameterizedtrigger.TriggerBuilder;
import hudson.tasks.Builder;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class BuildStepViewAction implements Action {

    public class BuilderInfo {
        public String jobName;
        public List<AbstractProject> projects;
    }

    private final List<Builder> builders;

    public BuildStepViewAction(List<Builder> builders) {
        this.builders = builders;
    }

    public String getIconFileName() {
        return "monitor.gif";
    }

    public String getDisplayName() {
        return "Builds running";
    }

    public String getUrlName() {
        return "build-step-view";
    }

    public List<BuilderInfo> getBuilderInfos() {
        ArrayList<BuilderInfo> builderInfos = new ArrayList<BuilderInfo>();
        for (Builder builder : builders) {
            BuilderInfo builderInfo = new BuilderInfo();
            if (builder instanceof TriggerBuilder) {
                TriggerBuilder tBuilder = (TriggerBuilder) builder;

                builderInfo.jobName = tBuilder.getConfigs().iterator().next().getProjects();
                builderInfo.projects = getProjectsForNames(builderInfo.jobName);
            }
            builderInfos.add(builderInfo);
        }
        return builderInfos;
    }

    private List<AbstractProject> getProjectsForNames(String jobNames) {
        ArrayList<AbstractProject> projects = new ArrayList<AbstractProject>();
        StringTokenizer tokens = new StringTokenizer(Util.fixNull(jobNames),",");
        while(tokens.hasMoreTokens()) {
            projects.add(getProjectForName(tokens.nextToken().trim()));
        }
        return projects;
    }

    private AbstractProject getProjectForName(String jobName) {
        List<AbstractProject> items = Hudson.getInstance().getItems(AbstractProject.class);
        for (AbstractProject abstractProject : items) {
            if (jobName.equals(abstractProject.getName())) {
                return abstractProject;
            }
        }
        return null;
    }

}
