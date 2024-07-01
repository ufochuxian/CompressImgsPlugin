package com.transsion.compressor

import com.transsion.CompressInfo
import com.transsion.ImgCompressExtension
import com.transsion.ResultInfo
import com.transsion.util.FileUtils
import com.transsion.util.Logger
import com.transsion.util.PngquantUtil
import com.transsion.util.ZopflipngUtil
import org.gradle.api.Project
import org.gradle.api.tasks.Input

class ZopflipngCompressor extends BaseCompressor {
//    def project;
    @Input
    def compressInfoList = new ArrayList<CompressInfo>()
    @Input
    ImgCompressExtension config
    def beforeTotalSize = 0
    def afterTotalSize = 0
    @Input
    Logger log
    def skipCount=0 //用于压缩后变大的情况

    @Override
    void compress(Project rootProject, List<CompressInfo> unCompressFileList, ImgCompressExtension config, ResultInfo resultInfo) {
        this.rootProject = rootProject
        this.compressInfoList = compressInfoList
        this.config = config
        log = Logger.getInstance(rootProject)
        ZopflipngUtil.copyZopflipng2BuildFolder(rootProject)
        log.i("使用ZopflipngCompressor进行压缩")
        log.i("type>>ZopflipngCompressor init....")
        PngquantUtil.copyPngquant2BuildFolder(rootProject)
        def zopflipng = ZopflipngUtil.getZopflipngFilePath(rootProject)
        unCompressFileList.each { info ->
            File originFile = new File(info.path)
            String type = originFile.getAbsolutePath().substring(originFile.getAbsolutePath().indexOf("."))
            long originalSize = originFile.length()
            Process process = new ProcessBuilder(zopflipng, "-y", "-m",info.path,info.outputPath).redirectErrorStream(true).start();

            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))
            StringBuilder error = new StringBuilder()
            String line
            while (null != (line = br.readLine())) {
                error.append(line)
            }
            int exitCode = process.waitFor()

            if (exitCode == 0) {
                super.onCompressed(info)
                long optimizedSize = new File(info.outputPath).length()
                float rate = 1.0f * (originalSize - optimizedSize) / originalSize * 100
                info.update(originalSize,optimizedSize,FileUtils.generateMD5(new File(info.outputPath)))
                log.i("Succeed! ${FileUtils.formetFileSize(originalSize)}-->${FileUtils.formetFileSize(optimizedSize)}, ${rate}% saved! ${info.outputPath}")
                beforeTotalSize += originalSize
                afterTotalSize += optimizedSize
            } else if (exitCode == 98) {
                log.w("Skipped! ${info.outputPath}")
                skipCount++
            } else {
                log.e("Failed! ${info.outputPath}")
                skipCount++
            }
        }

//        log.i("Task finish, compress ${unCompressFileList.size()} files, before total size: ${FileUtils.formetFileSize(beforeTotalSize)} after total size: ${FileUtils.formetFileSize(afterTotalSize)}")
        resultInfo.compressedSize = unCompressFileList.size()
        resultInfo.beforeSize = beforeTotalSize
        resultInfo.afterSize = afterTotalSize
        resultInfo.skipCount = skipCount
    }






}