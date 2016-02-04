import org.jppf.management.*
import org.jppf.ui.monitoring.data.*
import org.jppf.node.policy.*
import org.jppf.ui.utils.*
import java.awt.*

void init() {
  def editor = option.findFirstWithName("/ExecutionPolicy")
  def text = editor.getValue()
  if ((text == null) || text.trim().isEmpty()) editor.setValue(GuiUtils.DEFAULT_EMPTY_FILTER)
  apply()
}

void apply() {
  def activeOption = option.findFirstWithName("/ActivateFilter")
  def b = activeOption.getValue()
  def manager = StatsHandler.getInstance().getTopologyManager()
  if (!b) {
    manager.setNodeFilter(null)
  } else {
    def text = option.findFirstWithName("/ExecutionPolicy").getValue()
    if ((text != null) && !text.trim().isEmpty()) {
      try {
        def policy = PolicyParser.parsePolicy(text)
        def selector = new ExecutionPolicySelector(policy)
        manager.setNodeFilter(selector)
      } catch(Exception e) {
        e.printStackTrace()
      }
    } else manager.setNodeFilter(null)
  }
  updateTabColor(activeOption)
}

void loadOrSave(loadFlag) {
  def editor = option.findFirstWithName("/ExecutionPolicy")
  def file = option.getValue()
  if (loadFlag) editor.setValue(FileUtils.readTextFile(file));
  else FileUtils.writeTextFile(file, editor.getValue())
}

void updateTabColor(activeOption) {
  def label = GuiUtils.getTabComponent(activeOption)
  if (label != null) {
    if (activeOption.getValue()) label.setText("<html>Filter <font color=\"green\">on </font</html>")
    else label.setText("<html>Filter <font color=\"red\">off</font</html>")
  }
}