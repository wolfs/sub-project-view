package org.jenkins.plugins.buildstepview;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.tasks.Builder;

import java.io.IOException;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

public class BuildStepViewBuildWrapper extends BuildWrapper {
    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher,
            BuildListener listener) throws IOException, InterruptedException {
        FreeStyleBuild fBuild = (FreeStyleBuild) build;
        List<Builder> builders = fBuild.getProject().getBuilders();
        build.addAction(new BuildStepViewAction(builders));
        return new Environment() {
        };
    }

    @DataBoundConstructor
    public BuildStepViewBuildWrapper() {
    }

    @Extension
    public static class BuildStepViewBuildWrapperDescriptor extends
            BuildWrapperDescriptor {

        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "View Build Steps";
        }

    }
}
