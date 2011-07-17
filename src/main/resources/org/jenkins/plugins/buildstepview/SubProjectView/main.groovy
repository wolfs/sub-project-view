package org.jenkins.plugins.buildstepview.SubProjectView

import org.apache.commons.jelly.XMLOutput
import org.dom4j.io.SAXContentHandler

f=namespace(lib.FormTagLib)
l=namespace(lib.LayoutTagLib)
t=namespace("/lib/hudson")
st=namespace("jelly:stapler")
j=namespace("jelly:core")

jsHeader()

if (items.empty) {
  noProjectPage();
} else {
  def columnExtensions = my.columns ?: hudson.model.ListView.defaultColumns
  listView(columnExtensions, items)
}

private def listView(columnExtensions, projects) {
  t.setIconSize()

  div(class: "dashboard") {
    tabBar()
    content(columnExtensions, projects)
  }
  t.iconSize()
  t.rssBar()
}

private def tabBar() {
  context.variables.currentView = my;
  context.variables.views = my.owner.views;
  if (my.owner.class.name == 'hudson.model.MyViewsProperty') {
    st.include(page: "myViewTabs.jelly", 'it': my.owner.myViewsTabBar);
  } else {
    st.include(page: "viewTabs.jelly", 'it': my.owner.viewsTabBar);
  }
}

def content(columnExtensions, projects) {
  table(id:"projectstatus", class:"sortable pane bigtable",
         style: 'margin-top:0px; border-top: none;') {
    showHeaders(columnExtensions)

    def i = 0;
    for (job in projects) {
      def sc = wrapOutput() {
        projectRow(job, columnExtensions)
      }
      addNodeAttribute(sc, i)
      if (i!=0) {
        addChildAttribute(sc, 0)
      }
      raw(sc.getDocument().asXML())
      i++;
    }
  }

}

private def addChildAttribute(SAXContentHandler sc, parent) {
  sc.getDocument().getRootElement().addAttribute("class", "child-of-node-${parent}")
}

private def addNodeAttribute(SAXContentHandler sc, number) {
  sc.getDocument().getRootElement().addAttribute("id", "node-${number}")
}

private def wrapOutput(Closure viewInstructions) {
  def sc = new SAXContentHandler()
  def old = setOutput(new XMLOutput(sc))
  viewInstructions();
  setOutput(old);
  return sc
}

private def projectRow(job, columnExtensions) {
  context.variables.job = job
  context.variables.columnExtensions = columnExtensions
  t.projectViewRow()
}

private def jsHeader() {
  link(href: "${rootURL}/plugin/build-step-view/css/jquery.treeTable.css", rel: "stylesheet", type: "text/css")
  script(src: "${rootURL}/plugin/build-step-view/js/jquery.treeTable.min.js")
  script(type: "text/javascript") {
    text("""jQuery(document).ready(function() {
    jQuery("#projectstatus").treeTable();
  })""")
  }
}


private def noProjectPage() {
  if (!app.items.empty) {
    context.variables.views = my.owner.views;
    context.variables.currentView = my;
    st.include(page: "viewTabs.jelly", it: my.owner.viewsTabBar);
  }
  st.include(page: "noJob.jelly")
}

private def showHeaders(columnExtensions) {
  tr(style: "border-top: 0px;") {
    for (col in columnExtensions) {
      st.include(page: "columnHeader.jelly", 'it': col)
    }
    emptyHead()
  }
}

private def emptyHead() {
  th {
    st.nbsp()
  }
}

