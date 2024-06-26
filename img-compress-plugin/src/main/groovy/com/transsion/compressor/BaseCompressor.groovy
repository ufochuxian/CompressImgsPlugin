package com.transsion.compressor

import com.transsion.CompressInfo
import com.transsion.ImgCompressExtension
import com.transsion.ResultInfo
import org.gradle.api.Project

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
            WebpCompressor.convertImageToWebP(sourceFileAbsPath)
            WebpCompressor.deleteSourceImg(sourceFileAbsPath)
        }
    }
}