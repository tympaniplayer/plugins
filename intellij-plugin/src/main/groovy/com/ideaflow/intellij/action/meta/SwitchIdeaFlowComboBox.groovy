package com.ideaflow.intellij.action.meta

import com.ideaflow.controller.IFMController
import com.ideaflow.intellij.IdeaFlowApplicationComponent
import com.ideaflow.intellij.action.ActionSupport
import com.ideaflow.intellij.settings.AddNewTaskWizard
import com.ideaflow.model.Task
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.actionSystem.ex.ComboBoxAction
import com.intellij.openapi.project.Project

import javax.swing.*

/**
 * NOTE: all events generated from dynamically created actions seem to have the most recently opened project attached.
 * So, if multiple projects are opened, the activate/open/close actions invoked in one project could refer to the
 * other project when the event is processed.  Sucks but understandable since actions are generallly meant to be
 * instantiated via plugin.xml, not dynamically.  As a workaround, pass the project to the actions instead of
 * relying on the project associated with the event.
 * This applies to all the static inner classes but not to the ComboBoxAction itself since it's created in plugin.xml
 */
@Mixin(ActionSupport)
class SwitchIdeaFlowComboBox extends ComboBoxAction {

	private static class ActivateIdeaFlowAction extends AnAction {

		private static final Icon ACTIVE_ICON = IdeaFlowApplicationComponent.getIcon("ideaflow.png")
		private static final Icon INACTIVE_ICON = IdeaFlowApplicationComponent.getIcon("ideaflow_inactive.png")

		private Project project
		private Task task

		public ActivateIdeaFlowAction(Project project, Task task) {
			this.project = project
			this.task = task

			getTemplatePresentation().setText(task.taskId, false)
			getTemplatePresentation().setDescription("Set ${task.taskId} as Active IdeaFlow")
		}

		public void actionPerformed(final AnActionEvent e) {
			IdeaFlowApplicationComponent.getIFMController().newIdeaFlow(project, task)
		}

		@Override
		void update(AnActionEvent e) {
			super.update(e)

			IFMController controller = IdeaFlowApplicationComponent.getIFMController()
			Task activeTask = controller.activeIdeaFlowModel?.task
			e.presentation.icon = (activeTask == task) ? ACTIVE_ICON : INACTIVE_ICON
		}
	}

	private static class OpenActiveInVisualizerAction extends AnAction {

		private static final Icon BROWSE_ICON = IdeaFlowApplicationComponent.getIcon("browse.png")

		OpenActiveInVisualizerAction() {
			getTemplatePresentation().setText("Open in Visualizer")
			getTemplatePresentation().setDescription("Open the active IdeaFlow in the Visualizer")
			getTemplatePresentation().setIcon(BROWSE_ICON)
		}

		@Override
		void actionPerformed(AnActionEvent event) {
			IFMController controller = IdeaFlowApplicationComponent.getIFMController()
			File activeIfmFile = controller.activeIdeaFlowModel?.file

			if (activeIfmFile) {
				OpenInVisualizerAction.openInBrowser(activeIfmFile)
			}
		}
	}

	private static class AddNewTaskAction extends AnAction {

		private Project project

		AddNewTaskAction(Project project) {

			this.project = project

			getTemplatePresentation().setText("Add new task...")
			getTemplatePresentation().setDescription("Add a new task")
		}

		@Override
		void actionPerformed(final AnActionEvent e) {

			def wizard = new AddNewTaskWizard(project)

			def result = wizard.showAndSaveSettings()

			if (result) {
				IdeaFlowApplicationComponent.getIFMController().newIdeaFlow(project, wizard.task)
			}
		}
	}

	private static class RemoveIdeaFlowAction extends AnAction {

		private static final Icon REMOVE_IDEAFLOW_ICON = IdeaFlowApplicationComponent.getIcon("ideaflow_remove.png")

		private Project project

		public RemoveIdeaFlowAction(Project project) {
			this.project = project
			getTemplatePresentation().setText("Remove from WorkingSet")
			getTemplatePresentation().setDescription("Remove Active IdeaFlow from WorkingSet")
			getTemplatePresentation().setIcon(REMOVE_IDEAFLOW_ICON)
		}

		public void actionPerformed(final AnActionEvent e) {
			IdeaFlowApplicationComponent.getIFMController().closeIdeaFlow(project)
		}
	}


	@Override
	protected DefaultActionGroup createPopupActionGroup(JComponent button) {
		DefaultActionGroup actionGroup = new DefaultActionGroup()
		Project project = PlatformDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext(button))

		if (project != null) {
			IFMController<Project> controller = IdeaFlowApplicationComponent.getIFMController()

			for (Task task : controller.getWorkingSetTasks()) {
				actionGroup.add(new ActivateIdeaFlowAction(project, task))
			}

			actionGroup.addSeparator();
			actionGroup.add(new OpenActiveInVisualizerAction())
			actionGroup.add(new RemoveIdeaFlowAction(project))
			actionGroup.addSeparator();
			actionGroup.add(new AddNewTaskAction(project))
		}
		return actionGroup
	}

	@Override
	public void update(AnActionEvent e) {
		super.update(e)

		IFMController controller = getIFMController(e)
		if (controller) {
			e.presentation.enabled = true
			e.presentation.text = controller.getActiveIdeaFlowName() ?: "Add new task"
		}
	}
}
