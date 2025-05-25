package com.github.tempoden.llmjudge.toolWindow

import com.github.tempoden.llmjudge.backend.Model
import com.github.tempoden.llmjudge.gui.LLMJudgeUICreator

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory

class MyToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(toolWindow: ToolWindow) {
        private lateinit var model: Model

        fun getContent() = JBPanel<JBPanel<*>>().apply {
            val vm = LLMJudgeUICreator.createContent(this)
            model = Model(vm)
        }
    }
}
