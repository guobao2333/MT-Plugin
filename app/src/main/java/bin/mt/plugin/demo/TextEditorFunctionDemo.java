package bin.mt.plugin.demo;

import android.text.TextUtils;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import bin.mt.json.JSONObject;
import bin.mt.plugin.api.editor.BaseTextEditorFunction;
import bin.mt.plugin.api.editor.BufferedText;
import bin.mt.plugin.api.editor.TextEditor;
import bin.mt.plugin.api.regex.Matcher;
import bin.mt.plugin.api.regex.MatcherSnapshot;
import bin.mt.plugin.api.regex.Pattern;
import bin.mt.plugin.api.regex.Regex;
import bin.mt.plugin.api.ui.PluginEditText;
import bin.mt.plugin.api.ui.PluginSwitchButton;
import bin.mt.plugin.api.ui.PluginUI;
import bin.mt.plugin.api.ui.PluginView;
import bin.mt.plugin.api.ui.PluginViewGroup;
import bin.mt.plugin.api.ui.dialog.LoadingDialog;
import bin.mt.plugin.api.util.AsyncTask;

public class TextEditorFunctionDemo extends BaseTextEditorFunction {
    @NonNull
    @Override
    public String name() {
        return "{editor:find_and_replace}";
    }

    @Override
    public boolean supportEditTextView() {
        return false;
    }

    @Override
    public boolean supportRepeat() {
        return false;
    }

    @Override
    public PluginView buildOptionsView(@NonNull PluginUI pluginUI, @Nullable JSONObject data) {
        // 提前获取文本范围列表
        List<String> itemList = pluginUI.getContext().getStringList(
                "{editor:selected_text}",
                "{editor:current_line_text}",
                "{editor:text_before_cursor}",
                "{editor:text_after_cursor}",
                "{editor:full_text}"
        );
        // 构建选项View
        return pluginUI.buildVerticalLayout()
                // 查找内容
                .addTextView().text("{editor:find_content}")
                // 查找内容输入框
                .addEditText("find").text(data).singleLine(true).requestFocus()
                // 替换内容
                .addTextView().text("{editor:replace_content}").marginTopDp(10)
                // 替换内容输入框
                .addEditText("replace").text(data).singleLine(true)
                // 区分大小写
                .addSwitchButton("matchCase").text("{editor:match_case}").checked(data).widthMatchParent().marginTopDp(8)
                // 正则表达式
                .addSwitchButton("regex").text("{editor:regex}").checked(data).widthMatchParent().marginTopDp(8)
                .onCheckedChange((buttonView, isChecked) -> {
                    PluginViewGroup rootView = buttonView.getRootView();
                    PluginEditText find = rootView.requireViewById("find");
                    PluginEditText replace = rootView.requireViewById("replace");
                    // 设置正则语法高亮
                    find.setSyntaxHighlight(isChecked ? "Regex" : null);
                    replace.setSyntaxHighlight(isChecked ? "RegexReplacement" : null);
                })
                // 文本范围组
                .addHorizontalLayout().children(builder -> builder
                        // 文本范围
                        .addTextView("label1").text("{editor:text_range}")
                        // 文本范围下拉框
                        .addSpinner("textRange").items(itemList).selection(data).widthMatchParent().marginLeftDp(4)
                )
                // 替换次数组
                .addHorizontalLayout().gravity(Gravity.CENTER).children(builder -> builder
                        // 替换次数
                        .addTextView("label2").text("{editor:replace_count}")
                        // 替换次数输入框
                        .addEditText("replaceCount").text(data).textSize(16)
                        .hint("{editor:replace_count_hint}").inputTypeNumber()
                        .marginLeftDp(4)
                )
                // 让「文本范围」和「替换次数」保持相同宽度
                // 这边虽然它们本来就宽度相同，但是如果翻译成其它语言，宽度可能就不同了
                .unifyWidth("label1", "label2")
                // 完成构建
                .build();
    }

    @Nullable
    @Override
    public JSONObject getOptionsData(@NonNull PluginUI pluginUI, @NonNull PluginView pluginView) {
        PluginEditText findEditText = pluginView.requireViewById("find");
        PluginEditText replaceEditText = pluginView.requireViewById("replace");
        PluginEditText replaceCountEditText = pluginView.requireViewById("replaceCount");
        PluginSwitchButton matchCaseSwitch = pluginView.requireViewById("matchCase");
        PluginSwitchButton regexSwitch = pluginView.requireViewById("regex");
        // 检查输入内容
        if (findEditText.length() == 0) {
            // 必须输入查找内容
            findEditText.requestFocus();
            getContext().showToast("{editor:enter_content}");
            return VALIDATION_FAILED;
        }
        if (regexSwitch.isChecked()) {
            try {
                // 检查正则表达式
                Regex.compile(findEditText.getText().toString());
            } catch (Exception ex) {
                pluginUI.showErrorMessage(ex);
                findEditText.selectAll();
                findEditText.requestFocus();
                return VALIDATION_FAILED;
            }
            try {
                // 检查正则替换模板
                Regex.checkReplacementTemplate(replaceEditText.getText().toString());
            } catch (Exception ex) {
                pluginUI.showErrorMessage(ex);
                replaceEditText.selectAll();
                replaceEditText.requestFocus();
                return VALIDATION_FAILED;
            }
            if (replaceCountEditText.length() > 0) {
                try {
                    // 检查替换次数
                    Integer.parseInt(replaceCountEditText.getText().toString());
                } catch (Exception ex) {
                    pluginUI.showErrorMessage(ex);
                    replaceCountEditText.selectAll();
                    replaceCountEditText.requestFocus();
                    return VALIDATION_FAILED;
                }
            }
        }
        // 保存数据到JSON
        JSONObject data = new JSONObject();
        data.putText(findEditText);
        data.putText(replaceEditText);
        data.putChecked(matchCaseSwitch);
        data.putChecked(regexSwitch);
        data.putText(replaceCountEditText);
        data.putSelection(pluginView.requireViewById("textRange"));
        return data;
    }

