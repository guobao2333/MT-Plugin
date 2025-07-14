package bin.mt.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("SdCardPath")
public class PluginPacker {

    public String ADB_PATH;
    public boolean PUSH_MTP_TO_DEVICE;
    public String PLUGIN_ROOT_DIR;
    public boolean LOCAL_DEV;
    public String TARGET_DIR;

    public static void main(String[] args) throws Exception {
        // 获取配置
        ObjectMapper mapper = new ObjectMapper();
        PluginPacker config = mapper.readValue(new File("config.json"), PluginPacker.class);
        
        // 检查配置非法
        if (config.TARGET_DIR == null || config.TARGET_DIR.isEmpty()) {
            throw new IllegalArgumentException("TARGET_DIR cannot be empty!");
        } else if (!config.TARGET_DIR.endsWith("/")) {
            config.TARGET_DIR += "/";
        }

        List<File> outList = new ArrayList<>();
        File rootDir = new File(config.PLUGIN_ROOT_DIR).getAbsoluteFile();

        System.out.println("Packing... ");
        for (File file : Objects.requireNonNull(rootDir.listFiles())) {
            if (file.isDirectory()) {
                outList.add(pack(rootDir, file.getName()));
            }
        }

        if (config.PUSH_MTP_TO_DEVICE) {
            System.out.println("Pushing...");
            for (File outFile : outList) {
                if (config.LOCAL_DEV) {
                    // 本机开发，直接移动
                    File target = new File(config.TARGET_DIR, outFile.getName());
                    Files.move(outFile.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Pushed to " + outFile.getName());
                } else {
                    // 非本机开发，必须有adb
                    File adbFile = new File(config.ADB_PATH);
                    if (!adbFile.exists()) {
                        System.err.println("adb not found at: " + config.ADB_PATH);
                        System.exit(1);
                    }
                    String[] commands = {
                        config.ADB_PATH,
                        "push",
                        outFile.getAbsolutePath(),
                        config.TARGET_DIR + outFile.getName()
                    };
                    Process process = Runtime.getRuntime().exec(commands);
                    int exitCode = process.waitFor();
                    if (exitCode != 0) {
                        System.err.println("Push failed.");
                        System.exit(exitCode);
                    }
                }
            }
        }
        System.out.println("Done.");
    }

    public static File pack(File rootDir, String moduleName) throws Exception {
        System.out.println(">> " + moduleName);
        File srcDir = new File(rootDir, moduleName + "/src/main/java");
        File gradleFile = new File(rootDir, moduleName + "/build.gradle");
        File assetsDir = new File(rootDir, moduleName + "/src/main/assets");
        File manifestFile = new File(rootDir, moduleName + "/src/main/resources/manifest.json");
        File iconFile1 = new File(rootDir, moduleName + "/src/main/resources/icon.png");
        File iconFile2 = new File(rootDir, moduleName + "/src/main/resources/icon.jpg");
        File libsDir = new File("libs");
        File outFile = new File("outputs/" + moduleName + ".mtp");

        //noinspection ResultOfMethodCallIgnored
        outFile.getParentFile().mkdirs();

        String gradleText = new String(Files.readAllBytes(gradleFile.toPath()));

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outFile))) {
            zos.setLevel(Deflater.BEST_COMPRESSION);
            zos.setMethod(ZipOutputStream.DEFLATED);
            addDirectory(zos, srcDir, "src/", true);
            addDirectory(zos, assetsDir, "assets/", false);
            if (gradleText.contains("implementation fileTree(dir: './libs', include: ['*.jar'])") && libsDir.isDirectory()) {
                File[] files = libsDir.listFiles((pathname) -> {
                    if (!pathname.isFile()) {
                        return false;
                    } else {
                        String name = pathname.getName().toLowerCase();
                        return name.endsWith(".jar") && !name.endsWith("-sources.jar");
                    }
                });
                if (files != null) {
                    zos.setMethod(ZipOutputStream.STORED);
                    CRC32 crc32 = new CRC32();
                    for (File file : files) {
                        byte[] data = Files.readAllBytes(file.toPath());
                        crc32.reset();
                        crc32.update(data);
                        ZipEntry entry = new ZipEntry("libs/" + file.getName());
                        entry.setSize(file.length());
                        entry.setCrc(crc32.getValue());
                        zos.putNextEntry(entry);
                        zos.write(data);
                        zos.closeEntry();
                    }
                    zos.setMethod(ZipOutputStream.DEFLATED);
                }
            }
            zos.putNextEntry(new ZipEntry("manifest.json"));
            zos.write(Files.readAllBytes(manifestFile.toPath()));
            zos.closeEntry();
            if (iconFile1.isFile()) {
                zos.putNextEntry(new ZipEntry(iconFile1.getName()));
                zos.write(Files.readAllBytes(iconFile1.toPath()));
                zos.closeEntry();
            } else if (iconFile2.isFile()) {
                zos.putNextEntry(new ZipEntry(iconFile2.getName()));
                zos.write(Files.readAllBytes(iconFile2.toPath()));
                zos.closeEntry();
            }
        }
        return outFile;
    }

    private static void addDirectory(ZipOutputStream zos, File dir, String parentPathInZip, boolean onlyJava) throws IOException {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    addDirectory(zos, file, parentPathInZip + file.getName() + "/", onlyJava);
                } else {
                    String name = file.getName();
                    if (!name.equals(".DS_Store") && !name.toLowerCase().endsWith(".bak") && (!onlyJava || name.toLowerCase().endsWith(".java"))) {
                        addFile(zos, file, parentPathInZip);
                    }
                }
            }
        }
    }

    private static void addFile(ZipOutputStream zos, File file, String parentPathInZip) throws IOException {
        zos.putNextEntry(new ZipEntry(parentPathInZip + file.getName()));
        zos.write(Files.readAllBytes(file.toPath()));
        zos.closeEntry();
    }

}