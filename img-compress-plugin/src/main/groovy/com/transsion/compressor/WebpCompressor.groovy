package com.transsion.compressor

import com.transsion.util.CwebpUtil
import org.gradle.api.Project

import java.nio.file.Files
import java.nio.file.Paths

class WebpCompressor {

    static def convertImageToWebP(String inputImagePath, Project project, Closure onComplete) {
        println("进入转换webp图片的任务")
        def outputImagePath = inputImagePath.replaceAll(/\.(png|jpg|jpeg)$/, ".webp")

        CwebpUtil.copyCwebp2BuildFolder(project)

        def cwebpExecutablePath = CwebpUtil.getCwebpFilePath(project)
//        def command = "${cwebpExecutablePath} -q 75 ${inputImagePath} -o ${outputImagePath}"

        def command = "cwebp -q 75 ${inputImagePath} -o ${outputImagePath}"
        Process process = command.execute()
        process.waitFor()
        if (process.exitValue() == 0) {
            println("Converted ${inputImagePath} to ${outputImagePath}")
            if (onComplete != null) {
                onComplete.call(outputImagePath)
            }
        } else {
            println("Conversion failed for ${inputImagePath}")
            if (onComplete != null) {
                onComplete.call(null)
            }
        }
    }

    static void deleteSourceImg(String inputImagePath) {
        //delete original img
        if(inputImagePath.endsWith("png") || inputImagePath.endsWith("jpg")||inputImagePath.endsWith("jpeg")) {
            def deleteOriginImgResult = Files.deleteIfExists(Paths.get(inputImagePath))
            println("Deleted original PNG image: ${inputImagePath},deleteOriginImgResult:${deleteOriginImgResult}")
        }
    }
}