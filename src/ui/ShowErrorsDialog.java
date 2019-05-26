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

package ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Select the language dialog you want to convert.
 *
 * @author airsaid
 */
public class ShowErrorsDialog extends DialogWrapper {

    private List<String> mErrors;


    public ShowErrorsDialog(@Nullable Project project,List<String> errors) {
        super(project, false);
        this.mErrors = errors;
        setTitle("这些字符串没有被翻译");
        setResizable(true);
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return doCreateCenterPanel();
    }

    private JComponent doCreateCenterPanel() {
        final JPanel panel = new JPanel(new BorderLayout(16, 6));
        final Container container = new Container();
        container.setLayout(new GridLayout(mErrors.size(), 1));
        for (String error : mErrors) {
            Label label = new Label(error);
            container.add(label);
        }
        panel.add(container, BorderLayout.CENTER);
        return panel;
    }


}
