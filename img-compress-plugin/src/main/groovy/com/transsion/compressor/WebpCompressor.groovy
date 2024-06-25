package com.transsion.compressor

import java.nio.file.Files
import java.nio.file.Paths

class WebpCompressor {

    static def convertImageToWebP(String inputImagePath) {
        println("进入转换webp图片的任务")
        def outputImagePath = inputImagePath.replaceAll(/\.png$/, ".webp")
        def command = "cwebp -q 75 ${inputImagePath} -o ${outputImagePath}"
        Process process = command.execute()
        process.waitFor()
        println("Converted ${inputImagePath} to ${outputImagePath}")
    }

    static void deleteSourceImg(String inputImagePath) {
        //delete original img
        def deleteOriginImgResult = Files.deleteIfExists(Paths.get(inputImagePath))
        println("Deleted original PNG image: ${inputImagePath},deleteOriginImgResult:${deleteOriginImgResult}")
    }

    static def convertImagesToWebP(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            dir.eachFileRecurse { file ->
                if (file.isFile() && file.name.endsWith('.png') || file.name.endsWith('.jpg')) {
                    convertImageToWebP(file.path)
                }
            }
        } else {
            println("Directory ${dir.path} does not exist or is not a directory.")
        }
    }
}