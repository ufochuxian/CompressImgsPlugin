package com.transsion.compressor
class WebpCompressor {

    static def convertToWebP(String imagePath) {
        // Example of converting to WebP using `cwebp` command line tool
        // Replace with appropriate command or library for WebP conversion
        def webpPath = "${imagePath}.webp"
        def command = "cwebp -q 75 ${imagePath} -o ${webpPath}"
        println("convert webp,command:${command}")
        Process process = command.execute()
        process.waitFor()
    }

}