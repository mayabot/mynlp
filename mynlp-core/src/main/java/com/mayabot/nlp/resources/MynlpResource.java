package com.mayabot.nlp.resources;

import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import com.mayabot.nlp.utils.CharSourceLineReader;

import java.io.IOException;
import java.io.InputStream;

/**
 * 读取的模型是基于文本的。一般一行一个数据。
 * 项目中和外部系统驳接，比如数据库、HDSF
 */
public interface MynlpResource {

    InputStream openInputStream() throws IOException;

    CharSourceLineReader openLineReader() throws IOException;

    /**
     * 有很多实现办法。要么对文件或数据进行计算，还有他同名文件 abc.txt 对应一个文件 abc.txt.hash 进行记录
     *
     * @return
     */
    default String hash() {
        ByteSource byteSource = new ByteSource() {
            @Override
            public InputStream openStream() throws IOException {
                return openInputStream();
            }
        };

        try {
            return byteSource.hash(Hashing.md5()).toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
