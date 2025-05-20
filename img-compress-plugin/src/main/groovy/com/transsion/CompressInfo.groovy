package com.transsion

import groovy.transform.CompileStatic
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal

@CompileStatic
class CompressInfo implements Serializable {
    private static final long serialVersionUID = 1L

    @Input
    long preSize

    @Input
    long compressedSize

    @Input
    String ratio //压缩比例

    @Input
    String path

    @Input
    String outputPath //输出目录

    @Input
    String md5

    // 添加无参构造函数
    CompressInfo() {}

    CompressInfo(long preSize, long compressedSize, String ratio, String path, String outputPath, String md5) {
        this.preSize = preSize
        this.compressedSize = compressedSize
        this.ratio = ratio
        this.path = path
        this.outputPath = outputPath
        this.md5 = md5
    }

    void update(long presize, long compressedSize, String md5) {
        this.preSize = presize
        this.compressedSize = compressedSize
        this.md5 = md5
        this.ratio = calculateRatio(presize, compressedSize)
    }

    @Internal
    private String calculateRatio(long presize, long compressedSize) {
        return (compressedSize * 100.0F / presize).toInteger() + "%"
    }

    // 添加equals和hashCode方法
    boolean equals(Object o) {
        if (this.is(o)) return true
        if (!(o instanceof CompressInfo)) return false

        CompressInfo that = (CompressInfo) o
        return md5 == that.md5
    }

    int hashCode() {
        return md5.hashCode()
    }

    // 添加toString方法，便于调试
    @Override
    String toString() {
        return "CompressInfo{" +
                "preSize=" + preSize +
                ", compressedSize=" + compressedSize +
                ", ratio='" + ratio + '\'' +
                ", path='" + path + '\'' +
                ", outputPath='" + outputPath + '\'' +
                ", md5='" + md5 + '\'' +
                '}'
    }
}
