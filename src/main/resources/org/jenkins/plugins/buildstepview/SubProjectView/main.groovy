package org.jenkins.plugins.buildstepview.SubProjectView

f=namespace(lib.FormTagLib)
l=namespace(lib.LayoutTagLib)
t=namespace(lib.JenkinsTagLib)
st=namespace("jelly:stapler")
j=namespace("jelly:core")

if (items.size == 0) {
  if (app.items.size !=0) {
    j.set(var:"views", value:it.owner.views);
    j.set(var:"currentView", value:it);
    st.include(page:"viewTabs.jelly",it:it.owner.viewsTabBar);
  }
  st.include(page:"noJob.jelly");
} else {
  h2(items.size)
  def localIt = it;
  def locItems=items
  t.projectView(jobs:locItems, jobBaseUrl:"", showViewTabs:true, columnExtensions:localIt.columns, indenter:localIt.indenter) {
    context.variables.currentView = localIt;
    context.variables.views=localIt.owner.views;
    if (localIt.owner.class.name == 'hudson.model.MyViewsProperty') {
      st.include(page:"myViewTabs.jelly", 'it':localIt.owner?.myViewsTabBar);
    } else {
      st.include(page:"viewTabs.jelly", 'it':localIt.owner.viewsTabBar);
    }
  }
}