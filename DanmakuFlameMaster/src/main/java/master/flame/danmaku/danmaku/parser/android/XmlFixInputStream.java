package master.flame.danmaku.danmaku.parser.android;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 修复不规范 XML 的 InputStream 包装器
 * 主要处理未转义的 & 符号
 */
public class XmlFixInputStream extends FilterInputStream {
    private static final int BUFFER_SIZE = 8192;
    private byte[] buffer = new byte[BUFFER_SIZE];
    private int bufferPos = 0;
    private int bufferLen = 0;
    private boolean eof = false;

    public XmlFixInputStream(InputStream in) {
        super(in);
    }

    @Override
    public int read() throws IOException {
        if (bufferPos >= bufferLen) {
            fillBuffer();
            if (bufferLen == -1) {
                return -1;
            }
        }
        return buffer[bufferPos++] & 0xFF;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        int bytesRead = 0;
        while (bytesRead < len) {
            if (bufferPos >= bufferLen) {
                fillBuffer();
                if (bufferLen == -1) {
                    return bytesRead == 0 ? -1 : bytesRead;
                }
            }
            int available = Math.min(len - bytesRead, bufferLen - bufferPos);
            System.arraycopy(buffer, bufferPos, b, off + bytesRead, available);
            bufferPos += available;
            bytesRead += available;
        }
        return bytesRead;
    }

    private void fillBuffer() throws IOException {
        if (eof) {
            bufferLen = -1;
            return;
        }

        byte[] tempBuffer = new byte[BUFFER_SIZE];
        int read = in.read(tempBuffer);
        if (read == -1) {
            eof = true;
            bufferLen = -1;
            return;
        }

        // 将读取的数据转换为字符串并修复
        String content = new String(tempBuffer, 0, read, "UTF-8");
        
        // 修复未转义的 & 符号（但不要修复已经转义的实体）
        // 这是一个简单的实现，可能需要更复杂的逻辑
        content = fixXmlEntities(content);
        
        byte[] fixedBytes = content.getBytes("UTF-8");
        buffer = fixedBytes;
        bufferLen = fixedBytes.length;
        bufferPos = 0;
    }

    private String fixXmlEntities(String content) {
        // 简单的修复：将未转义的 & 替换为 &amp;
        // 但要避免重复转义已经转义的实体
        StringBuilder result = new StringBuilder();
        int len = content.length();
        
        for (int i = 0; i < len; i++) {
            char c = content.charAt(i);
            if (c == '&') {
                // 检查是否已经是一个实体引用
                boolean isEntity = false;
                if (i + 1 < len) {
                    // 查找下一个分号
                    int semicolonPos = content.indexOf(';', i + 1);
                    if (semicolonPos != -1 && semicolonPos - i < 10) {
                        String entity = content.substring(i + 1, semicolonPos);
                        // 检查是否是有效的实体名称
                        if (entity.matches("[a-zA-Z0-9#]+")) {
                            isEntity = true;
                        }
                    }
                }
                
                if (!isEntity) {
                    result.append("&amp;");
                } else {
                    result.append(c);
                }
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }
}
