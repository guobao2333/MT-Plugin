package bin.mt.plugin.demo.examples;

import java.util.HashSet;
import java.util.Set;

import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.drawable.MaterialIcons;
import bin.mt.plugin.api.preference.PluginPreference;
import bin.mt.plugin.api.ui.PluginView;
import bin.mt.plugin.api.ui.dialog.PluginDialog;
import bin.mt.plugin.api.ui.menu.PluginMenu;
import bin.mt.plugin.api.ui.menu.PluginPopupMenu;
import bin.mt.plugin.api.ui.menu.PluginSubMenu;

public class ExamplePopupMenu implements PluginPreference {

    @Override
    public void onBuild(PluginContext context, Builder builder) {
        builder.title("弹出菜单");

        // 菜单点击事件
        PluginMenu.OnMenuItemClickListener commonListener = menuItem -> {
            if (!menuItem.hasSubMenu()) { // 避免展开子菜单时也会弹出提示
                context.showToast(String.format("点击了 id:%s title:%s", menuItem.getItemId(), menuItem.getTitle()));
            }
            return true;
        };

        builder.addText("基本用法").summary("basic").onClick((pluginUI, item) -> {
            PluginView.OnClickListener listener = button -> {
                // 创建弹出菜单
                PluginPopupMenu popupMenu = pluginUI.createPopupMenu(button);
                PluginMenu menu = popupMenu.getMenu();
                // 添加菜单，分别指定id和标题
                menu.add("{menu1}", "菜单1");
                // 添加菜单，同时指定id和标题，标题来自语言包
                menu.add("{menu2}");
                // 添加菜单，同时指定id和标题
                menu.add("菜单3");
                popupMenu.setOnMenuItemClickListener(commonListener);
                popupMenu.show();
            };
            PluginView view = pluginUI.buildFrameLayout()
                    .addButton().text("菜单1").onClick(listener) // 点击普通按钮后弹出菜单
                    .build();
            PluginDialog dialog = pluginUI.buildDialog()
                    .setTitle(item.getTitle())
                    .setView(view)
                    .setPositiveButton("{close}", null)
                    .setNeutralButton("菜单2", null)
                    .show();
            dialog.getNeutralButton().setOnClickListener(listener); // 点击对话框按钮后弹出菜单
        });

        builder.addText("菜单图标").summary("icon").onClick((pluginUI, item) -> {
            PluginDialog dialog = pluginUI.buildDialog()
                    .setTitle(item.getTitle())
                    .setPositiveButton("{close}", null)
                    .setNeutralButton("点击这里", null)
                    .show();
            dialog.getNeutralButton().setOnClickListener(view -> {
                PluginPopupMenu popupMenu = pluginUI.createPopupMenu(view);
                PluginMenu menu = popupMenu.getMenu();
                menu.add("{menu1}").setIcon(MaterialIcons.get("search"));
                menu.add("{menu2}").setIcon(MaterialIcons.get("content_copy"));
                menu.add("{menu3}").setIcon(MaterialIcons.get("content_cut"));
                menu.add("{menu4}").setIcon(MaterialIcons.get("delete"));
                popupMenu.setOnMenuItemClickListener(commonListener);
                popupMenu.show();
            });
        });

        builder.addText("多选菜单").summary("checkable").onClick((pluginUI, item) -> {
            PluginDialog dialog = pluginUI.buildDialog()
                    .setTitle(item.getTitle())
                    .setPositiveButton("{close}", null)
                    .setNeutralButton("点击这里", null)
                    .show();
            Set<String> checkedItems = new HashSet<>();
            dialog.getNeutralButton().setOnClickListener(view -> {
                PluginPopupMenu popupMenu = pluginUI.createPopupMenu(view);
                PluginMenu menu = popupMenu.getMenu();
                menu.add("{menu1}").setCheckable(true).setChecked(checkedItems.contains("{menu1}"));
                menu.add("{menu2}").setCheckable(true).setChecked(checkedItems.contains("{menu2}"));
                menu.add("{menu3}").setCheckable(true).setChecked(checkedItems.contains("{menu3}"));
                menu.add("{menu4}").setCheckable(true).setChecked(checkedItems.contains("{menu4}"));
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    if (menuItem.isChecked()) {
                        checkedItems.remove(menuItem.getItemId());
                        menuItem.setChecked(false);
                    } else {
                        checkedItems.add(menuItem.getItemId());
                        menuItem.setChecked(true);
                    }
                    return true;
                });
                popupMenu.show();
            });
        });

        builder.addText("单选菜单").summary("checkable & exclusive").onClick((pluginUI, item) -> {
            PluginDialog dialog = pluginUI.buildDialog()
                    .setTitle(item.getTitle())
                    .setPositiveButton("{close}", null)
                    .setNeutralButton("点击这里", null)
                    .show();
            String[] checkId = new String[]{"{menu1}"};
            dialog.getNeutralButton().setOnClickListener(view -> {
                PluginPopupMenu popupMenu = pluginUI.createPopupMenu(view);
                PluginMenu menu = popupMenu.getMenu();
                menu.add("{menu1}", "{menu1}", "group0").setChecked(checkId[0].equals("{menu1}"));
                menu.add("{menu2}", "{menu2}", "group0").setChecked(checkId[0].equals("{menu2}"));
                menu.add("{menu3}", "{menu3}", "group0").setChecked(checkId[0].equals("{menu3}"));
                menu.add("{menu4}", "{menu4}", "group0").setChecked(checkId[0].equals("{menu4}"));
                menu.setGroupCheckable("group0", true, true);
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    menuItem.setChecked(true);
                    checkId[0] = menuItem.getItemId();
                    return true;
                });
                popupMenu.show();
            });
        });

        builder.addText("多级菜单").summary("subMenu").onClick((pluginUI, item) -> {
            PluginDialog dialog = pluginUI.buildDialog()
                    .setTitle(item.getTitle())
                    .setPositiveButton("{close}", null)
                    .setNeutralButton("点击这里", null)
                    .show();
            dialog.getNeutralButton().setOnClickListener(view -> {
                PluginPopupMenu popupMenu = pluginUI.createPopupMenu(view);
                PluginMenu menu = popupMenu.getMenu();

                PluginSubMenu subMenu1 = menu.addSubMenu("{menu1}");
                subMenu1.add("sub1_0", "{item0}");
                subMenu1.add("sub1_1", "{item1}");
                subMenu1.add("sub1_2", "{item2}");
                subMenu1.add("sub1_3", "{item3}");

                PluginSubMenu subMenu2 = menu.addSubMenu("{menu2}");
                subMenu2.add("sub2_0", "{item0}");
                subMenu2.add("sub2_1", "{item1}");
                subMenu2.add("sub2_2", "{item2}");
                subMenu2.add("sub2_3", "{item3}");

                menu.add("{menu3}");
                menu.add("{menu4}");

                popupMenu.setOnMenuItemClickListener(commonListener);
                popupMenu.show();
            });
        });

        builder.addText("分割线").summary("divider").onClick((pluginUI, item) -> {
            PluginDialog dialog = pluginUI.buildDialog()
                    .setTitle(item.getTitle())
                    .setPositiveButton("{close}", null)
                    .setNeutralButton("点击这里", null)
                    .show();
            dialog.getNeutralButton().setOnClickListener(view -> {
                PluginPopupMenu popupMenu = pluginUI.createPopupMenu(view);
                PluginMenu menu = popupMenu.getMenu();

                // 不同group之间会有分割线
                menu.add("{menu1}");
                menu.add("{menu2}");
                menu.add("{menu3}", "{menu3}", "group1");
                menu.add("{menu4}", "{menu4}", "group1");
                menu.setGroupDividerEnabled(true);

                popupMenu.setOnMenuItemClickListener(commonListener);
                popupMenu.show();
            });
        });

    }
}
