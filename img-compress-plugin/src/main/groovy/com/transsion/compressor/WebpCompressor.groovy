package com.transsion.compressor

import java.nio.file.Files
import java.nio.file.Paths

class WebpCompressor {

     static def convertToWebP(String inputImagePath) {
        println("进入转换webp图片的任务")
        def outputImagePath = inputImagePath.replaceAll(/\.png$/, ".webp")
        def command = "cwebp -q 75 ${inputImagePath} -o ${outputImagePath}"
        Process process = command.execute()
        process.waitFor()
        println("Converted ${inputImagePath} to ${outputImagePath}")

        //delete original img
        println(Paths.get(inputImagePath))
        def deleteOriginImgResult = Files.deleteIfExists(Paths.get(inputImagePath))
        println("Deleted original PNG image: ${inputImagePath},deleteOriginImgResult:${deleteOriginImgResult}")
    }

}