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

    private static final int ITEM_COUNT = 100;
    private final Project mProject;
    private List<AndroidString> mAndroidStrings;
    private List<AndroidString> mSelected = new ArrayList<>();
    private OnConfirmListener mOnConfirmListener;
    private int page;
    private JPanel mPanel;


    public interface OnConfirmListener {
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
        mPanel = new JPanel(new BorderLayout(16, 6));
        setupPanel();
        return mPanel;
    }

    private void setupPanel() {
        final Container container = new Container();
        int fromIndex = page * ITEM_COUNT;
        List<AndroidString> androidStrings = mAndroidStrings.subList(fromIndex, fromIndex + ITEM_COUNT);
        container.setLayout(new GridLayout(androidStrings.size() / 4, 4));

        for (AndroidString androidString : androidStrings) {
            JBCheckBox checkBox = new JBCheckBox(String.format("%s (%s)", androidString.getName(), androidString.getValue()));
            container.add(checkBox);
            checkBox.addItemListener(e -> {
                int state = e.getStateChange();
                if (state == ItemEvent.SELECTED) {
                    mSelected.add(androidString);
                } else {
                    mSelected.remove(androidString);
                }
            });

        }
        mPanel.add(container, BorderLayout.CENTER);

        JPanel pages = new JPanel();
        pages.setLayout(new FlowLayout());
        int pageCount = mAndroidStrings.size() / ITEM_COUNT + 1;
        for (int i = 0; i < pageCount; i++) {
            JButton button = new JButton(String.valueOf(i+1));
            button.setEnabled(page != i);
            final int p = i;
            button.addActionListener(e->{
                page = p;
                mPanel.removeAll();
                setupPanel();
            });
            pages.add(button);
        }

        mPanel.add(pages,BorderLayout.SOUTH);
        mPanel.revalidate();
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
