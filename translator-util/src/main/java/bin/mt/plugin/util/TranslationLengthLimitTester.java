package bin.mt.plugin.util;

import android.os.SystemClock;
import android.text.InputType;

import java.io.IOException;
import java.util.Random;

import bin.mt.plugin.api.preference.PluginPreference;
import bin.mt.plugin.api.ui.PluginEditText;
import bin.mt.plugin.api.ui.PluginUI;
import bin.mt.plugin.api.ui.PluginView;
import bin.mt.plugin.api.ui.dialog.LoadingDialog;
import bin.mt.plugin.api.ui.dialog.PluginDialog;

/**
 * 翻译长度限制测试器。
 * <p>
 * 通过指数探测和二分搜索两个阶段，自动检测翻译引擎单次可翻译的最大字符长度。
 */
public class TranslationLengthLimitTester {

    /**
     * 向插件设置页面添加测试入口。
     *
     * @param builder  设置页面构建器
     * @param function 翻译功能实现
     */
    public static void addToPluginPreference(PluginPreference.Builder builder, TranslationFunction function) {
        builder.addHeader("测试");
        builder.addText("开始测试").summary("测试单次可翻译的最大字符长度").onClick((pluginUI, preferenceItem) -> {
            inputAndStartTest(pluginUI, function);
        });
    }

    /**
     * 弹出输入框让用户输入起始测试长度，校验后启动测试。
     *
     * @param pluginUI 插件UI实例
     * @param function 翻译功能实现
     */
    public static void inputAndStartTest(PluginUI pluginUI, TranslationFunction function) {
        PluginView view = pluginUI.buildVerticalLayout().addEditText("input")
                .singleLine(true).maxLength(5).inputType(InputType.TYPE_CLASS_NUMBER)
                .text("2000")
                .build();
        PluginDialog dg = pluginUI.buildDialog()
                .setTitle("请输入起始测试长度")
                .setView(view)
                .setPositiveButton("{ok}", null)
                .setNegativeButton("{cancel}", null)
                .show();
        PluginEditText editText = view.requireViewById("input");
        editText.requestFocusAndShowIME();
        dg.getPositiveButton().setOnClickListener(v -> {
            String text = editText.getText().toString();
            int length;
            try {
                length = Integer.parseInt(text);
            } catch (NumberFormatException e) {
                pluginUI.showToast("数字格式错误");
                return;
            }
            if (length <= 0 || length % 100 != 0) {
                pluginUI.showToast("请填写 100 的整数倍数字");
                return;
            }
            dg.dismiss();
            startTest(pluginUI, length, function);
        });
    }

    /**
     * 启动翻译长度限制测试。
     * <p>
     * 阶段1：从 initLength 开始指数倍增探测，找到翻译失败的上界。
     * 阶段2：在成功下界和失败上界之间二分搜索，精确定位最大可翻译长度（精度100字符）。
     *
     * @param pluginUI   插件UI实例
     * @param initLength 起始测试长度，需为100的整数倍
     * @param function   翻译功能实现
     */
    public static void startTest(PluginUI pluginUI, int initLength, TranslationFunction function) {
        LoadingDialog loadingDialog = new LoadingDialog(pluginUI).show();
        new Thread(() -> {
            try {
                // 阶段1：指数探测，找到失败的上界
                int low = 0;
                int high = -1;
                int length = initLength;
                int maxLimit = 100000;

                while (length <= maxLimit) {
                    loadingDialog.setMessage("指数探测：" + length);
                    if (testTranslate(pluginUI, length, function)) {
                        pluginUI.getContext().log("测试成功，长度 = " + length);
                        low = length;
                        length *= 2;
                        length = length / 100 * 100;
                    } else {
                        pluginUI.getContext().log("测试失败，长度 = " + length);
                        high = length;
                        break;
                    }
                }

                if (high == -1) {
                    pluginUI.showToast("在 " + maxLimit + " 范围内未找到上限");
                    loadingDialog.dismiss();
                    return;
                }

                // 阶段2：二分搜索精确定位
                while (high - low > 100) {
                    int mid = (low + high) / 2;
                    mid = mid / 100 * 100;
                    if (mid == low) mid = low + 100;
                    if (mid == high) break;

                    loadingDialog.setMessage("二分搜索：" + mid);
                    loadingDialog.setSecondaryMessage("范围 [" + low + ", " + high + "]");
                    if (testTranslate(pluginUI, mid, function)) {
                        pluginUI.getContext().log("测试成功，长度 = " + mid);
                        low = mid;
                    } else {
                        pluginUI.getContext().log("测试失败，长度 = " + mid);
                        high = mid;
                    }
                }

                String result = "最大可翻译长度：" + low;
                pluginUI.getContext().log(result);
                pluginUI.showToastL(result);
            } finally {
                loadingDialog.dismiss();
                pluginUI.getContext().openLogViewer();
            }
        }).start();
    }

