/*
 * Copyright 2018 Airsaid. https://github.com/airsaid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package action;


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import logic.LanguageHelper;
import module.AndroidString;
import org.jetbrains.annotations.NotNull;
import task.GetAndroidStringTask;
import task.TranslateTask;
import task.WriteDataTask;
import translate.lang.LANG;
import ui.SelectLanguageDialog;
import ui.SelectStringDialog;
import ui.ShowResultDialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author airsaid
 */
public class ConvertAction extends AnAction implements SelectLanguageDialog.OnClickListener {

    public static final String STRINGS_XML_PATH = "/TickTick/src/main/res/values/strings.xml";
    private Project mProject;
    private VirtualFile mSelectFile;
    private List<AndroidString> mAndroidStrings;
    private static String sPath;

    @Override
    public void actionPerformed(AnActionEvent e) {
        mProject = e.getData(CommonDataKeys.PROJECT);
        sPath = mProject.getBaseDir().getPath() + STRINGS_XML_PATH;
        VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(String.format("file://%s", sPath));
        PsiFile file = PsiManager.getInstance(mProject).findFile(virtualFile);

        GetAndroidStringTask getAndroidStringTask = new GetAndroidStringTask(mProject, "Load strings.xml...", file);
        getAndroidStringTask.setOnGetAndroidStringListener(new GetAndroidStringTask.OnGetAndroidStringListener() {
            @Override
            public void onGetSuccess(@NotNull List<AndroidString> list) {
                if (!isTranslatable(list)) {
                    Messages.showInfoMessage("strings.xml has no text to translate!", "Prompt");
                    return;
                }
                showSelectStringDialog(list);
            }

            @Override
            public void onGetError(@NotNull Throwable error) {
                Messages.showErrorDialog("Load strings.xml error: " + error, "Error");
            }
        });
        getAndroidStringTask.queue();
    }

    private void showSelectStringDialog(List<AndroidString> list) {
        SelectStringDialog dialog = new SelectStringDialog(mProject, list);
        dialog.setOnConfirmListener(selected -> {
            mAndroidStrings = selected;
            showSelectLanguageDialog();
        });
        dialog.show();
    }

    private void showSelectLanguageDialog() {
        SelectLanguageDialog dialog = new SelectLanguageDialog(mProject);
        dialog.setOnClickListener(this);
        dialog.show();
    }


    /**
     * Verify that there is a text in the strings.xml file that needs to be translated.
     *
     * @param list strings.xml text list.
     * @return true: there is text that needs to be translated.
     */
    private boolean isTranslatable(@NotNull List<AndroidString> list) {
        boolean isTranslatable = false;
        for (AndroidString androidString : list) {
            if (androidString.isTranslatable()) {
                isTranslatable = true;
                break;
            }
        }
        return isTranslatable;
    }

    @Override
    public void onClickListener(List<LANG> selectedLanguage) {
        LanguageHelper.saveSelectedLanguage(mProject, selectedLanguage);
        TranslateTask translationTask = new TranslateTask(
                mProject, "In translation...", selectedLanguage, mAndroidStrings);
        translationTask.setOnTranslateListener(new TranslateTask.OnTranslateListener() {
            @Override
            public void onTranslateSuccess(Map<String, List<AndroidString>> data) {
                showResult(data);
            }

            @Override
            public void onTranslateError(Throwable e) {
                Messages.showErrorDialog("Translate error: " + e, "Error");
            }
        });
        translationTask.queue();
    }

    private void showResult(Map<String, List<AndroidString>> data) {
        ShowResultDialog showResultDialog = new ShowResultDialog(mProject, data);
        showResultDialog.setActionClickListener(new ShowResultDialog.ActionClickListener() {
            @Override
            public void onWriteAll(Map<String, List<AndroidString>> list) {
                WriteDataTask writeDataTask = new WriteDataTask(mProject, "", list);
                writeDataTask.queue();
            }

            @Override
            public void onWrite(String key, List<AndroidString> androidStrings) {
                WriteDataTask writeDataTask = new WriteDataTask(mProject, "", new HashMap<String, List<AndroidString>>() {
                    {
                        put(key, androidStrings);
                    }
                });
                writeDataTask.queue();
            }
        });
        showResultDialog.show();
    }

}