    // 我们这里实现的doFunction是个异步任务
    // 用户如果快速重复点击，可能出现多个任务同时在执行，导致出现异常
    // 因此通过此变量来防止出现同时执行的情况
    private boolean doingFunction;

    @Override
    public void doFunction(PluginUI pluginUI, TextEditor editor, @Nullable JSONObject data) {
        if (doingFunction) {
            return;
        }
        Objects.requireNonNull(data);
        String find = data.getString("find");
        String replace = data.getString("replace");
        boolean matchCase = data.getBoolean("matchCase");
        boolean regex = data.getBoolean("regex");
        int textRange = data.getInt("textRange");
        String replaceCountStr = data.getString("replaceCount");
        int replaceCount = replaceCountStr.isEmpty() ? 0 : Integer.parseInt(replaceCountStr);
        int flags = regex ? Pattern.MULTILINE : Pattern.LITERAL;
        if (matchCase) {
            flags |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        }
        Pattern pattern;
        try {
            pattern = Regex.compile(find, flags);
            if (regex) {
                Regex.checkReplacementTemplate(replace);
            }
        } catch (Exception e) {
            pluginUI.showToast(e.toString());
            return;
        }
        BufferedText text = editor.getBufferedText();
        int[] selection = {editor.getSelectionStart(), editor.getSelectionEnd()};
        if (textRange == 0) { // 选中的文本
            if (selection[0] == selection[1]) {
                pluginUI.showToast("{editor:no_text_selected}");
                return;
            }
        } else if (textRange == 1) { // 当前行文本
            selection[0] = TextUtils.lastIndexOf(text, '\n', selection[0] - 1) + 1;
            selection[1] = TextUtils.indexOf(text, '\n', selection[1]);
            if (selection[1] == -1) {
                selection[1] = text.length();
            }
        } else if (textRange == 2) { // 光标前的文本
            selection[1] = selection[0];
            selection[0] = 0;
        } else if (textRange == 3) { // 光标后的文本
            selection[0] = selection[1];
            selection[1] = text.length();
        } else { // 全部文本
            selection = new int[]{0, text.length()};
        }

        boolean backwardReplace = textRange != 2; // 是否向后查找
        Matcher matcher = text.matcher(pattern);
        matcher.region(selection[0], selection[1]);
        ArrayList<MatcherSnapshot> snapshots = new ArrayList<>();

        new AsyncTask(getContext()) {
            LoadingDialog loadingDialog;

            @Override
            protected void beforeThread() throws Exception {
                doingFunction = true;
                loadingDialog = new LoadingDialog(pluginUI)
                        .setMessage("{processing}")
                        // 延迟200毫秒显示处理中对话框，避免因为小文本替换速度太快，导致画面一闪而过的问题
                        .showDelay(200);
            }

            @Override
            protected void onThread() throws Exception {
                if (backwardReplace) {
                    while (matcher.find()) {
                        snapshots.add(matcher.toSnapshot());
                        if (replaceCount > 0 && snapshots.size() == replaceCount) {
                            break;
                        }
                    }
                } else {
                    while (matcher.find()) {
                        snapshots.add(matcher.toSnapshot());
                    }
                    if (replaceCount > 0 && snapshots.size() > replaceCount) {
                        snapshots.subList(0, snapshots.size() - replaceCount).clear();
                    }
                }

                if (regex && !snapshots.isEmpty()) {
                    for (MatcherSnapshot snapshot : snapshots) {
                        snapshot.prepareReplacement(replace);
                    }
                }
            }

            @Override
            protected void afterThread() throws Exception {
                if (!snapshots.isEmpty()) {
                    int finalSelection = backwardReplace ? snapshots.get(snapshots.size() - 1).end() : snapshots.get(0).start();
                    editor.startLargeBatchEditingMode();
                    try {
                        for (int i = snapshots.size() - 1; i >= 0; i--) {
                            MatcherSnapshot snapshot = snapshots.get(i);
                            String replacement = regex ? snapshot.getComputedReplacement() : replace;
                            editor.replaceText(snapshot.start(), snapshot.end(), replacement);
                            if (backwardReplace) {
                                finalSelection = finalSelection - (snapshot.end() - snapshot.start()) + replacement.length();
                            }
                        }
                    } finally {
                        editor.finishLargeBatchEditingMode();
                    }
                    editor.setSelection(finalSelection);
                    editor.pushSelectionToUndoBuffer();
                    editor.requestFocus();
                    editor.ensureSelectionVisible();
                    pluginUI.showToast("{editor:replace_result}", snapshots.size());
                } else {
                    pluginUI.showToast("{editor:text_not_found}");
                }
            }

            @Override
            protected void onException(Exception e) {
                pluginUI.showErrorMessage(e);
            }

            @Override
            protected void onFinally() {
                doingFunction = false;
                if (loadingDialog != null) {
                    loadingDialog.dismiss();
                }
            }
        }.start();

    }
}