    /**
     * 测试指定长度的文本是否能被正确翻译。
     * <p>
     * 生成指定长度的随机英文文本进行翻译，通过比较原文与译文的行数来判断翻译是否完整。
     * 失败时最多重试一次。
     *
     * @param pluginUI 插件UI实例，用于日志输出
     * @param length   待测试的文本长度
     * @param function 翻译功能实现
     * @return 翻译成功返回 true，失败返回 false
     */
    private static boolean testTranslate(PluginUI pluginUI, int length, TranslationFunction function) {
        for (int i = 0; i < 2; i++) {
            try {
                SystemClock.sleep(500);
                String original = getText(length);
                String translated = function.translate(original);
                pluginUI.getContext().log("testTranslate:\n" + translated);
                return countLine(original.trim()) == countLine(translated.trim());
            } catch (Exception e) {
                pluginUI.getContext().log("testTranslate", e);
            }
        }
        return false;
    }

    /**
     * 统计字符串中的换行符数量。
     *
     * @param s 输入字符串
     * @return 换行符数量
     */
    private static int countLine(String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\n') count++;
        }
        return count;
    }

    private static final String[] SENTENCES = {
            "On quiet nights, I map tomorrow with coffee, courage, and one stubborn little star above my window.\n",
            "The sun rises, birds sing on the tree.\nI drink my coffee, read a good book, and wait by the window.\n",
            "She waved goodbye.\nAn old train just left.\nHe stood alone on the cold, empty platform, watching it.\n",
            "Apples sat on a table, some kids played outside, a warm summer breeze blew slowly through the door.\n",
            "He fixed the old clock, she painted the big wall.\nThey shared warm bread and hot tea before sunset.\n",
            "Cold wind blew hard.\nDry leaves fell down.\nI grabbed my warm coat and bag, then walked off to work.\n",
            "The moon rose so high, owls called in the dark woods, we sat by the fire and told jokes until dawn.\n",
            "Waves hit the warm shore, sand felt warm under my feet.\nKids built a castle while seagulls flew by.\n",
    };

    private static final Random RANDOM = new Random();

    /**
     * 生成指定长度的随机英文测试文本。
     * <p>
     * 从预定义的句子库中随机选取句子拼接，每条句子约100字符。
     *
     * @param length 目标文本长度，需为100的整数倍
     * @return 拼接后的测试文本
     */
    private static String getText(int length) {
        assert length % 100 == 0;
        StringBuilder sb = new StringBuilder(length);
        int count = length / 100;
        for (int i = 0; i < count; i++) {
            sb.append(SENTENCES[RANDOM.nextInt(SENTENCES.length)]);
        }
        return sb.toString();
    }

    /**
     * 翻译功能的函数式接口，用于将具体的翻译实现传入测试器。
     */
    public interface TranslationFunction {

        /**
         * 将文本翻译为目标语言。
         *
         * @param text 待翻译的原文（英文）
         * @return 翻译后的文本
         * @throws IOException 翻译请求失败时抛出
         */
        String translate(String text) throws IOException;

    }

}
