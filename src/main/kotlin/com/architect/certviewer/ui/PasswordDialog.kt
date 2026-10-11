package com.architect.certviewer.ui

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBPasswordField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent

class PasswordDialog(private val keystoreType: String = "PKCS12") : DialogWrapper(true) {
    private val passwordField = JBPasswordField()
    private var password = CharArray(0)

    init {
        title = "Certificate Password Required"
        init()
    }

    override fun createCenterPanel(): JComponent {
        return FormBuilder.createFormBuilder()
            .addLabeledComponent("Enter password for $keystoreType store:", passwordField)
            .panel
    }

    // Closing the dialog disposes it (and clears the field), so the password
    // must be captured before super.doOKAction() runs.
    override fun doOKAction() {
        password = passwordField.password
        super.doOKAction()
    }

    /**
     * Returns the password entered before OK was pressed. Callers own it and
     * must zero it out once the keystore has been opened.
     */
    fun getPassword(): CharArray = password

    override fun dispose() {
        // Best effort: drop the password from the field's document as well.
        passwordField.text = ""
        super.dispose()
    }
}
