package org.jenkins.plugins.buildstepview.SubProjectView

import org.apache.commons.jelly.XMLOutput
import org.dom4j.Element
import org.dom4j.io.SAXContentHandler
import org.dom4j.Node

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

    row = 0
    for (job in projects) {
      Map sortColumnValues = [:]
      Integer column = 0
      nodeLabel = addJobNode(job, columnExtensions, row) {
        List<Element> content = it.getDocument().getRootElement().content()
        for (n in content) {
          def prevData = n.attribute('data')
          String data = prevData?.value ?: getTextContents(n)
          sortColumnValues.put(column, data)
          column++
        }
      }
      row++

      def subProjects = my.getSubProjects(job)
      for (subProject in subProjects) {
        column = 0
        addJobNode(subProject,columnExtensions,row) {
          List<Element> content = it.getDocument().getRootElement().content()
          for (n in content) {
            def prevData = n.attribute('data')
            String data = prevData?.value ?: getTextContents(n)

            n.addAttribute('data', sortColumnValues[column] + data)
            column ++
          }
          addChildAttribute(it, nodeLabel)
        }
        row ++
      }
    }
  }

}

private def addJobNode(job, columnExtensions, row, Closure xmlModifier = {}) {
  def sc = wrapOutput() {
    projectRow(job, columnExtensions)
  }
  def nodeLabel = job.fullName + "-RowNum" + row
  addNodeAttribute(sc, nodeLabel)
  xmlModifier(sc)
  raw(sc.getDocument().asXML())
  return nodeLabel
}

private def getTextContents(Node x) {
  if (x.getNodeType() == Node.TEXT_NODE) {
    return x.getText()
  }
  Element el = x;
  String test = ""
  for (subNode in el.content()) {
    switch(subNode.getNodeType()) {
      case Node.ELEMENT_NODE:
        test = test + getTextContents(subNode)
        break
      case Node.TEXT_NODE:
        test = test + x.getText()
        break
    }
  }
  return test
}

private def addChildAttribute(SAXContentHandler sc, parent) {
  addAttributes(sc, ["class": "child-of-node-${parent}"])
}

private def addAttributes(SAXContentHandler sc, Map attributes) {
  for (e in attributes) {
    sc.getDocument().getRootElement().addAttribute(e.key, e.value)
  }
}

private def addNodeAttribute(SAXContentHandler sc, number) {
  addAttributes(sc,["id":"node-${number}"])
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

