package com.transsion.compressor


import com.transsion.ResultInfo
import com.transsion.CompressInfo
import com.transsion.ImgCompressExtension
import org.gradle.api.Project

/**
 * 压缩处理器抽象接口,有多种类型
 */
public interface ICompressor {

    void compress(Project rootProject, List<CompressInfo> unCompressFileList, ImgCompressExtension config, ResultInfo resultInfo)
}