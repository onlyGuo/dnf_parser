package com.xiaoyouma.dnf.parser.pvf.model;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.map.CaseInsensitiveTreeMap;
import cn.hutool.json.JSONObject;
import com.xiaoyouma.dnf.parser.pvf.enums.ScriptType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.xiaoyouma.dnf.parser.pvf.util.PvfHelper.crcDecrypt;
import static com.xiaoyouma.dnf.parser.pvf.util.PvfHelper.getScriptType;
import static com.xiaoyouma.dnf.parser.util.ByteHelper.byteToStr;
import static com.xiaoyouma.dnf.parser.util.ByteHelper.bytesToInt;

/**
 * PVF
 *
 * @author CN
 */
@Data
@Slf4j
public class Pvf {

    /**
     * PVF原始文件字节缓冲
     */
    private ByteBuffer buffer;

    /**
     * pvf文件头
     */
    private PvfHeader pvfHeader;

    /**
     * 文件树字典
     */
    private Map<String, List<PvfFile>> treeDict = new CaseInsensitiveTreeMap<>();

    /**
     * stringtable.bin
     */
    private Map<Integer, String> stringTable;

    /**
     * n_string.lst
     */
    private JSONObject nString;

    /**
     * 字符编码
     */
    private Charset charset;

    public Pvf(String path, Charset charset) throws IOException {
        try (InputStream stream = FileUtil.getInputStream(FileUtil.file(path))) {
            this.buffer = ByteBuffer.allocateDirect(stream.available());
            this.buffer.put(IoUtil.readBytes(stream));
            this.buffer.position(0);
            this.buffer.order(ByteOrder.LITTLE_ENDIAN);

            this.pvfHeader = new PvfHeader(this.buffer);

            this.charset = charset;

            // 加载树
            this.loadTree();

            // 加载 stringtable.bin
            this.loadStringTable();

            // 加载n_string.lst
            this.loadNString();
        }
    }

    /**
     * 加载文件树
     */
    public void loadTree() {
        for (int i = 0; i < pvfHeader.getTreeCount(); i++) {
            int fileNumber = bytesToInt(pvfHeader.getTreeBytes(4));
            int filePathLength = bytesToInt(pvfHeader.getTreeBytes(4));
            String filePath = byteToStr(pvfHeader.getTreeBytes(filePathLength));
            int fileLength = (bytesToInt(pvfHeader.getTreeBytes(4)) + 3) & 0xFFFFFFFC;
            int fileCrc32 = bytesToInt(pvfHeader.getTreeBytes(4));
            int relativeOffset = bytesToInt(pvfHeader.getTreeBytes(4));

            PvfFile pvfFile = PvfFile.builder()
                    .number(fileNumber)
                    .pathLength(filePathLength)
                    .path(filePath)
                    .length(fileLength)
                    .crc32(fileCrc32)
                    .offset(relativeOffset)
                    .build();

            // 添加文件树
            putTreeFile(pvfFile);
        }
    }

    /**
     * 添加文件树
     */
    private void putTreeFile(PvfFile pvfFile) {
        List<PvfFile> pvfFiles;
        String path = pvfFile.getPath();
        String rootFilePath = path.indexOf("/") > 0 ? path.substring(0, path.lastIndexOf("/")) : "/";
        if (treeDict.containsKey(rootFilePath)) {
            pvfFiles = treeDict.get(rootFilePath);
        } else {
            pvfFiles = new ArrayList<>();
            treeDict.put(rootFilePath, pvfFiles);
        }
        pvfFiles.add(pvfFile);
    }

    /**
     * 获取文件树
     */
    private PvfFile getTreeFile(String path) {
        String rootFilePath = path.indexOf("/") > 0 ? path.substring(0, path.lastIndexOf("/")) : "/";
        List<PvfFile> pvfFiles = treeDict.get(rootFilePath);

        if (pvfFiles == null || pvfFiles.isEmpty()) {
            throw new RuntimeException("未找到pvf文件:" + path);
        }

        PvfFile pvfFile = null;

        for (PvfFile file : pvfFiles) {
            if (file.getPath().equalsIgnoreCase(path)) {
                pvfFile = file;
                break;
            }
        }

        if (pvfFile == null) {
            for (PvfFile file : pvfFiles) {
                String treeFileName = FileNameUtil.getName(file.getPath());
                String fileName = FileNameUtil.getName(path);
                if (treeFileName.equalsIgnoreCase("(r)" + fileName) || treeFileName.equalsIgnoreCase("(f)" + fileName)) {
                    pvfFile = file;
                    break;
                }
            }
        }

        return pvfFile;
    }

