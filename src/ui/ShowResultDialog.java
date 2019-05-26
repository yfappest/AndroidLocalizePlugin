package ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import module.AndroidString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShowResultDialog extends DialogWrapper {

    private Map<String, List<AndroidString>> mWriteData;
    private final List<String> mKeys;
    private int index = 0;
    private List<AndroidString> mSelected = new ArrayList<>();
    private ActionClickListener mActionClickListener;
    private JPanel mPanel;

    public interface ActionClickListener {
        void onWriteAll(Map<String, List<AndroidString>> data);

        void onWrite(String lang, List<AndroidString> androidStrings);
    }

    public ShowResultDialog(@Nullable Project project, Map<String, List<AndroidString>> writeData) {
        super(project, false);
        this.mWriteData = writeData;
        mKeys = new ArrayList<>(mWriteData.keySet());
        setTitle(mKeys.get(0));
        setResizable(true);
        setOKButtonText("全部写入");
        setCancelButtonText("取消");
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
        mPanel.removeAll();
        final Container container = new Container();
        String key = mKeys.get(index);
        setTitle(key);
        List<AndroidString> list = mWriteData.get(key);
        if (list.isEmpty()) {
            mPanel.add(new JLabel("没有翻译好的字符串"));
            return;
        }
        container.setLayout(new GridLayout(list.size(), 1));
        mSelected = new ArrayList<>();
        for (AndroidString string : list) {
            JCheckBox checkbox = new JCheckBox("<string name=\"" + string.getName() + "\">" + string.getValue() + "</string>");
            boolean isNotNull = string.getValue() != null;
            checkbox.setEnabled(isNotNull);
            checkbox.setSelected(isNotNull);
            if (isNotNull) {
                checkbox.addItemListener(e -> {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        mSelected.add(string);
                    } else {
                        mSelected.remove(string);
                    }
                });
                mSelected.add(string);
            }
            container.add(checkbox);
        }
        mPanel.add(container, BorderLayout.CENTER);
        mPanel.revalidate();
    }

    @NotNull
    @Override
    protected JPanel createButtonsPanel(@NotNull List<JButton> buttons) {

        JButton next = new JButton("下一个");
        next.addActionListener(e -> next());
        buttons.add(1, next);

        JButton write = new JButton("写入");
        write.addActionListener(e -> write());
        buttons.add(2, write);
        return super.createButtonsPanel(buttons);
    }

    private void write() {
        String key = mKeys.get(index);
        mActionClickListener.onWrite(key, mSelected);
        mWriteData.remove(key);
        mKeys.remove(index);
        if(mKeys.isEmpty()){
            dispose();
        }else {
            setupPanel();
        }
    }

    @Override
    protected void doOKAction() {
        mActionClickListener.onWriteAll(mWriteData);
        dispose();
    }

    public void next() {
        index = ++index % mKeys.size();
        setupPanel();
    }

    public void setActionClickListener(ActionClickListener listener) {
        this.mActionClickListener = listener;
    }
}
