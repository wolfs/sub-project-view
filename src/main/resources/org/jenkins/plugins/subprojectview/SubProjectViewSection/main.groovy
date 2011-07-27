package org.jenkins.plugins.subprojectview.SubProjectViewSection

import org.apache.commons.jelly.XMLOutput
import org.dom4j.io.SAXContentHandler

f=namespace(lib.FormTagLib)
l=namespace(lib.LayoutTagLib)
t=namespace("/lib/hudson")
st=namespace("jelly:stapler")
j=namespace("jelly:core")

//jsHeader()
script(type: "text/javascript") {
  text("""jQuery(document).ready(function() {
  jQuery("#projectstatus2").treeTable({
  clickableNodeNames: true
}  );
}
)""")
}
script(type: "text/javascript") {
 text('jQuery(document).ready(function() { var foo = ')
 st.bind(value: section)
 text(""";
//    var updateBalls = function() {
//      var jobList = new Array();
//      jQuery("td[jobname]").each(function() {
//        var size = jQuery(this).children('img').attr('class');
//        jobList.push({
//          'jobname': this.getAttribute('jobname'),
//          'status': { 'size': size }
//        });
//      });
//      foo.areRunningBalls(jobList, function(t){
//        var result = t.responseObject();
//        jQuery("td[jobname]").each(function() {
//          var jobName = this.getAttribute('jobname');
//          jQuery(this).children('img').each(function() {
//          var ball = result[jobName].status;
//          var imgTag = this;
//          imgTag.alt = ball.description;
//          imgTag.src = ball.image;
//          imgTag.tooltip = ball.description;
//          })
//        });
//        setTimeout(function() {updateBalls()},4000);
//      })
//    }
//    updateBalls();
//  });
var ajaxUpdater = function(spec) {
    var my = {};
    my.jsProxy = spec.jsProxy;
    my.extraArgs = {
        name: 'status',
        extractFunction: function(node) {
            var size = jQuery(node).children('img').attr('class');
            return { 'size': size }
        },
        updateFunction: function(node,status) {
            jQuery(node).children('img').each(function() {
                var imgTag = this;
                imgTag.alt = status.description;
                imgTag.src = status.image;
                imgTag.tooltip = status.description;
            })
        }
    }

    var that = {};

    that.ajaxUpdate = function() {
        var jobList = new Array();
        jQuery("td[jobname]").each(function() {
            var toSend = {
                'jobname': this.getAttribute('jobname')
            }
            toSend[my.extraArgs.name] = my.extraArgs.extractFunction(this);
            jobList.push(toSend);
        });
        my.jsProxy.areRunningBalls(jobList, function(t){
          var result = t.responseObject();
          jQuery("td[jobname]").each(function() {
            var jobname = this.getAttribute('jobname');
            my.extraArgs.updateFunction(this,result[jobname].status);
          });
          setTimeout(function() {that.ajaxUpdate()},4000);
        });
    }
    that.updateStatus = function(node,status) {
    }
    return that;
}
ajaxUpdater({jsProxy:foo}).ajaxUpdate()
});
""")
}
if (section.name.length() > 0) {
  h2(section.name)
}
if(section.items.empty) {
  p("No jobs in this section.")
} else {
  def columnExtensions = section.columns ?: section.defaultColumns
  content(columnExtensions, section.items)
}

private def listView(columnExtensions, projects) {
  t.setIconSize()

  div(class: "dashboard") {
    content(columnExtensions, projects)
  }
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
  table(id:"projectstatus2", class:"pane bigtable") {
    showHeaders(columnExtensions)

    Integer row = 0
    for (job in projects) {
      nodeLabel = addJobNode(job, columnExtensions, row)
      row ++

      def subProjects = section.getSubProjects(job)
      for (subProject in subProjects) {
        addJobNode(subProject,columnExtensions,row) {
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
  sc.document.rootElement.elements().iterator().next().addAttribute("jobName", job.fullName)
  def nodeLabel = job.fullName + "-RowNum" + row
  setNodeAttribute(sc, nodeLabel)
  xmlModifier(sc)
  raw(sc.getDocument().asXML())
  return nodeLabel
}

private def addChildAttribute(SAXContentHandler sc, parent) {
  addAttributes(sc, ["class": "child-of-node-${parent}"])
}

private def addAttributes(SAXContentHandler sc, Map attributes) {
  def rootElement = sc.getDocument().getRootElement()
  for (e in attributes) {
    def oldAttribute = rootElement.attribute(e.key)?.value
    rootElement.addAttribute(e.key, "${e.value} ${oldAttribute}")
  }
}

private def setNodeAttribute(SAXContentHandler sc, number) {
  def rootElement = sc.getDocument().getRootElement()
  rootElement.addAttribute("id","node-${number}")
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

