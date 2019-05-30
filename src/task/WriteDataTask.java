package task;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import module.AndroidString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import translate.lang.LANG;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WriteDataTask extends Task.Backgroundable {


    private Map<String, List<AndroidString>> mWriteData;

    public WriteDataTask(@Nullable Project project, String title, Map<String, List<AndroidString>> writeData) {
        super(project, title);
        this.mWriteData = writeData;
    }

    @Override
    public void run(@NotNull ProgressIndicator progressIndicator) {
        writeResultData(progressIndicator);
    }

    private void writeResultData(ProgressIndicator progressIndicator) {
        if (mWriteData == null) {
//            translateError(new IllegalArgumentException("No translate data."));
            return;
        }

        Set<String> keySet = mWriteData.keySet();
        for (String key : keySet) {
            File writeFile = getWriteFileForCode(key);
            progressIndicator.setText("Write to " + writeFile.getParentFile().getName() + " data...");
            write(writeFile, mWriteData.get(key));
            refreshAndOpenFile(writeFile);
        }
    }

    private File getWriteFileForCode(String langCode) {
        return getStringFile(langCode, true);
    }

    private VirtualFile getVirtualFile(LANG lang) {
        File file = getStringFile(lang.getCode());
        return LocalFileSystem.getInstance().findFileByIoFile(file);
    }

    private File getStringFile(String langCode) {
        return getStringFile(langCode, false);
    }

    private File getStringFile(String langCode, boolean mkdirs) {
        String parentPath = myProject.getBasePath()+"/TickTick_IN/src/main/res";
        File stringFile;
        if (mkdirs) {
            File parentFile = new File(parentPath, getDirNameForCode(langCode));
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            stringFile = new File(parentFile, "strings.xml");
            if (!stringFile.exists()) {
                try {
                    stringFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            stringFile = new File(parentPath.concat(File.separator).concat(getDirNameForCode(langCode)), "strings.xml");
        }
        return stringFile;
    }

    private String getDirNameForCode(String langCode) {
        String suffix;
        if (langCode.equals(LANG.ChineseSimplified.getCode())) {
            suffix = "zh-rCN";
        } else if (langCode.equals(LANG.ChineseTraditional.getCode())) {
            suffix = "zh-rTW";
        } else if (langCode.equals(LANG.Filipino.getCode())) {
            suffix = "fil";
        } else if (langCode.equals(LANG.Indonesian.getCode())) {
            suffix = "in-rID";
        } else if (langCode.equals(LANG.Javanese.getCode())) {
            suffix = "jv";
        } else {
            suffix = langCode;
        }
        return "values-".concat(suffix);
    }

    private void write(File file, List<AndroidString> androidStrings) {

        ApplicationManager.getApplication().invokeLater(() -> ApplicationManager.getApplication().runWriteAction(() -> {
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
                long length = randomAccessFile.length();
                boolean isNewFile = length == 0;
                int offset = isNewFile ? 0 : "</resources>".length();
                if (isNewFile) {
                    randomAccessFile.write("<resources>\n".getBytes(StandardCharsets.UTF_8));
                } else {
                    randomAccessFile.seek(length - offset);
                }
                for (AndroidString androidString : androidStrings) {
                    randomAccessFile.writeChars("");
                    String value = "\t<string name=\"" + androidString.getName() + "\">" + androidString.getValue() + "</string>";
                    randomAccessFile.write(value.getBytes(StandardCharsets.UTF_8));
                    randomAccessFile.write("\n".getBytes(StandardCharsets.UTF_8));
                }
                randomAccessFile.write("</resources>".getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }));
    }

    private void refreshAndOpenFile(File file) {
        VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
        if (virtualFile != null) {
            ApplicationManager.getApplication().invokeLater(() ->
                    FileEditorManager.getInstance(myProject).openFile(virtualFile, true));
        }
    }
}
