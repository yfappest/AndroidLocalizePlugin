package ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBCheckBox;
import module.AndroidString;
import org.jetbrains.annotations.Nullable;
import translate.lang.LANG;
import translate.trans.impl.I18NTranslator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;

public class SelectStringDialog extends DialogWrapper {

    private final Project mProject;
    private List<AndroidString> mAndroidStrings;
    private List<AndroidString> mSelected = new ArrayList<>();
    private OnConfirmListener mOnConfirmListener;



    public interface OnConfirmListener{
        void onConfirm(List<AndroidString> selected);
    }

    public SelectStringDialog(@Nullable Project project, List<AndroidString> androidStrings) {
        super(project, false);
        mProject = project;
        mAndroidStrings = androidStrings;
        setTitle("Select Strings");
        setResizable(true);
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        final JPanel panel = new JPanel(new BorderLayout(16, 6));
        final Container container = new Container();
        java.util.List<LANG> supportLanguages = new I18NTranslator().getSupportLang();
        container.setLayout(new GridLayout(supportLanguages.size() / 4, 4));

        for (AndroidString androidString : mAndroidStrings) {
            JBCheckBox checkBox = new JBCheckBox(String.format("%s (%s)", androidString.getName(), androidString.getValue()));
            container.add(checkBox);
            checkBox.addItemListener(e -> {
                int state = e.getStateChange();
                if(state == ItemEvent.SELECTED){
                    mSelected.add(androidString);
                }else {
                    mSelected.remove(androidString);
                }
            });

        }
        panel.add(container, BorderLayout.CENTER);
        return panel;
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
        mOnConfirmListener.onConfirm(mSelected);
    }

    @Override
    public void doCancelAction() {
        super.doCancelAction();
    }

    public void setOnConfirmListener(OnConfirmListener onConfirmListener) {
        this.mOnConfirmListener = onConfirmListener;
    }
}
