package com.architect.certviewer

import com.architect.certviewer.ui.CertificateDetailView
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class ViewerToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val viewer = CertificateDetailView()
        val content = ContentFactory.getInstance().createContent(viewer.content, "", false)
        toolWindow.contentManager.addContent(content)
    }
}
