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
    }

}