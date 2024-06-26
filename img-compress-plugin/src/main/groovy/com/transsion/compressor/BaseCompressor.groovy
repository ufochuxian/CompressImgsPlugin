package com.transsion.compressor

import com.transsion.CompressInfo
import com.transsion.ImgCompressExtension
import com.transsion.ResultInfo
import org.gradle.api.Project
import org.gradle.internal.impldep.org.apache.http.util.TextUtils

public class BaseCompressor implements ICompressor {

    def rootProject;

    @Override
    void compress(Project rootProject, List<CompressInfo> unCompressFileList, ImgCompressExtension config, ResultInfo resultInfo) {

    }

    def deleteSourceFiles(String sourcePath) {
        //delete original img
        def deleteOriginImgResult = Files.deleteIfExists(Paths.get(sourcePath))
        println("Deleted original PNG image: ${inputImagePath},deleteOriginImgResult:${deleteOriginImgResult}")
    }

    public onCompressed(CompressInfo compressedInfo) {
        def sourceFile = new File(compressedInfo.outputPath)
        if (sourceFile.exists()) {
            def sourceFileAbsPath = sourceFile.absolutePath
            WebpCompressor.convertImageToWebP(sourceFileAbsPath, this.rootProject, { outputImagePath ->
                if (outputImagePath != null) {
                    println("回调: 转换成功，输出文件路径为 ${outputImagePath}")
                    // 在这里可以进行其他处理，如删除源图像
                    compressedInfo.outputPath = outputImagePath
                    WebpCompressor.deleteSourceImg(sourceFileAbsPath)
                } else {
                    println("回调: 转换失败")
                }
            })
        }
    }
}