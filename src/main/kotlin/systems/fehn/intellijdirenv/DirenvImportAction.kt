package systems.fehn.intellijdirenv

import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import systems.fehn.intellijdirenv.services.DirenvProjectService

class DirenvImportAction : AnAction(MyBundle.message("importDirenvAction")) {
    override fun update(e: AnActionEvent) {
        if (e.project == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        when (e.place) {
            ActionPlaces.MAIN_TOOLBAR -> e.presentation.isEnabledAndVisible = true
            ActionPlaces.PROJECT_VIEW_POPUP -> {

                val psiFile = e.getData(CommonDataKeys.PSI_FILE)
                if (psiFile == null || psiFile.isDirectory || !psiFile.isPhysical) {
                    e.presentation.isEnabledAndVisible = false
                } else {
                    e.presentation.isEnabledAndVisible = psiFile.name.equals(".envrc", ignoreCase = true)
                }
            }

            else -> e.presentation.isEnabledAndVisible = false
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val service = project.getService(DirenvProjectService::class.java)

        when (e.place) {
            ActionPlaces.MAIN_TOOLBAR -> service.projectEnvrcFile?.let {
                service.importDirenv(it)
            } ?: run {
                val notification = notificationGroup
                    .createNotification(
                        MyBundle.message("noTopLevelEnvrcFileFound"),
                        NotificationType.ERROR,
                    )

                Notifications.Bus.notify(notification, project)
            }

            ActionPlaces.PROJECT_VIEW_POPUP -> {
                val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
                service.importDirenv(psiFile.virtualFile)
            }
        }
    }
}
