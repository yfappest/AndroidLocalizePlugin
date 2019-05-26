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

package task;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import module.AndroidString;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import translate.lang.LANG;
import translate.querier.Querier;
import translate.trans.AbstractTranslator;
import translate.trans.impl.I18NTranslator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author airsaid
 */
public class TranslateTask extends Task.Backgroundable {

    private List<LANG> mLanguages;
    private List<AndroidString> mAndroidStrings;
    private Map<String, List<AndroidString>> mWriteData;
    private OnTranslateListener mOnTranslateListener;
    private List<String> mErrors = new ArrayList<>();

    public interface OnTranslateListener {
        void onTranslateSuccess(Map<String, List<AndroidString>> data );

        void onTranslateError(Throwable e);
    }

    public TranslateTask(@Nullable Project project, @Nls @NotNull String title, List<LANG> languages,
                         List<AndroidString> androidStrings) {
        super(project, title);
        this.mLanguages = languages;
        this.mAndroidStrings = androidStrings;
        this.mWriteData = new HashMap<>();
    }

    @Override
    public void run(@NotNull ProgressIndicator progressIndicator) {

        Querier<AbstractTranslator> translator = new Querier<>();
        I18NTranslator delegate = new I18NTranslator();
        translator.attach(delegate);
        mWriteData.clear();

        for (LANG toLanguage : mLanguages) {
            progressIndicator.setText("Translating in the " + toLanguage.getEnglishName() + " language...");
            ApplicationManager.getApplication().runReadAction(() -> translate(translator, toLanguage));
        }
        delegate.close();
    }

    private void translate(Querier<AbstractTranslator> translator, LANG toLanguage) {
        List<AndroidString> writeAndroidString = new ArrayList<>();
        for (AndroidString androidString : mAndroidStrings) {
            if (!androidString.isTranslatable()) {
                continue;
            }
            translator.setParams(LANG.Auto, toLanguage, androidString.getValue());
            String resultValue = translator.executeSingle();
            writeAndroidString.add(new AndroidString(androidString.getName(), resultValue, false));
        }
        mWriteData.put(toLanguage.getCode(), writeAndroidString);
    }



    @Override
    public void onSuccess() {
        super.onSuccess();
        translateSuccess();
    }

    @Override
    public void onThrowable(@NotNull Throwable error) {
        super.onThrowable(error);
        translateError(error);
    }

    private void translateSuccess() {
        if (mOnTranslateListener != null) {
            mOnTranslateListener.onTranslateSuccess(mWriteData);
        }
    }

    private void translateError(Throwable error) {
        if (mOnTranslateListener != null) {
            mOnTranslateListener.onTranslateError(error);
        }
    }

    /**
     * Set translate result listener.
     *
     * @param listener callback interface. success or fail.
     */
    public void setOnTranslateListener(OnTranslateListener listener) {
        this.mOnTranslateListener = listener;
    }

    public List<String> getErrors() {
        return mErrors;
    }
}
