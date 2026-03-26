package bin.plugin.transcoder;

import android.content.SharedPreferences;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import bin.mt.plugin.api.ui.PluginUI;
import bin.mt.plugin.api.ui.PluginView;

public abstract class Codec extends SpannableString {
    protected final SharedPreferences preferences;

    public Codec(PluginUI pluginUI, CharSequence name) {
        super(name);
        preferences = pluginUI.getContext().getPreferences();
        setSpan(new AbsoluteSizeSpan(pluginUI.sp2px(18)), 0, length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    @Nullable
    public abstract PluginView.OnClickListener getOnOptionsButtonClickListener(PluginUI pluginUI);

    @NonNull
    public abstract String encode(@NonNull String text) throws Exception;

    @NonNull
    public abstract String decode(@NonNull String text) throws Exception;

}