    /**
     * 是否存在
     *
     * @param path 路径
     */
    public boolean isExist(String path) {
        String rootFilePath = path.indexOf("/") > 0 ? path.substring(0, path.lastIndexOf("/")) : "/";
        List<PvfFile> pvfFiles = treeDict.get(rootFilePath);

        if (pvfFiles == null || pvfFiles.isEmpty()) {
            return false;
        }

        for (PvfFile file : pvfFiles) {
            if (file.getPath().equalsIgnoreCase(path)) {
                return true;
            }
        }

//        if (pvfFile == null) {
//            for (PvfFile file : pvfFiles) {
//                String treeFileName = FileNameUtil.getName(file.getPath());
//                String fileName = FileNameUtil.getName(path);
//                if (treeFileName.equalsIgnoreCase("(r)" + fileName) || treeFileName.equalsIgnoreCase("(f)" + fileName)) {
//                    pvfFile = file;
//                    break;
//                }
//            }
//        }

        return false;
    }

    /**
     * 加载stringtable.bin
     */
    private void loadStringTable() {
        stringTable = getScript("stringtable.bin").toBean(new TypeReference<>() {
        });
    }

    /**
     * 加载n_string.lst
     */
    private void loadNString() {
        nString = new JSONObject(
                getScript("n_string.lst").values()
                        .stream()
                        .collect(Collectors.toMap(str -> str, str -> getScript((String) str)))
        );
    }

    /**
     * 获取树内容
     *
     * @param path 路径
     */
    private byte[] getTreeContent(String path) {
        // 获取文件树
        PvfFile file = getTreeFile(path);
        if (file == null) {
            log.warn("load script {} content fail,can't found the script!", path);
            return null;
        }
        // 读取content
        byte[] content = new byte[file.getLength()];
        buffer.reset();
        buffer.get(buffer.position() + file.getOffset(), content, 0, file.getLength());
        // crc解密content
        crcDecrypt(content, file.getCrc32());
        return content;
    }

    /**
     * 获取脚本内容
     *
     * @param path 路径
     */
    public JSONObject getScript(String path) {
        path = path.toLowerCase();
        ScriptType scriptType = getScriptType(path);
        return scriptType.getParser().convert(this, path, getTreeContent(path));
    }

    /**
     * 获取脚本源码
     *
     * @param path 路径
     */
    public String getScriptSource(String path) {
        path = path.toLowerCase();
        ScriptType scriptType = getScriptType(path);
        return scriptType.getParser().convertSource(this, path, getTreeContent(path));
    }

    /**
     * 获取string table
     *
     * @param key 值
     */
    public String getStringTable(Integer key) {
        return stringTable.get(key);
    }

    /**
     * 获取 n_string（按文件前缀精确匹配）
     *
     * @param filePrefix 文件前缀
     * @param key        值
     */
    public String getNString(String filePrefix, Object key) {
        String prefix = normalizeFilePrefix(filePrefix);
        if (prefix == null) {
            return null;
        }

        for (String fileName : nString.keySet()) {
            if (!fileName.startsWith(prefix + "/")) {
                continue;
            }

            JSONObject object = (JSONObject) nString.get(fileName);
            if (object.containsKey((String) key)) {
                return object.getStr((String) key);
            }
        }
        return null;
    }

    /**
     * 规范化文件前缀
     */
    private String normalizeFilePrefix(String filePrefix) {
        if (filePrefix == null) {
            return null;
        }

        String prefix = filePrefix.replace("\\", "/").toLowerCase().trim();
        int separatorIndex = prefix.indexOf('/');
        if (separatorIndex >= 0) {
            prefix = prefix.substring(0, separatorIndex);
        }

        return prefix.isEmpty() ? null : prefix;
    }

}
